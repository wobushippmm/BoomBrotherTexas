package core.net;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.google.protobuf.GeneratedMessageV3.Builder;

import protocol.ProtoUtil;
import protocol.Protocol;
import protocol.Protocol.DisconnectCauseEnm;
import protocol.Protocol.DisconnectRep;
import protocol.Protocol.HeartRpc;
import protocol.Protocol.HelloRpc;
import protocol.Protocol.RpcPo;
import redis.clients.jedis.Jedis;
import core.config.Constant;
import core.log.LoggerHelper;
import core.udp.UdpDataPackage;
import core.udp.UdpThread;
import core.websocket.MyWebSocketServer;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import logic.common.LogicManager;
import logic.common.LogicThread;

public class NetManager {
	private static Logger getLog(){
		return LoggerHelper.getLogger();
	}
	public static volatile boolean quit = false; // 退出全部socketthread
	
	public static String serverType = ""; // 当前服务器类型
	public static int port = 0; // 当前服务器port
	public static String name = ""; // 服务器唯一id
	public static String sosAddr = ""; // sos地址
	public static int sosPort = 0; // sos port
	public static boolean accClient = false; // 是否接受客户端连接
	
	public static int udpPort = 0; // udp的端口
	public static int udpPacketCounter = 0; // udp包计数
	public static UdpThread udpThread = null; // 监听udp请求
	
	public static int redisPort = 0; // redis port
	public static String redisAddr = ""; // redis address
	public static String redisPass = ""; // redis 密码
	public static Jedis redis = null; // jedis，可以做个连接池
	
	public static int websocketPort = 0; // websocket port
	public static MyWebSocketServer websocketThread = null;
	
	public static String mysqlAddr = "";
	public static int mysqlPort = 0; // mysql port
	public static String mysqlDB = "";
	public static String mysqlUser = "";
	public static String mysqlPass = "";
	public static java.sql.Connection mysqlConn = null;
	public static Statement mysqlStmt = null;
	
	public static ServerThread serverThread = null; // 监听连接
	public static ConnectThread sosConnect = null; // 连接sos
	public static Hashtable<String, ClientThread> fromers = new Hashtable<String, ClientThread>(); // 被动连接
	public static Hashtable<String, ConnectThread> targets = new Hashtable<String, ConnectThread>(); // 主动连接
	
	public static Hashtable<String, SocketTerminal> clients = new Hashtable<String, SocketTerminal>(); // 客户端
	
	public static Vector<ClientThread> socketList = new Vector<ClientThread>(); // 临时连接
	
	public static Vector<DataPackage> dataList = new Vector<DataPackage>(); // 接收到的数据
	public static Vector<UdpDataPackage> udpDataList = new Vector<UdpDataPackage>(); // udp数据包

	// 发送身份
	public static void sayHello(SocketThread client){
		RpcPo.Builder rpcBuilder = RpcPo.newBuilder();
		HelloRpc.Builder helloBuilder = HelloRpc.newBuilder();
		rpcBuilder.setRpc(HelloRpc.class.getSimpleName());
		helloBuilder.setName(NetManager.name);
		helloBuilder.setPort(NetManager.port);
		helloBuilder.setType(NetManager.serverType);
		helloBuilder.setWebsocketPort(NetManager.websocketPort);
		helloBuilder.setUdpPort(NetManager.udpPort);
		rpcBuilder.setHelloRpc(helloBuilder); // 必须先把子builder赋值...
		client.send(rpcBuilder);
	}
	// 收到服务端身份
	public static void hello(DataPackage data, SocketThread client){
		socketList.remove(client);
		((ClientThread)client).fromerType = data.rpcPo.getHelloRpc().getType();
		((ClientThread)client).fromerPort = data.rpcPo.getHelloRpc().getPort();
		((ClientThread)client).fromerName = data.rpcPo.getHelloRpc().getName();
		client.setWebsocketPort(data.rpcPo.getHelloRpc().getWebsocketPort());
		client.setUdpPort(data.rpcPo.getHelloRpc().getUdpPort());
		fromers.put(data.rpcPo.getHelloRpc().getName(), (ClientThread) client);
		getLog().info("Receive hello from " + data.rpcPo.getHelloRpc().getType() 
				+ " " + data.rpcPo.getHelloRpc().getPort() + " " + data.rpcPo.getHelloRpc().getName());
		
		// 服务器连接，逻辑可能还未启动
		int count = 0;
		while(LogicManager.logicThread == null && ++count < 1000){
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				getLog().error(e);
			}
		}
		
		LogicManager.logicThread.serverConnect((ClientThread)client);
	}
	// 客户端登录
	// 在调用之前还不能使用sendToClient
	public static void setClient(String username, SocketTerminal client){
		if(client.socketThread != null){
			socketList.remove(client.socketThread);
		}
		client.setSocketType(Constant.TYPE_CLIENT);
		client.setSocketName(username);
		if(clients.containsKey(username)){ // 已有连接要退出
			clients.get(username).callDisconnectFunc = false; // 不会掉断线
			
			// 其他地方登陆
			DisconnectRep.Builder discRep = DisconnectRep.newBuilder();
			discRep.setCause(DisconnectCauseEnm.LOGIN_OTHER_CLIENT);
			clients.get(username).send(ProtoUtil.packData(discRep));
			
			clients.get(username).quit();
		}
		clients.put(username, client);
		LogicManager.logicThread.clientConnect(client);
	}
	public static SocketTerminal getClient(String username){
		return clients.get(username);
	}
	// 发送心跳
	public static void heartBeat(SocketThread client){
		Protocol.RpcPo.Builder rpcBuilder = Protocol.RpcPo.newBuilder();
		rpcBuilder.setRpc(HeartRpc.class.getSimpleName());
		rpcBuilder.setHeartRpc(HeartRpc.newBuilder());
		client.send(rpcBuilder);
	}
	// 收到心跳
	public static void heart(DataPackage data, SocketThread client){
		((ClientThread)client).heartTime = System.currentTimeMillis();
	}
	// 通过名字获得服务器连接
	public static SocketThread getServer(String name){
		synchronized (fromers) {
			if(fromers.containsKey(name)){
				return fromers.get(name);
			}
		}
		
		synchronized (targets) {
			if(targets.containsKey(name)){
				return targets.get(name);
			}
		}
		
		return null;
	}
	public static HashMap<String, SocketThread> getServersByType(String type){
		HashMap<String, SocketThread> dic = new HashMap<String, SocketThread>();
		
		synchronized (fromers) {
			Iterator<String> iter = fromers.keySet().iterator();
			while(iter.hasNext()){
				String name = iter.next();
				if(fromers.get(name).fromerType.equals(type)){
					dic.put(name, fromers.get(name));
				}
			}
		}
		synchronized (targets) {
			Iterator<String> iter = targets.keySet().iterator();
			while(iter.hasNext()){
				String name = iter.next();
				if(!dic.containsKey(name) && targets.get(name).serverType.equals(type)){
					dic.put(name, targets.get(name));
				}
			}
		}
		
		return dic;
	}
	public static HashMap<String, SocketThread> getServers(){
		HashMap<String, SocketThread> dic = new HashMap<String, SocketThread>();
		
		synchronized (fromers) {
			Iterator<String> iter = fromers.keySet().iterator();
			while(iter.hasNext()){
				String name = iter.next();
				dic.put(name, fromers.get(name));
			}
		}
		
		
		synchronized (targets) {
			Iterator<String> iter = targets.keySet().iterator();
			while(iter.hasNext()){
				String name = iter.next();
				if(!dic.containsKey(name)){
					dic.put(name, targets.get(name));
				}
			}
		}
		
		return dic;
	}
	
	public static void broadcastToServer(Protocol.RpcPo.Builder msg){
		HashMap<String, Boolean> dic = new HashMap<String, Boolean>();

		synchronized (fromers) {
			Iterator<String> iter = fromers.keySet().iterator(); // 必须通过迭代器操作，否则异常，next()调一次往后指针移动一个
			while(iter.hasNext()){
				String name = iter.next();
				dic.put(name, true);
				fromers.get(name).send(msg);
			}
		}
		

		synchronized (targets) {
			Iterator<String> iter = targets.keySet().iterator();
			while(iter.hasNext()){
				String name = iter.next();
				if(!dic.containsKey(name)){
					targets.get(name).send(msg);
				}
			}
		}
		
	}
	public static void broadcastToClient(byte[] msg){
		synchronized (clients) {
			Iterator<SocketTerminal> iter = clients.values().iterator();
			while(iter.hasNext()){
				SocketTerminal client = iter.next();
				client.send(msg);
			}
		}
	}
	public static void broadcastToClient(RpcPo.Builder msg){
		synchronized (clients) {
			Iterator<SocketTerminal> iter = clients.values().iterator();
			while(iter.hasNext()){
				SocketTerminal client = iter.next();
				client.send(msg);
			}
		}
	}
	
	public static void sendToServer(String name, byte[] msg){
		SocketThread server = getServer(name);
		if(server != null){
			server.send(msg);
		}
	}
	public static void sendToServer(String name, RpcPo.Builder msg){
		SocketThread server = getServer(name);
		if(server != null){
			server.send(msg);
		}
	}
	// 有客户端直接连接的服可以调用
	public static void sendToClient(String name, byte[] msg){
		synchronized (clients) {
			// 已经连接上的客户端
			if(clients.containsKey(name)){
				clients.get(name).send(msg);
			}
		}
	}
	public static void sendToClient(String name, RpcPo.Builder msg){
		msg.setClientName(name);
		synchronized (clients) {
			// 已经连接上的客户端
			if(clients.containsKey(name)){
				clients.get(name).send(msg);
			}
		}
	}
	
	private static byte[] builderToBytes(RpcPo.Builder builder){
		byte[] body = builder.build().toByteArray();
		if(body.length > Constant.SIZE_OF_UDP_CACHE){
			getLog().warn("Send data size > udp cache " + builder.toString());
			return null;
		}
		getLog().info("Send udp data " + builder.toString());
		
		udpPacketCounter++;
		byte[] data = new byte[body.length + Constant.LENGTH_OF_NET_PACKAGE_HEADER];
		data[0] = (byte) ((udpPacketCounter >> 24) & 0x000000ff);
		data[1] = (byte) ((udpPacketCounter >> 16) & 0x000000ff);
		data[2] = (byte) ((udpPacketCounter >> 8) & 0x000000ff);
		data[3] = (byte) ((udpPacketCounter >> 0) & 0x000000ff);
		System.arraycopy(body, 0, data, 4, body.length);
		
		return data;
	}
	private static DatagramPacket packetData(String addr, int port, byte[] data){
		
		DatagramPacket packet = new DatagramPacket(data, data.length);
		try {
			packet.setAddress(InetAddress.getByName(addr));
		} catch (UnknownHostException e) {
			getLog().error(e);
			return null;
		}
		packet.setPort(port);
		return packet;
	}
	// udp方式发送
	public static void udpSend(String addr, int port, RpcPo.Builder msg){
		if(NetManager.udpThread != null){
			byte[] data = builderToBytes(msg);
			if(data != null){
				DatagramPacket packet = packetData(addr, port, data);
				if(packet != null){
					NetManager.udpThread.send(packet);
				}
			}
		}
	}
	// udp方式广播
	public static void udpBroadcast(String[] addrs, int[] ports, RpcPo.Builder msg){
		if(NetManager.udpThread != null){
			if(addrs.length != ports.length)return;
			byte[] data = builderToBytes(msg);
			if(data != null){
				for(int i=0; i<addrs.length; i++){
					DatagramPacket packet = packetData(addrs[i], ports[i], data);
					if(packet != null){
						NetManager.udpThread.send(packet);
					}
				}
			}
		}
	}
}
