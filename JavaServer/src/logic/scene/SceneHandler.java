package logic.scene;

import java.util.Date;

import org.apache.log4j.Logger;

import com.google.protobuf.InvalidProtocolBufferException;

import protocol.GameData.AddTempGoldRpc;
import protocol.GameData.ChangeNicknameRep;
import protocol.GameData.ChangeNicknameReq;
import protocol.GameData.ChangeNicknameResultEnm;
import protocol.GameData.ChangeNicknameRpc;
import protocol.GameData.ChangePortraitRep;
import protocol.GameData.ChangePortraitReq;
import protocol.GameData.ChangeSceneRep;
import protocol.GameData.ChangeSceneReq;
import protocol.GameData.ChangeSceneResultEnm;
import protocol.GameData.ClientDisconnectRpc;
import protocol.GameData.DailyLoginAwardRep;
import protocol.GameData.EnterSceneRep;
import protocol.GameData.EnterSceneRpc;
import protocol.GameData.ExitSceneRep;
import protocol.GameData.ExitSceneRpc;
import protocol.GameData.ExitWorldRpc;
import protocol.GameData.RejoinBattleRpc;
import protocol.GameData.SetEmailReadRpc;
import protocol.GameData.SetGoldCauseEnm;
import protocol.GameData.SetGoldRep;
import protocol.GameData.SetGoldRpc;
import protocol.GameData.UserDat;
import protocol.ProtoUtil;
import protocol.GameData.ConnectReq;
import protocol.Protocol.RpcPo;
import utils.DateUtil;
import core.config.Constant;
import core.log.LoggerHelper;
import core.net.DataPackage;
import core.net.NetManager;
import core.net.SocketThread;
import logic.common.LogicManager;
import logic.common.SendLog;
import logic.common.Sender;
import logic.common.data.DataManager;
import logic.common.data.UserData;
import logic.common.redis.RedisHandler;
import logic.common.redis.RedisKey;

public class SceneHandler {
	private Logger log = LoggerHelper.getLogger();
	public static SceneHandler instance = null;
	
	public SceneHandler(){
		instance = this;
		
		LogicManager.logicThread.endLogicFuncs.add(new Object[]{"closeServer", this});
		
		LogicManager.logicThread.setRpc(EnterSceneRpc.class, this);
		LogicManager.logicThread.setRpc(ExitSceneRpc.class, this);
		LogicManager.logicThread.setRpc(ChangeSceneReq.class, this);
		LogicManager.logicThread.setRpc(ClientDisconnectRpc.class, this);
		LogicManager.logicThread.setRpc(SetEmailReadRpc.class, this);
		LogicManager.logicThread.setRpc(ChangeNicknameReq.class, this);
		LogicManager.logicThread.setRpc(AddTempGoldRpc.class, this);
		LogicManager.logicThread.setRpc(SetGoldRpc.class, this);
		LogicManager.logicThread.setRpc(ChangePortraitReq.class, this);
	}
	public void onChangePortraitReq(DataPackage data){
		try {
			ChangePortraitReq req = ChangePortraitReq.parseFrom(data.rpcPo.getAnyPo());
			UserData user = DataManager.instance.getUser(data.rpcPo.getClientName());
			if(user != null){ // 离线
				user.portrait = req.getPortrait();
				
				ChangePortraitRep.Builder rep = ChangePortraitRep.newBuilder();
				rep.setPortrait(user.portrait);
				Sender.sendToClient(user.username, ProtoUtil.packData(rep));
			}
		} catch (InvalidProtocolBufferException e) {
			log.error(e);
		}
		
	}
	public void onSetGoldRpc(DataPackage data){
	 	SetGoldRpc rpc;
		try {
			rpc = SetGoldRpc.parseFrom(data.rpcPo.getAnyPo());
			UserData user = DataManager.instance.getUser(rpc.getUsername());
			if(user == null){ // 离线
				String key = RedisKey.UserKey(rpc.getUsername());
				if(NetManager.redis.exists(key)){
					NetManager.redis.hset(key, RedisKey.Gold, rpc.getGold()+"");
				}
			}else{
				user.gold = rpc.getGold();
				
				// 通知客户端
				SetGoldRep.Builder setGoldRep = SetGoldRep.newBuilder();
				setGoldRep.setGold(user.gold);
				setGoldRep.setCause(rpc.getCause());
				Sender.sendToClient(user.username, ProtoUtil.packData(setGoldRep));
			}
		} catch (InvalidProtocolBufferException e) {
			log.error(e);
		}
	}
	public void onAddTempGoldRpc(DataPackage data){
		AddTempGoldRpc rpc;
		try {
			rpc = AddTempGoldRpc.parseFrom(data.rpcPo.getAnyPo());
			UserData user = DataManager.instance.getUser(rpc.getUsername());
			if(user == null){
				return;
			}
			
			getTempGold(user);
			
			// 通知客户端
			SetGoldRep.Builder setGoldRep = SetGoldRep.newBuilder();
			setGoldRep.setGold(user.gold);
			setGoldRep.setCause(SetGoldCauseEnm.BY_GM);
			Sender.sendToClient(user.username, ProtoUtil.packData(setGoldRep));
		} catch (InvalidProtocolBufferException e) {
			log.error(e);
		}
	}
	
	public void onChangeNicknameReq(DataPackage data){
		try {
			UserData user = DataManager.instance.getUser(data.rpcPo.getClientName());
			if(user == null){
				return;
			}
			
			ChangeNicknameReq changeReq = ChangeNicknameReq.parseFrom(data.rpcPo.getAnyPo());
			
			ChangeNicknameRep.Builder changeRep = ChangeNicknameRep.newBuilder();
			
			if(changeReq.getNickname().length() < 1 && changeReq.getNickname().length() > 12){
				changeRep.setResult(ChangeNicknameResultEnm.ERROR_FORMAT);
			}else if(NetManager.redis.hexists(RedisKey.NicknameTable, changeReq.getNickname())){
				// 已经存在
				changeRep.setResult(ChangeNicknameResultEnm.NICKNAME_USED);
			}else if(NetManager.redis.exists(RedisKey.UserKey(changeRep.getNickname()))){
				// 是用户名
				changeRep.setResult(ChangeNicknameResultEnm.NICKNAME_USED);
			}else{
				NetManager.redis.hdel(RedisKey.NicknameTable, user.nickname);
				NetManager.redis.hset(RedisKey.NicknameTable, changeReq.getNickname(), user.username);
				user.nickname = changeReq.getNickname();
				
				changeRep.setResult(ChangeNicknameResultEnm.OK_CHANGENICKNAMERESULT);
				changeRep.setNickname(changeReq.getNickname());
				
				ChangeNicknameRpc.Builder changeRpc = ChangeNicknameRpc.newBuilder();
				changeRpc.setUsername(user.username);
				changeRpc.setNickname(user.nickname);
				Sender.sendToServer(getWorld().getSocketName(), ProtoUtil.packData(changeRpc));
			}
			Sender.sendToClient(user.username, ProtoUtil.packData(changeRep));
		} catch (InvalidProtocolBufferException e) {
			log.error(e);
		}
		
	}
	public SocketThread getWorld(){
		for(SocketThread world : NetManager.getServersByType(Constant.TYPE_WORLD).values()){
			return world;
		}
		return null;
	}
	// 服务器关闭时处理
	public void closeServer(){
		// 保存玩家数据
		for(UserData user : DataManager.instance.userDic.values()){
			RedisHandler.instance.saveUser(user);
		}
	}
	public void onSetEmailReadRpc(DataPackage data){
		try {
			SetEmailReadRpc rpc = SetEmailReadRpc.parseFrom(data.rpcPo.getAnyPo());
			UserData user = DataManager.instance.getUser(rpc.getUsername());
			if(user != null){
				user.gold += rpc.getGold();
				
				SetGoldRep.Builder setGoldRep = SetGoldRep.newBuilder();
				setGoldRep.setGold(user.gold);
				setGoldRep.setCause(SetGoldCauseEnm.FROM_EMAIL);
				Sender.sendToClient(user.username, ProtoUtil.packData(setGoldRep));
			}
		} catch (InvalidProtocolBufferException e) {
			log.error(e);
		}
		
	}
	// 进入场景
	public void onEnterSceneRpc(DataPackage data){
		try {
			EnterSceneRpc req = EnterSceneRpc.parseFrom(data.rpcPo.getAnyPo());
			UserData user = DataManager.instance.getUser(req.getUsername()); // 可能还在缓存中未清除，刷新页面快速登陆
			if(user == null){
				user = RedisHandler.instance.queryUser(req.getUsername());
			}
			user.gateway = data.termianl.getSocketName();
			user.scene = NetManager.name;
			user.clientID = req.getCid();
			DataManager.instance.addUser(user);
			
			// 这里也可以通知其它玩家加入
			EnterSceneRep.Builder builder = EnterSceneRep.newBuilder();
			builder.setScene(NetManager.name);
			builder.setUsername(user.username);
			builder.setUserDat(user.toUserDat());
			Sender.sendToClient(data.rpcPo.getClientName(), ProtoUtil.packData(builder));
			
			// 登录时 从gold缓存中收取gold
			// 不做另外通知
			getTempGold(user);
			RechargeHandler.instance.getRecharge(user);
			// 每日奖励
			Date today = new Date();
			if(!DateUtil.isSameDay(today, user.dailyAwardTime)){
				user.dailyAwardTime = today.getTime();
				user.gold += 10000;
				DailyLoginAwardRep.Builder dailyRep = DailyLoginAwardRep.newBuilder();
				dailyRep.setGold(10000);
				Sender.sendToClient(user.username, ProtoUtil.packData(dailyRep));
			}
			
			if(!user.battle.equals("")){
				RejoinBattleRpc.Builder rejoinBattleRpc = RejoinBattleRpc.newBuilder();
				rejoinBattleRpc.setUser(user.toUserDat());
				Sender.sendToServer(user.battle, ProtoUtil.packData(rejoinBattleRpc));
			}
		} catch (InvalidProtocolBufferException e) {
			log.error(e);
		}
	}
	public void getTempGold(UserData user){
		String key = RedisKey.TempGold(user.username);
		while(NetManager.redis.llen(key) > 0){
			String tgs = NetManager.redis.lpop(key);
			int tg = RedisHandler.parseInt(tgs);
			user.gold += tg;
			
			SendLog.sendGoldLog(user.username, tg, SetGoldCauseEnm.BY_GM);
		}
	}
	// 退出场景
	public void onExitSceneRpc(DataPackage data){
		try {
			ExitSceneRpc po = ExitSceneRpc.parseFrom(data.rpcPo.getAnyPo());
			// 处理玩家退出
			RedisHandler.instance.saveUser(
					DataManager.instance.delUser(data.rpcPo.getClientName()));
			// 通知其它玩家退出
		} catch (InvalidProtocolBufferException e) {
			log.error(e);
		}
	}
	
	public void onChangeSceneReq(DataPackage data){
		try {
			ChangeSceneReq req = ChangeSceneReq.parseFrom(data.rpcPo.getAnyPo());
			SocketThread server = NetManager.getServer(req.getScene());
			if(server != null){
				UserData user = DataManager.instance.delUser(data.rpcPo.getClientName());
				RedisHandler.instance.saveUser(user);
				EnterSceneRpc.Builder rpc = EnterSceneRpc.newBuilder();
				rpc.setUsername(user.username);
				Sender.sendToServer(server.getSocketName(), ProtoUtil.packData(rpc));
			}else{
				ChangeSceneRep.Builder rep = ChangeSceneRep.newBuilder();
				rep.setResult(ChangeSceneResultEnm.SCENE_CONNECT_FAIL);
				Sender.sendToClient(data.rpcPo.getClientName(), ProtoUtil.packData(rep));
			}
		} catch (InvalidProtocolBufferException e) {
			log.error(e);
		}
		
	}
	public void onClientDisconnectRpc(DataPackage data){
		// 保存数据
		// 处理玩家退出
		RedisHandler.instance.saveUser(
				DataManager.instance.delUser(data.rpcPo.getClientName()));
		// 通知其它玩家退出
	}
}
