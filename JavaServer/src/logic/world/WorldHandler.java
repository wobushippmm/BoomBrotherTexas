package logic.world;

import java.util.HashMap;
import java.util.Iterator;

import logic.common.LogicManager;
import logic.common.data.DataManager;
import logic.common.data.UserData;
import logic.common.redis.RedisHandler;
import logic.common.redis.RedisKey;

import org.apache.log4j.Logger;

import com.google.protobuf.GeneratedMessageV3.Builder;
import com.google.protobuf.InvalidProtocolBufferException;

import protocol.ProtoUtil;
import protocol.GameData.ChangeNicknameRpc;
import protocol.GameData.EnterWorldRpc;
import protocol.GameData.ExitWorldRpc;
import protocol.GameData.FriendChangeNicknameRep;
import protocol.GameData.FriendDat;
import core.log.LoggerHelper;
import core.net.DataPackage;
import core.net.NetManager;

public class WorldHandler {
	private Logger log = LoggerHelper.getLogger();
	public static WorldHandler instance = null;

	public HashMap<String, WorldUserData> userDic = new HashMap<String, WorldUserData>();
	
	public WorldHandler(){
		instance = this;
		
		LogicManager.logicThread.endLogicFuncs.add(new Object[]{"closeServer", this});
		
		LogicManager.logicThread.setRpc(EnterWorldRpc.class, this);
		LogicManager.logicThread.setRpc(ExitWorldRpc.class, this);
		LogicManager.logicThread.setRpc(ChangeNicknameRpc.class, this);
	}
	// 服务器关闭时处理
	public void closeServer(){
		// 保存玩家数据
		for(WorldUserData user : userDic.values()){
			RedisHandler.instance.saveWorldUser(user);
		}
	}
	
	public void onChangeNicknameRpc(DataPackage data){
		ChangeNicknameRpc rpc;
		try {
			rpc = ChangeNicknameRpc.parseFrom(data.rpcPo.getAnyPo());
			WorldUserData user = RedisHandler.instance.queryWorldUser(rpc.getUsername());
			if(user == null){
				return;
			}
			
			user.nickname = rpc.getNickname();
			
			// 通知上线好友
			Iterator<FriendDat.Builder> iter = user.friendList.values().iterator();
			FriendChangeNicknameRep.Builder rep = FriendChangeNicknameRep.newBuilder();
			rep.setUsername(user.username);
			rep.setNickname(user.nickname);
			
			while(iter.hasNext()){
				FriendDat.Builder friend = iter.next();
				WorldUserData friendData = WorldHandler.instance.userDic.get(friend.getUsername());
				if(friendData != null){
					friendData.friendList.get(user.username).setNickname(user.nickname);
					WorldHandler.instance.sendToClient(friendData.username, rep);
				}
			}
		} catch (InvalidProtocolBufferException e) {
			log.error(e);
		}
	}
	public void onEnterWorldRpc(DataPackage data){
		String username = data.rpcPo.getClientName();
		// 如果已经有
		WorldUserData user = userDic.get(username);
		if(user == null){
			user = RedisHandler.instance.queryWorldUser(username);
			user.gateway = data.termianl.getSocketName();
			userDic.put(username, user);
		}
		
		user.gateway = data.termianl.getSocketName();
		FriendsHandler.instance.friendOnline(user);
	}
	
	public void onExitWorldRpc(DataPackage data){
		WorldUserData user = userDic.remove(data.rpcPo.getClientName());
		
		if(user != null){
			FriendsHandler.instance.friendOffline(user);
		}
	}
	
	public void sendToClient(String username, Builder<?> rep){
		WorldUserData wud = userDic.get(username);
		if(wud != null){
			LogicManager.logicThread.sendToClient(username, ProtoUtil.packData(rep), NetManager.getServer(wud.gateway));
		}
	}
}
