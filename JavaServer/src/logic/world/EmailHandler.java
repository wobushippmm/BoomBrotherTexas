package logic.world;

import java.util.List;

import logic.common.LogicManager;
import logic.common.redis.RedisHandler;
import logic.common.redis.RedisKey;
import logic.database.DatabaseHandler;

import org.apache.log4j.Logger;

import com.google.protobuf.InvalidProtocolBufferException;

import protocol.GameData.EmailListRep;
import protocol.ProtoUtil;
import protocol.GameData.EmailDat;
import protocol.GameData.EmailRpc;
import protocol.GameData.GetEmailListReq;
import protocol.GameData.GetRankListReq;
import protocol.GameData.SendEmailReq;
import protocol.GameData.SetEmailReadRep;
import protocol.GameData.SetEmailReadReq;
import core.log.LoggerHelper;
import core.net.DataPackage;
import core.net.NetManager;
import core.net.SocketThread;

public class EmailHandler {
	private Logger log = LoggerHelper.getLogger();
	public static EmailHandler instance = null;
	
	public EmailHandler(){
		instance = this;
		
		// 邮件系统移动到world可能更合适
		LogicManager.logicThread.setRpc(EmailRpc.class, this);
		LogicManager.logicThread.setRpc(GetEmailListReq.class, this);
		LogicManager.logicThread.setRpc(SetEmailReadReq.class, this);
		LogicManager.logicThread.setRpc(SendEmailReq.class, this);
	}
	public void onSetEmailReadReq(DataPackage data){
		SetEmailReadReq req;
		try {
			req = SetEmailReadReq.parseFrom(data.rpcPo.getAnyPo());
			WorldUserData user = WorldHandler.instance.userDic.get(data.rpcPo.getClientName());
			if(user == null){
				return;
			}
			if(user.emailList.size() <= req.getIndex()){
				return;
			}
			if(!user.emailList.get(req.getIndex()).getRead()){ // 未读取则设置]
				SetEmailReadRep.Builder rep = SetEmailReadRep.newBuilder();
				rep.setIndex(req.getIndex());
				rep.setGold(user.emailList.get(req.getIndex()).getGold());
				
				user.emailList.get(req.getIndex()).setRead(true);
				
				LogicManager.logicThread.sendToClient(data.rpcPo.getClientName(), ProtoUtil.packData(rep), data.termianl.socketThread);
			}
			// 不返回
		} catch (InvalidProtocolBufferException e) {
			log.error(e);
		}
	}
	public void onGetEmailListReq(DataPackage data){
		WorldUserData user = WorldHandler.instance.userDic.get(data.rpcPo.getClientName());
		if(user != null){
			LogicManager.logicThread.sendToClient(data.rpcPo.getClientName(), ProtoUtil.packData(user.toEmailListRep()), data.termianl.socketThread);
		}
	}
	public void onSendEmailReq(DataPackage data){
		try {
			SendEmailReq req = SendEmailReq.parseFrom(data.rpcPo.getAnyPo());
			saveEmail(req.getEmail());
		} catch (InvalidProtocolBufferException e) {
			log.error(e);
		}
	}
	public void onEmailRpc(DataPackage data){
		try {
			EmailRpc rpc = EmailRpc.parseFrom(data.rpcPo.getAnyPo());
			saveEmail(rpc.getEmail());
		} catch (InvalidProtocolBufferException e) {
			log.error(e);
		}
	}
	public void saveEmail(EmailDat email){
		for(int i=0; i<email.getTargetCount(); i++){
			String target = email.getTarget(i);
			
			WorldUserData user = WorldHandler.instance.userDic.get(target);
			// 在线
			if(user != null){
				user.emailList.addFirst(email.toBuilder());
				LogicManager.logicThread.sendToClient(target, ProtoUtil.packData(user.toEmailListRep()), NetManager.getServer(user.gateway));
			}else{
				user = RedisHandler.instance.queryEmail(target);
				user.emailList.addLast(email.toBuilder());
				while(user.emailList.size() > 10){
					user.emailList.removeFirst();
				}
				RedisHandler.instance.saveEmail(user);
			}
		}
	}
}
