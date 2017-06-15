package logic.scene;

import logic.common.LogicManager;
import logic.common.SendLog;
import logic.common.data.DataManager;
import logic.common.data.UserData;
import logic.common.redis.RedisHandler;
import logic.common.redis.RedisKey;

import org.apache.log4j.Logger;

import com.google.protobuf.InvalidProtocolBufferException;

import protocol.GameData.EnterSceneRpc;
import protocol.GameData.ExitSceneRpc;
import protocol.GameData.RechargeReq;
import protocol.GameData.RechargeRpc;
import protocol.GameData.SetGoldCauseEnm;
import core.log.LoggerHelper;
import core.net.DataPackage;
import core.net.NetManager;

public class RechargeHandler {
	private Logger log = LoggerHelper.getLogger();
	public static RechargeHandler instance = null;
	
	public RechargeHandler(){
		instance = this;
		
		LogicManager.logicThread.setRpc(RechargeRpc.class, this);
		LogicManager.logicThread.setRpc(RechargeReq.class, this);
	}
	
	public void onRechargeRpc(DataPackage data){
		try {
			RechargeRpc rpc = RechargeRpc.parseFrom(data.rpcPo.getAnyPo());
			UserData user = DataManager.instance.getUser(data.rpcPo.getClientName()); // 可能还在缓存中未清除，刷新页面快速登陆
			if(user == null){
				return;
			}
			getRecharge(user);
		} catch (InvalidProtocolBufferException e) {
			log.error(e);
		}
	}
	
	public void getRecharge(UserData user){
		String key = RedisKey.Recharge(user.username);
		while(NetManager.redis.llen(key) > 0){
			int recharge = RedisHandler.parseInt(NetManager.redis.lpop(key));
			int gold = recharge * 100;
			user.gold += gold; // 1块钱100点
			
			SendLog.sendGoldLog(user.username, gold, SetGoldCauseEnm.BY_RECHARGE);
		}
	}
	
	public void onRechargeReq(DataPackage data){
		try {
			RechargeReq req = RechargeReq.parseFrom(data.rpcPo.getAnyPo());
			UserData user = DataManager.instance.getUser(data.rpcPo.getClientName()); // 可能还在缓存中未清除，刷新页面快速登陆
			if(user == null){
				return;
			}
			NetManager.redis.hset(RedisKey.OrderList, req.getOrderID(), user.username); // 删除订单
		} catch (InvalidProtocolBufferException e) {
			log.error(e);
		}
	}
}
