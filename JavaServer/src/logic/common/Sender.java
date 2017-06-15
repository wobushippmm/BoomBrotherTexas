package logic.common;

import logic.common.data.DataManager;
import core.net.NetManager;
import core.net.SocketThread;
import protocol.Protocol.RpcPo;

public class Sender {
	// 只能有UserData的才能使用
	// 目前有scene, battle
	public static void sendToClient(String username, RpcPo.Builder builder){
		SocketThread gateway = NetManager.getServer(DataManager.instance.getUser(username).gateway);
		LogicManager.logicThread.sendToClient(username, builder, gateway);
	}
	
	public static void sendToServer(String servername, RpcPo.Builder builder){
		LogicManager.logicThread.sendToServer(servername, builder, "");
	}
	public static void sendToServer(String servername, RpcPo.Builder builder, String username){
		LogicManager.logicThread.sendToServer(servername, builder, username);
	}
}
