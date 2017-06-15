package logic.scene;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import com.google.protobuf.InvalidProtocolBufferException;

import protocol.GameData.ExitBattleRpc;
import protocol.GameData.JoinBattleRep;
import protocol.GameData.JoinBattleReq;
import protocol.GameData.JoinBattleResultEnm;
import protocol.GameData.JoinBattleRpc;
import protocol.GameData.SetServerRpc;
import protocol.GameData.UserDat;
import protocol.ProtoUtil;
import core.config.Constant;
import core.log.LoggerHelper;
import core.net.DataPackage;
import core.net.NetManager;
import core.net.SocketThread;
import logic.common.LogicManager;
import logic.common.Sender;
import logic.common.data.DataManager;
import logic.common.data.UserData;
import logic.common.redis.RedisKey;

public class AllocBattleHandler {
	public static AllocBattleHandler instance = null;
	private Logger log = LoggerHelper.getLogger();
	
	public AllocBattleHandler(){
		instance = this;
		
		LogicManager.logicThread.setRpc(JoinBattleReq.class, this);
		LogicManager.logicThread.setRpc(ExitBattleRpc.class, this);
		
	}
	

	// 离开牌局
	public void onExitBattleRpc(DataPackage data){
		try {
			ExitBattleRpc exitTableRpc = ExitBattleRpc.parseFrom(data.rpcPo.getAnyPo());
			UserData user = DataManager.instance.getUser(exitTableRpc.getUsername());
			user.battle = "";
			NetManager.redis.hset(RedisKey.UserKey(exitTableRpc.getUsername()), RedisKey.Battle, "");
		} catch (InvalidProtocolBufferException e) {
			log.error(e);
		}
		
	}
	// 参加排位赛
	public void onJoinBattleReq(DataPackage data){
		try {
			JoinBattleReq req = JoinBattleReq.parseFrom(data.rpcPo.getAnyPo());
			UserData user = DataManager.instance.getUser(data.rpcPo.getClientName());
			if(user != null){
				if(!user.battle.equals("")){
					// 连续点进入两次，直接忽略
					return;
				}
				SocketThread battle = allocate(); // 分配一个战斗服
				NetManager.redis.hset(RedisKey.UserKey(user.username), RedisKey.Battle, battle.getSocketName());
				user.battle = battle.getSocketName();
				JoinBattleRpc.Builder joinBattleRpc = JoinBattleRpc.newBuilder();
				UserDat.Builder userDat = user.toUserDat();
				joinBattleRpc.setUser(userDat);
				// 加入battle
				Sender.sendToServer(battle.getSocketName(), ProtoUtil.packData(joinBattleRpc));
			}
		} catch (InvalidProtocolBufferException e) {
			log.error(e);
		}
	}
	// 选择一个战斗服
	private SocketThread allocate(){
		// 分配一个scene
		HashMap<String, SocketThread> hash = NetManager.getServersByType(Constant.TYPE_BATTLE);
		for(SocketThread server : hash.values()){
			return server;
		}
		return null;
	}
}
