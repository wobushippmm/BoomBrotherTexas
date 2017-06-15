package logic.client;

import org.apache.log4j.Logger;

import com.google.protobuf.InvalidProtocolBufferException;

import protocol.GameData;
import protocol.TexasGameData.CallActionRep;
import protocol.GameData.ChangeSceneReq;
import protocol.GameData.ConnectReq;
import protocol.GameData.DailyLoginAwardRep;
import protocol.GameData.EnterSceneRep;
import protocol.TexasGameData.GetTableListRep;
import protocol.TexasGameData.GetTableListReq;
import protocol.GameData.HeartReq;
import protocol.GameData.JoinBattleRep;
import protocol.GameData.JoinBattleReq;
import protocol.GameData.LoginRep;
import protocol.GameData.LoginReq;
import protocol.TexasGameData.MatchTableRep;
import protocol.TexasGameData.MatchTableReq;
import protocol.TexasGameData.StartRoundRep;
import protocol.TexasGameData.TableInfoRep;
import protocol.ProtoUtil;
import protocol.Protocol.RpcPo;
import logic.common.LogicManager;
import core.config.Constant;
import core.log.LoggerHelper;
import core.net.ConnectThread;
import core.net.DataPackage;
import core.net.NetManager;

public class ClientHandler {
	public static ClientHandler instance = null;
	private String loginName = "Larry";
	private String gatewayName = "Gale";
	private Logger log = LoggerHelper.getLogger();
	
	private boolean isEnterScene = false;
	private long updateTime = 0;
	
	public ClientHandler(){
		instance = this;
		LogicManager.consoleThread.showCmd = true;
		
		LogicManager.logicThread.loopLogicFuncs.add(new Object[]{"onUpdate", this});
		
		LogicManager.consoleThread.setCmd("login", this);
		LogicManager.consoleThread.setCmd("battle", this);
		
		LogicManager.logicThread.setRpc(LoginRep.class, this);
		LogicManager.logicThread.setRpc(EnterSceneRep.class, this);
		LogicManager.logicThread.setRpc(JoinBattleRep.class, this);
		LogicManager.logicThread.setRpc(GetTableListRep.class, this);
		LogicManager.logicThread.setRpc(MatchTableRep.class, this);
		LogicManager.logicThread.setRpc(TableInfoRep.class, this);
		LogicManager.logicThread.setRpc(StartRoundRep.class, this);
		LogicManager.logicThread.setRpc(DailyLoginAwardRep.class, this);
		LogicManager.logicThread.setRpc(CallActionRep.class, this);
	}
	public int onUpdate(){
		if(!isEnterScene){
			return 0;
		}
		long now = System.currentTimeMillis();
		if(now - updateTime < 5000){ // 每一秒执行一次
			return 0;
		}
		updateTime = now;
		
		// 发送心跳
		HeartReq.Builder heartReq = HeartReq.newBuilder();
		log.info("send heart");
		LogicManager.logicThread.sendToServer(gatewayName, ProtoUtil.packData(heartReq), "");
		return 1;
	}
	public void battle(String[] args){
		JoinBattleReq.Builder req = JoinBattleReq.newBuilder();
		LogicManager.logicThread.sendToServer(gatewayName, ProtoUtil.packData(req), "");
	}
	// 登录命令
	public void login(String[] args){
		// 连接到login服
		ConnectThread connect = new ConnectThread("127.0.0.1", 5842);
		connect.setSocketName(loginName);
		connect.setSocketType(Constant.TYPE_LOGIN);
		connect.needSayHello = false;
		connect.autoConnect = 1;
		NetManager.targets.put(loginName, connect);
		connect.start();
		
		GameData.LoginReq.Builder reqBuilder = GameData.LoginReq.newBuilder();
		reqBuilder.setUsername(args[1]);
		if(args.length > 2){
			reqBuilder.setPassword(args[2]);
		}else{
			reqBuilder.setPassword("123456"); // 默认密码
		}
		
		LogicManager.logicThread.sendToServer(loginName, ProtoUtil.packData(reqBuilder), "");
	}
	// 登录返回
	public void onLoginRep(DataPackage data){
		try {
			LoginRep rep = LoginRep.parseFrom(data.rpcPo.getAnyPo().toByteArray());
			System.out.println("onLoginRep " + rep.toString());
			log.info(rep.toString());
			
			ConnectThread connect = new ConnectThread(rep.getGatewayHost(), rep.getGatewayPort());
			connect.setSocketName(gatewayName);
			connect.setSocketType(Constant.TYPE_GATEWAY);
			connect.needSayHello = false;
			connect.autoConnect = 1;
			NetManager.targets.put(gatewayName, connect);
			connect.start();

			ConnectReq.Builder builder = ConnectReq.newBuilder();
			builder.setUsername(data.rpcPo.getClientName());
			builder.setCid(rep.getCid());
			LogicManager.logicThread.sendToServer(gatewayName, ProtoUtil.packData(builder), "");
		} catch (InvalidProtocolBufferException e) {
			log.error(e);
		}
	}
	
	public void onEnterSceneRep(DataPackage data){
		EnterSceneRep rep;
		try {
			rep = EnterSceneRep.parseFrom(data.rpcPo.getAnyPo());
			System.out.println("onEnterSceneRep " + rep.toString());
			log.info(rep.toString());
			
			isEnterScene = true;
			
			// 进入战斗服
			JoinBattleReq.Builder req = JoinBattleReq.newBuilder();
			LogicManager.logicThread.sendToServer(gatewayName, ProtoUtil.packData(req), "");
		} catch (InvalidProtocolBufferException e) {
			log.error(e);
		}
	}
	public void onJoinBattleRep(DataPackage data){
		try {
			JoinBattleRep rep = JoinBattleRep.parseFrom(data.rpcPo.getAnyPo());
			System.out.println("onJoinBattleRep " + rep.toString());
			log.info(rep.toString());
			
			// 请求桌子列表
			GetTableListReq.Builder getTabelListReq = GetTableListReq.newBuilder();
			getTabelListReq.setStartIndex(0);
			LogicManager.logicThread.sendToServer(gatewayName, ProtoUtil.packData(getTabelListReq), "");
		} catch (InvalidProtocolBufferException e) {
			log.error(e);
		}
	}
	public void onGetTableListRep(DataPackage data){
		try {
			GetTableListRep rep = GetTableListRep.parseFrom(data.rpcPo.getAnyPo());
			System.out.println("onGetTableListRep " + rep.toString());
			log.info(rep.toString());
		
			// 参加匹配
			MatchTableReq.Builder req = MatchTableReq.newBuilder();
			LogicManager.logicThread.sendToServer(gatewayName, ProtoUtil.packData(req), "");
		} catch (InvalidProtocolBufferException e) {
			log.error(e);
		}
	}
	public void onMatchTableRep(DataPackage data){
		try {
			GetTableListRep rep = GetTableListRep.parseFrom(data.rpcPo.getAnyPo());
			System.out.println("onMatchTableRep " + rep.toString());
			log.info(rep.toString());
		
			
		} catch (InvalidProtocolBufferException e) {
			log.error(e);
		}
	}
	
	public void onTableInfoRep(DataPackage data){
		try {
			TableInfoRep rep = TableInfoRep.parseFrom(data.rpcPo.getAnyPo());
			System.out.println("onTableInfoRep " + rep.toString());
			log.info(rep.toString());
		
			
		} catch (InvalidProtocolBufferException e) {
			log.error(e);
		}
	}
	
	public void onStartRoundRep(DataPackage data){
		try {
			StartRoundRep rep = StartRoundRep.parseFrom(data.rpcPo.getAnyPo());
			System.out.println("onStartRoundRep " + rep.toString());
			log.info(rep.toString());
		
			
		} catch (InvalidProtocolBufferException e) {
			log.error(e);
		}
	}
	public void onDailyLoginAwardRep(DataPackage data){
		try {
			DailyLoginAwardRep rep = DailyLoginAwardRep.parseFrom(data.rpcPo.getAnyPo());
			System.out.println("onDailyLoginAwardRep " + rep.toString());
			log.info(rep.toString());
		
			
		} catch (InvalidProtocolBufferException e) {
			log.error(e);
		}
	}
	public void onCallActionRep(DataPackage data){
		try {
			CallActionRep rep = CallActionRep.parseFrom(data.rpcPo.getAnyPo());
			System.out.println("onCallActionRep " + rep.toString());
			log.info(rep.toString());
		
			
		} catch (InvalidProtocolBufferException e) {
			log.error(e);
		}
	}
}
