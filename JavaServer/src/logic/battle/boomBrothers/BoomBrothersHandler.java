package logic.battle.boomBrothers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import logic.battle.boomBrothers.entity.EntityCreator;
import logic.battle.texasHoldem.GameTable;
import logic.common.LogicManager;
import logic.common.Sender;
import logic.common.data.DataManager;
import logic.common.data.UserData;

import org.apache.log4j.Logger;

import com.google.protobuf.InvalidProtocolBufferException;

import protocol.BoomGameData.BanHeroRep;
import protocol.BoomGameData.BanHeroReq;
import protocol.BoomGameData.BattleActionReq;
import protocol.BoomGameData.BattleLoadCompletedReq;
import protocol.BoomGameData.CancelMatchBattleRep;
import protocol.BoomGameData.CancelMatchBattleReq;
import protocol.BoomGameData.LeaveBattleReq;
import protocol.BoomGameData.MatchBattleRep;
import protocol.BoomGameData.MatchBattleResultEnm;
import protocol.BoomGameData.RematchBattleRep;
import protocol.BoomGameData.RematchCauseEnm;
import protocol.BoomGameData.SelectHeroReq;
import protocol.ProtoUtil;
import protocol.GameData.ClientDisconnectRpc;
import protocol.GameData.ExitBattleRep;
import protocol.GameData.ExitBattleReq;
import protocol.GameData.ExitBattleRpc;
import protocol.GameData.JoinBattleRep;
import protocol.GameData.JoinBattleRpc;
import protocol.BoomGameData.MatchBattleReq;
import protocol.GameData.RejoinBattleRpc;
import core.log.LoggerHelper;
import core.net.DataPackage;
import core.net.NetManager;
import core.udp.UdpDataPackage;

// 爆炸兄弟
public class BoomBrothersHandler {
	private Logger log = LoggerHelper.getLogger();
	public static BoomBrothersHandler instance = null;
	
	// 匹配更新时间
	private long updateTime = 0;
	// [username time]
	private ArrayList<Object[]> queue = new ArrayList<Object[]>();
	
	public HashMap<Integer, BattleField> battleFieldDic = new HashMap<Integer, BattleField>();
	
	public BoomBrothersHandler(){
		instance = this;
		
		// 公用接口
		LogicManager.logicThread.setRpc(JoinBattleRpc.class, this);
		LogicManager.logicThread.setRpc(ExitBattleReq.class, this);
		LogicManager.logicThread.setRpc(RejoinBattleRpc.class, this);
		LogicManager.logicThread.setRpc(ClientDisconnectRpc.class, this);
		LogicManager.logicThread.setRpc(MatchBattleReq.class, this);
		LogicManager.logicThread.setRpc(CancelMatchBattleReq.class, this);
		LogicManager.logicThread.setRpc(LeaveBattleReq.class, this);
		LogicManager.logicThread.setRpc(BanHeroReq.class, this);
		LogicManager.logicThread.setRpc(SelectHeroReq.class, this);
		
		// udp 接口，绕过gateway直接到battle，后期需要考虑安全性
		LogicManager.logicThread.setRpc(BattleLoadCompletedReq.class, this);
		LogicManager.logicThread.setRpc(BattleActionReq.class, this);

		LogicManager.logicThread.loopLogicFuncs.add(new Object[]{"onUpdate", this});
	}
	public void onBattleActionReq(UdpDataPackage data){
		try {
			BattleActionReq actReq = BattleActionReq.parseFrom(data.rpcPo.getAnyPo());
			BattleField bf = getBattleField(actReq.getUsername(), actReq.getCid());
			if(bf != null){
				bf.setActionReq(actReq);
			}
		} catch (InvalidProtocolBufferException e) {
			log.error(e);
		}
	}
	
	public void gameOver(BattleField bf){
		battleFieldDic.remove(bf.id);
	}
	// 验证cid
	public BattleField getBattleField(String username, int cid){
		UserData user = DataManager.instance.getUser(username);
		if(user != null && user.clientID == cid){ // 验证cid
			BattleField bf = battleFieldDic.get(user.battleFieldID);
			return bf;
		}
		return null;
	}
	// 不验证cid
	public BattleField getBattleField(String username){
		UserData user = DataManager.instance.getUser(username);
		if(user != null){
			BattleField bf = battleFieldDic.get(user.battleFieldID);
			return bf;
		}
		return null;
	}
	public void onBattleLoadCompletedReq(UdpDataPackage data){
		try {
			BattleLoadCompletedReq completeReq = BattleLoadCompletedReq.parseFrom(data.rpcPo.getAnyPo());
			BattleField bf = getBattleField(completeReq.getUsername(), completeReq.getCid());
			if(bf != null){
				bf.loadComplete(data, completeReq);
			}
		} catch (InvalidProtocolBufferException e) {
			log.error(e);
		}
		
	}
	// 有人没ban或者选英雄，重新匹配
	public void rematch(BattleField bf, String runner){
		battleFieldDic.remove(bf.id);
		for(int i=0; i<bf.playerList.size(); i++){
			UserData user = DataManager.instance.getUser(bf.playerList.get(i).username);
			if(user != null){
				if(user.username.equals(runner)){
					user.gameCount += 1; // 记输一场
					user.battleFieldID = 0;
				}else{
					user.battleFieldID = 0;
					queue.add(0, new Object[]{user.username, System.currentTimeMillis()});
				}
			}
		}
		
		RematchBattleRep.Builder rematchRep = RematchBattleRep.newBuilder();
		rematchRep.setCause(RematchCauseEnm.SOMEONE_RUN);
		rematchRep.setRunner(runner);
		bf.broadcast(rematchRep);
	}
	public void onBanHeroReq(DataPackage data){
		BattleField bf = getBattleField(data);
		if(bf == null){
			return;
		}
		BanHeroReq banReq;
		try {
			banReq = BanHeroReq.parseFrom(data.rpcPo.getAnyPo());
			bf.setBanHero(data.rpcPo.getClientName(), banReq.getHeroTempID());
		} catch (InvalidProtocolBufferException e) {
			log.error(e);
		}
	}
	public void onSelectHeroReq(DataPackage data){
		BattleField bf = getBattleField(data);
		if(bf == null){
			return;
		}
		SelectHeroReq selectReq;
		try {
			selectReq = SelectHeroReq.parseFrom(data.rpcPo.getAnyPo());
			bf.setSelectHero(data.rpcPo.getClientName(), selectReq.getHeroTempID());
		} catch (InvalidProtocolBufferException e) {
			log.error(e);
		}
	}
	public BattleField getBattleField(DataPackage data){
		UserData user = DataManager.instance.getUser(data.rpcPo.getClientName());
		if(user == null){
			return null;
		}
		BattleField bf = battleFieldDic.get(user.battleFieldID);
		if(bf == null){
			return null;
		}
		return bf;
	}
	public int onUpdate(){
		Iterator<BattleField> iter = battleFieldDic.values().iterator();
		// 更新牌局逻辑
		while(iter.hasNext()){
			iter.next().onUpdate();
		}
		return onQueueUpdate();
	}
	// 定时器触发
	// 一直判定为闲
	private int onQueueUpdate(){
		long now = System.currentTimeMillis();
		if(now - updateTime < 1000){ // 每一秒执行一次
			return 0;
		}
		updateTime = now;
		
		if(queue.size() >= 2){
			BattleField bf = new BattleField(2);
			bf.id = BattleField.newID();
			int n = 0;
			while(n++ < 2){
				bf.addUser(DataManager.instance.getUser((String) queue.remove(0)[0]));
			}
			battleFieldDic.put(bf.id, bf);
			// 广播战场信息
			bf.broadcast(bf.toBattleFieldInfoRep());
		}else if(queue.size() > 0){ // 人数不满
			BattleField bf = new BattleField(2);
			bf.id = BattleField.newID();
			int n = 0;
			while(queue.size() > 0){
				if(now - (long)queue.get(0)[1] > 10000){ // 大于1分钟则进入游戏
					bf.addUser(DataManager.instance.getUser((String) queue.remove(0)[0]));
					n++;
				}else{
					break;
				}
			}
			if(n > 0){
				battleFieldDic.put(bf.id, bf);
				while(n < 2){
					bf.addAI("AI" + n++);
				}
				// 广播战场信息
				bf.broadcast(bf.toBattleFieldInfoRep());
			}
		}
		return 0;
	}
	// 匹配战场
	public void onMatchBattleReq(DataPackage data){
		MatchBattleRep.Builder matchBattleRep = MatchBattleRep.newBuilder();
		UserData user = DataManager.instance.getUser(data.rpcPo.getClientName());
		if(user == null){
			return;
		}
		for(int i=0; i<queue.size(); i++){
			if(queue.get(i)[0].equals(user.username)){ // 已经在队列
				matchBattleRep.setResult(MatchBattleResultEnm.ALREADY_IN_MATCH_QUEUE);
				Sender.sendToClient(user.username, ProtoUtil.packData(matchBattleRep));
				return;
			} 
		}
		if(user.battleFieldID != 0){ // 已经在战场
			BattleField bf = battleFieldDic.get(user.battleFieldID);
			if(bf != null){
				matchBattleRep.setResult(MatchBattleResultEnm.ALREADY_IN_BATTLE_FIELD);
				Sender.sendToClient(user.username, ProtoUtil.packData(matchBattleRep));
				return;
			}
			user.battleFieldID = 0;
		}
		queue.add(new Object[]{user.username, System.currentTimeMillis()});
		matchBattleRep.setResult(MatchBattleResultEnm.OK_MATCHBATTLERESULT);
		Sender.sendToClient(user.username, ProtoUtil.packData(matchBattleRep));
	}
	public void onCancelMatchBattleReq(DataPackage data){
		UserData user = DataManager.instance.getUser(data.rpcPo.getClientName());
		if(user == null){
			return;
		}
		for(int i=0; i<queue.size(); i++){
			if(queue.get(i)[0].equals(user.username)){ // 已经在队列
				queue.remove(i);
				CancelMatchBattleRep.Builder cancelRep = CancelMatchBattleRep.newBuilder();
				Sender.sendToClient(user.username, ProtoUtil.packData(cancelRep));
				return;
			} 
		}
	}
	public void onLeaveBattleReq(DataPackage data){
		UserData user = DataManager.instance.getUser(data.rpcPo.getClientName());
		if(user == null){
			return;
		}
		BattleField bf = battleFieldDic.get(user.battleFieldID);
		if(bf != null){
			bf.leaveBattleField(user.username);
		}
	}
	public void onClientDisconnectRpc(DataPackage data){
		// 玩家掉线
		// 
		UserData user = DataManager.instance.getUser(data.rpcPo.getClientName());
		if(user != null){
			for(int i=0; i<queue.size(); i++){
				if(queue.get(i)[0].equals(user.username)){ // 已经在队列
					queue.remove(i);
					break;
				} 
			}
			
			BattleField bf = battleFieldDic.get(user.tableID);
			if(bf != null){
				bf.setIsOffline(user.username, true);
			}
		}
	}
	// 退出battle
	public void onExitBattleReq(DataPackage data){
		try {
			ExitBattleReq req = ExitBattleReq.parseFrom(data.rpcPo.getAnyPo());
			UserData user = DataManager.instance.delUser(data.rpcPo.getClientName());
			// 通知客户端
			ExitBattleRep.Builder exitBattleRep = ExitBattleRep.newBuilder();
			Sender.sendToClient(data.rpcPo.getClientName(), ProtoUtil.packData(exitBattleRep));
			
			// 通知scene
			ExitBattleRpc.Builder exitBattleRpc = ExitBattleRpc.newBuilder();
			exitBattleRpc.setUsername(data.rpcPo.getClientName());
			Sender.sendToServer(user.scene, ProtoUtil.packData(exitBattleRpc));
		} catch (InvalidProtocolBufferException e) {
			log.error(e);
		}
	}
	// 断线重连
	public void onRejoinBattleRpc(DataPackage data){
		try {
			RejoinBattleRpc req = RejoinBattleRpc.parseFrom(data.rpcPo.getAnyPo());
			UserData user = DataManager.instance.getUser(data.rpcPo.getClientName());
			if(user != null){
				BattleField bf = battleFieldDic.get(user.battleFieldID);
				if(bf != null){
					// 先设置进战场
					JoinBattleRep.Builder joinBattleRep = JoinBattleRep.newBuilder();
					joinBattleRep.setBattleName(NetManager.name);
					Sender.sendToClient(user.username, ProtoUtil.packData(joinBattleRep));

					bf.getHeroByUsername(user.username).send(ProtoUtil.packData(bf.toBattleFieldInfoRep()));
					bf.setIsOffline(user.username, false);
				}
			}
		} catch (InvalidProtocolBufferException e) {
			log.error(e);
		}
		
	}
	// 开始游戏
	public void onJoinBattleRpc(DataPackage data){
		JoinBattleRpc rpc;
		try {
			rpc = JoinBattleRpc.parseFrom(data.rpcPo.getAnyPo());
			UserData user = new UserData();
			user.fromUserPo(rpc.getUser());
			DataManager.instance.addUser(user);
			user.battle = NetManager.name;
			
			JoinBattleRep.Builder joinBattleRep = JoinBattleRep.newBuilder();
			joinBattleRep.setBattleName(NetManager.name);
			// 注意 Req的协议包才可以保证 data.rpcPo.getClientName()存在值
			Sender.sendToClient(user.username, ProtoUtil.packData(joinBattleRep));
		} catch (InvalidProtocolBufferException e) {
			log.error(e);
		}
		
	}
}
