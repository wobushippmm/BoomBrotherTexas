package logic.gateway;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.google.protobuf.InvalidProtocolBufferException;

import protocol.GameData.AcceptFriendReq;
import protocol.GameData.RechargeReq;
import protocol.TexasGameData.ActionReq;
import protocol.GameData.AddFriendRequireReq;
import protocol.TexasGameData.CancelMatchTableReq;
import protocol.GameData.ChangeNicknameRep;
import protocol.GameData.ChangeNicknameReq;
import protocol.GameData.ChangePortraitReq;
import protocol.GameData.ChangeSceneRep;
import protocol.GameData.ChangeSceneReq;
import protocol.GameData.ChangeSceneResultEnm;
import protocol.GameData.ChatRep;
import protocol.GameData.ChatReq;
import protocol.GameData.ClientDisconnectRpc;
import protocol.GameData.DeleteFriendReq;
import protocol.GameData.EmailListRep;
import protocol.GameData.EnterSceneRpc;
import protocol.GameData.EnterWorldRpc;
import protocol.GameData.ExitBattleRep;
import protocol.GameData.ExitBattleReq;
import protocol.GameData.ExitBattleRpc;
import protocol.GameData.ExitWorldRpc;
import protocol.GameData.GetEmailListReq;
import protocol.GameData.GetRankListReq;
import protocol.TexasGameData.GetTableListRep;
import protocol.TexasGameData.GetTableListReq;
import protocol.TexasGameData.JoinTableReq;
import protocol.GameData.JoinBattleRep;
import protocol.GameData.JoinBattleReq;
import protocol.GameData.KickOutRpc;
import protocol.TexasGameData.LeaveTableReq;
import protocol.TexasGameData.MatchTableReq;
import protocol.GameData.RefuseFriendReq;
import protocol.GameData.SendEmailReq;
import protocol.GameData.SetEmailReadRep;
import protocol.GameData.SetEmailReadReq;
import protocol.GameData.SetEmailReadRpc;
import protocol.GameData.SetServerRpc;
import protocol.Protocol.RpcPo;
import protocol.ProtoUtil;
import protocol.GameData.ConnectReq;
import core.config.Constant;
import core.log.LoggerHelper;
import core.net.ClientThread;
import core.net.DataPackage;
import core.net.NetManager;
import core.net.SocketTerminal;
import core.net.SocketThread;
import logic.common.LogicManager;
import logic.common.LogicThread;
import logic.common.ServerHelper;
import logic.common.redis.RedisHandler;
import logic.common.redis.RedisKey;
import logic.login.LoginHandler;

public class GatewayHandler {
	private Logger log = LoggerHelper.getLogger();
	public static GatewayHandler instance = null;
	
	public GatewayHandler(){
		instance = this;
		GatewayLogic gatewayLogic = (GatewayLogic) LogicManager.logicThread;
		
		// 注册接口转发列表
		gatewayLogic.setRpc2Type(ChangeSceneReq.class, Constant.TYPE_SCENE);
		gatewayLogic.setRpc2Type(JoinBattleReq.class, Constant.TYPE_SCENE);
		gatewayLogic.setRpc2Type(ExitBattleReq.class, Constant.TYPE_BATTLE);
		gatewayLogic.setRpc2Type(ActionReq.class, Constant.TYPE_BATTLE);
		gatewayLogic.setRpc2Type(LeaveTableReq.class, Constant.TYPE_BATTLE);
		gatewayLogic.setRpc2Type(GetTableListReq.class, Constant.TYPE_BATTLE);
		gatewayLogic.setRpc2Type(MatchTableReq.class, Constant.TYPE_BATTLE);
		gatewayLogic.setRpc2Type(CancelMatchTableReq.class, Constant.TYPE_BATTLE);
		gatewayLogic.setRpc2Type(JoinTableReq.class, Constant.TYPE_BATTLE);
		gatewayLogic.setRpc2Type(GetRankListReq.class, Constant.TYPE_DATABASE);
		gatewayLogic.setRpc2Type(SendEmailReq.class, Constant.TYPE_WORLD);
		gatewayLogic.setRpc2Type(GetEmailListReq.class, Constant.TYPE_WORLD);
		gatewayLogic.setRpc2Type(SetEmailReadReq.class, Constant.TYPE_WORLD);
		gatewayLogic.setRpc2Type(EmailListRep.class, Constant.TYPE_WORLD);
		gatewayLogic.setRpc2Type(SetEmailReadRep.class, Constant.TYPE_WORLD);
		gatewayLogic.setRpc2Type(AddFriendRequireReq.class, Constant.TYPE_WORLD);
		gatewayLogic.setRpc2Type(AcceptFriendReq.class, Constant.TYPE_WORLD);
		gatewayLogic.setRpc2Type(RefuseFriendReq.class, Constant.TYPE_WORLD);
		gatewayLogic.setRpc2Type(DeleteFriendReq.class, Constant.TYPE_WORLD);
		gatewayLogic.setRpc2Type(ChangeNicknameReq.class, Constant.TYPE_SCENE);
		gatewayLogic.setRpc2Type(ChangePortraitReq.class, Constant.TYPE_SCENE);
		gatewayLogic.setRpc2Type(RechargeReq.class, Constant.TYPE_SCENE);
		
		// 接收接口
		gatewayLogic.setRpc(ConnectReq.class, this);
		gatewayLogic.setRpc(SetServerRpc.class, this);
		gatewayLogic.setRpc(ExitBattleRep.class, this);
		gatewayLogic.setRpc(JoinBattleRep.class, this);
		gatewayLogic.setRpc(ChangeSceneRep.class, this);
		gatewayLogic.setRpc(ChatReq.class, this); // 聊天只能在相同gateway聊
		gatewayLogic.setRpc(SetEmailReadRep.class, this); // 收附件
		gatewayLogic.setRpc(KickOutRpc.class, this);
		
		// 监听接口
		gatewayLogic.clientDisconnectFuncs.add(new Object[]{"clientDisconnect", this});
	}
	public void onKickOutRpc(DataPackage data){
		try {
			KickOutRpc rpc = KickOutRpc.parseFrom(data.rpcPo.getAnyPo());
			SocketTerminal client = NetManager.getClient(rpc.getUsername());
			if(client != null){
				client.quit();
			}
		} catch (InvalidProtocolBufferException e) {
			log.error(e);
		}
		
	}
	public void onSetEmailReadRep(DataPackage data){
		try {
			SetEmailReadRep rep = SetEmailReadRep.parseFrom(data.rpcPo.getAnyPo());
			
			SetEmailReadRpc.Builder rpc = SetEmailReadRpc.newBuilder();
			rpc.setUsername(data.rpcPo.getClientName());
			rpc.setGold(rep.getGold());
			
			SocketTerminal client = NetManager.getClient(data.rpcPo.getClientName());
			LogicManager.logicThread.sendToServer(client.getType2Name(Constant.TYPE_SCENE), 
					ProtoUtil.packData(rpc), data.rpcPo.getClientName());
		} catch (InvalidProtocolBufferException e) {
			log.error(e);
		}
	}
	public void onChatReq(DataPackage data){
		ChatReq req;
		try {
			req = ChatReq.parseFrom(data.rpcPo.getAnyPo());
			
			ChatRep.Builder rep = ChatRep.newBuilder();
			rep.setFrom(data.termianl.getSocketName()); // 这里不能用data.rpcPo.getClientName(); client未给其赋值
			rep.setChannel(req.getChannel());
			rep.setMsg(req.getMsg());
			
			RpcPo.Builder po = ProtoUtil.packData(rep);
			
			for(int i=0; i<req.getTargetCount(); i++){
				LogicManager.logicThread.sendToClient(req.getTarget(i), po, null);
			}
		} catch (InvalidProtocolBufferException e) {
			log.error(e);
		}
	}
	// client连接到gateway
	public void onConnectReq(DataPackage data){
		try {
			ConnectReq req = ConnectReq.parseFrom(data.rpcPo.getAnyPo());
			String userKey = RedisKey.UserKey(req.getUsername());

			long cid = RedisHandler.parseLong(NetManager.redis.hget(userKey, RedisKey.Clientid));
			if(cid == req.getCid()){
				NetManager.setClient(req.getUsername(), data.termianl); // 切换为非临时连接
				
				String scene = NetManager.redis.hget(userKey, RedisKey.Scene);
				if(scene == null || NetManager.getServer(scene) == null 
						|| NetManager.getServer(scene).getSocketType().equals(Constant.TYPE_SCENE)){
					// 分配一个scene
					SocketThread server = allocate();
					data.termianl.setType2Name(Constant.TYPE_SCENE, server.getSocketName()); 
					EnterSceneRpc.Builder crq = EnterSceneRpc.newBuilder();
					crq.setUsername(req.getUsername());
					crq.setCid(req.getCid());
					LogicManager.logicThread.sendToServer(server.getSocketName(), ProtoUtil.packData(crq), req.getUsername());
				}else{
					// 成功
					data.termianl.setType2Name(Constant.TYPE_SCENE, scene); // 注册转发列表
					ConnectReq.Builder crq = ConnectReq.newBuilder();
					crq.setUsername(req.getUsername());
					crq.setCid(req.getCid());
					LogicManager.logicThread.sendToServer(scene, ProtoUtil.packData(crq), req.getUsername());
				}
				
				// 指向一个db
				SocketThread dbs = ServerHelper.getDatabase();
				if(dbs != null){
					data.termianl.setType2Name(Constant.TYPE_DATABASE, dbs.getSocketName());
				}
				
				// 发送到world
				SocketThread world = ServerHelper.getWorld();
				if(world != null){
					data.termianl.setType2Name(Constant.TYPE_WORLD, world.getSocketName());
					EnterWorldRpc.Builder enterWorldRpc = EnterWorldRpc.newBuilder();
					LogicManager.logicThread.sendToServer(world.getSocketName(), ProtoUtil.packData(enterWorldRpc), req.getUsername());
				}
			}else{
				log.error("不统一的cid");
			}
		} catch (InvalidProtocolBufferException e) {
			log.error(e);
		}
	}
	
	// 设置gateway转发表
	public void onSetServerRpc(DataPackage data){
		try {
			SetServerRpc po = SetServerRpc.parseFrom(data.rpcPo.getAnyPo());
			SocketTerminal client = NetManager.getClient(po.getUsername());
			if(client != null){
				client.setType2Name(po.getType(), po.getName());
			}
		} catch (InvalidProtocolBufferException e) {
			log.error(e);
		}
	}
	
	private SocketThread allocate(){
		// 分配一个scene
		HashMap<String, SocketThread> hash = NetManager.getServersByType(Constant.TYPE_SCENE);
		for(SocketThread server : hash.values()){
			return server;
		}
		return null;
	}
	public void clientDisconnect(SocketTerminal client){
		log.info("client disconnect " + client.getSocketName());
		ClientDisconnectRpc.Builder clientDisRpc = ClientDisconnectRpc.newBuilder();
		clientDisRpc.setUsername(client.getSocketName());
		log.info(client.getSocketName());
		NetManager.redis.hset(RedisKey.UserKey(client.getSocketName()), RedisKey.LogoutTime, System.currentTimeMillis() + "");
		if(client.getType2Name(Constant.TYPE_SCENE) != null){
			LogicManager.logicThread.sendToServer(client.getType2Name(Constant.TYPE_SCENE), ProtoUtil.packData(clientDisRpc), client.getSocketName());
		}
		if(client.getType2Name(Constant.TYPE_BATTLE) != null){
			LogicManager.logicThread.sendToServer(client.getType2Name(Constant.TYPE_BATTLE), ProtoUtil.packData(clientDisRpc), client.getSocketName());
		}
		
		// 发送到world
		SocketThread world = ServerHelper.getWorld();
		if(world != null){
			ExitWorldRpc.Builder exitWorldRpc = ExitWorldRpc.newBuilder();
			LogicManager.logicThread.sendToServer(world.getSocketName(), ProtoUtil.packData(exitWorldRpc), client.getSocketName());
		}
	}
	public void onExitBattleRep(DataPackage data){
		SocketTerminal client = NetManager.getClient(data.rpcPo.getClientName());
		if(client != null){
			client.setType2Name(Constant.TYPE_BATTLE, null);
		}
	}
	public void onJoinBattleRep(DataPackage data){
		SocketTerminal client = NetManager.getClient(data.rpcPo.getClientName());
		if(client != null){
			client.setType2Name(Constant.TYPE_BATTLE, data.termianl.getSocketName());
		}
	}
	// 切换场景
	public void onChangeSceneRep(DataPackage data){
		SocketTerminal client = NetManager.getClient(data.rpcPo.getClientName());
		if(client != null){
			try {
				ChangeSceneRep rep = ChangeSceneRep.parseFrom(data.rpcPo.getAnyPo());
				if(rep.getResult() == ChangeSceneResultEnm.OK_CHANGESCENERESULT){
					client.setType2Name(Constant.TYPE_SCENE, rep.getScene());
				}
			} catch (InvalidProtocolBufferException e) {
				log.error(e);
			}
		}
	}
}
