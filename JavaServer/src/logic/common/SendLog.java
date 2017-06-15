package logic.common;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSON;
import com.google.protobuf.GeneratedMessageV3.Builder;

import core.config.Constant;
import core.net.NetManager;
import core.net.SocketThread;
import protocol.ProtoUtil;
import protocol.GameData.LogRpc;
import protocol.GameData.LogTypeEnm;
import protocol.GameData.SetGoldCauseEnm;

public class SendLog {
	public static void sendGMLog(String username, HttpServletRequest request){
		LogRpc.Builder logRpc = LogRpc.newBuilder();
		logRpc.setType(LogTypeEnm.GMCOMMAND_LOG);
		Map<String, String> args = getArgs(request);
		logRpc.setUsername(username);
		logRpc.setCmd(args.get("cmd"));
		logRpc.setArgs(JSON.toJSONString(args));
		logRpc.setGm(args.get("password"));
		sendToDatabase(logRpc);
	}
	
	public static void sendRechargeLog(String username, HttpServletRequest request){
		LogRpc.Builder logRpc = LogRpc.newBuilder();
		logRpc.setType(LogTypeEnm.RECHARGE_LOG);
		Map<String, String> args = getArgs(request);
		logRpc.setUsername(username);
		logRpc.setArgs(JSON.toJSONString(args));
		sendToDatabase(logRpc);
	}
	
	public static void sendGoldLog(String username, int gold, SetGoldCauseEnm cause){
		LogRpc.Builder logRpc = LogRpc.newBuilder();
		logRpc.setType(LogTypeEnm.ADD_GOLD_LOG);
		logRpc.setGold(gold);
		logRpc.setCause(cause);
		sendToDatabase(logRpc);
	}
	
	//////////////// 工具函数 /////////////////////
	public static Map<String, String> getArgs(HttpServletRequest request){
		HashMap<String, String> args = new HashMap<String, String>();
		Enumeration<String> names = request.getParameterNames();
		while(names.hasMoreElements()){
			String name = names.nextElement();
			args.put(name, request.getParameter(name));
		}
		return args;
	}
	public static void broadcastToScene(Builder<?> rpc){
		HashMap<String, SocketThread> scenes = NetManager.getServersByType(Constant.TYPE_SCENE);
		for(SocketThread scene : scenes.values()){
			LogicManager.logicThread.sendToServer(scene.getSocketName(), ProtoUtil.packData(rpc), "");
		}
	}
	public static void sendToDatabase(Builder<?> rpc){
		HashMap<String, SocketThread> servers = NetManager.getServersByType(Constant.TYPE_DATABASE);
		for(SocketThread server : servers.values()){
			server.send(ProtoUtil.packData(rpc));
			break;
		}
	}
}
