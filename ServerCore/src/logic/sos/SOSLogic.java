package logic.sos;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import core.net.ClientThread;
import core.net.ConnectThread;
import core.net.NetManager;
import core.net.SocketThread;
import logic.common.LogicManager;
import logic.common.LogicThread;
import protocol.Protocol;
import protocol.Protocol.AddressDat;
import protocol.Protocol.QuitRpc;
import protocol.Protocol.ServerListRpc;

// 监控各服务器状态
public class SOSLogic extends LogicThread {
	
	public void startLogic(){
	}
	
	// 广播服务器列表
	private synchronized void broadcastServerList(){
		ServerListRpc.Builder listBuilder = ServerListRpc.newBuilder();
		synchronized (NetManager.fromers) {
			Iterator<ClientThread> iter = NetManager.fromers.values().iterator();
			while(iter.hasNext()){
				ClientThread socket = iter.next();
				AddressDat.Builder addrBuilder = AddressDat.newBuilder();
				addrBuilder.setAddress(((ClientThread)socket).getSocketAddress());
				addrBuilder.setPort(((ClientThread)socket).getSocketPort());
				addrBuilder.setName(((ClientThread)socket).getSocketName());
				addrBuilder.setType(((ClientThread)socket).getSocketType());
				addrBuilder.setWebsocketPort(((ClientThread)socket).getWebsocketPort());
				addrBuilder.setUdpPort(((ClientThread)socket).getUdpPort());
				listBuilder.addServerList(addrBuilder);
			}
		}
		
		Protocol.RpcPo.Builder rpcBuilder = Protocol.RpcPo.newBuilder();
		rpcBuilder.setRpc(ServerListRpc.class.getSimpleName());
		rpcBuilder.setServerListRpc(listBuilder);
		NetManager.broadcastToServer(rpcBuilder);
	}
	
	public synchronized void shutdown(){
		Protocol.RpcPo.Builder rpcBuilder = Protocol.RpcPo.newBuilder();
		rpcBuilder.setRpc(QuitRpc.class.getSimpleName());
		rpcBuilder.setQuitRpc(QuitRpc.newBuilder());
		NetManager.broadcastToServer(rpcBuilder);
		
		log.info("shutdown....................");
		LogicManager.consoleThread.setQuit();
		NetManager.serverThread.quit = true;
		LogicManager.logicThread.quit = true;
		NetManager.quit = true;
	}
	
	// 服务器连接到sos
	public synchronized void serverConnect(ClientThread client){
		broadcastServerList();
	}
	
	// 服务器失去连接
	public synchronized void serverDisconnect(ClientThread client){
		broadcastServerList();
	}
}
