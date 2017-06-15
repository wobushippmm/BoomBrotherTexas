package logic.common;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.google.protobuf.GeneratedMessageV3.Builder;
import com.google.protobuf.InvalidProtocolBufferException;

import protocol.Protocol;
import protocol.Protocol.AddressDat;
import protocol.Protocol.QuitRpc;
import protocol.Protocol.RpcPo;
import protocol.Protocol.ServerListRpc;
import logic.sos.SOSLogic;
import core.config.Constant;
import core.log.LoggerHelper;
import core.net.ClientThread;
import core.net.ConnectThread;
import core.net.DataPackage;
import core.net.NetManager;
import core.net.SocketTerminal;
import core.net.SocketThread;
import core.udp.UdpDataPackage;

public class LogicThread extends Thread{
	private Hashtable<String, Object> rpcDic = new Hashtable<String, Object>();
	
	public void setRpc(Class<?> rpc, Object obj){
		rpcDic.put(rpc.getSimpleName(), obj);
	}
	
	public Object getRpc(String rpc){
		return rpcDic.get(rpc);
	}
	
	public volatile boolean quit = false;
	protected Logger log = LoggerHelper.getLogger();
	
	public void run(){
		// 加入底层网络接口
		setRpc(ServerListRpc.class, this);
		setRpc(QuitRpc.class, this);
		
		
		startLogic(); // 开始逻辑
		while(!quit){
			int busy = 0;
			busy += dealTcpRpc();
			busy += dealUdpRpc();
			busy += loopLogic();
			// 非忙碌sleep
			if(busy == 0){
				try {
					sleep(1);
				} catch (InterruptedException e) {
					log.error(e);
				}
			}
		}
		
		endLogic(); // 结束逻辑
		log.info("Quit LogicThread");
	}
	
	// 逻辑开始  [funcname, obj]
	public ArrayList<Object[]> startLogicFuncs = new ArrayList<Object[]>();
	public void startLogic(){
		runFuncs1(startLogicFuncs);
	}
	// 逻辑结束
	public ArrayList<Object[]> endLogicFuncs = new ArrayList<Object[]>();
	public void endLogic(){
		runFuncs1(endLogicFuncs);
	}
	// 处理tcp调用
	public int dealTcpRpc(){
		synchronized (NetManager.dataList) {
			if(NetManager.dataList.size() > 0){
				DataPackage data = NetManager.dataList.remove(0);
				// 解析数据
				Object obj = rpcDic.get(data.rpcPo.getRpc());
				if(obj != null){
					try {
						// 必须是 public void funcname(DataPackage) 格式
						obj.getClass().getMethod("on" + data.rpcPo.getRpc(), DataPackage.class).invoke(obj, data); // 执行rpc
					} catch (SecurityException | NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						log.error(data.rpcPo.getRpc(), e);
					}
				}
				return 1;
			}
		}
		return 0;
	}
	// 处理udp调用
	public int dealUdpRpc(){
		if(NetManager.udpThread != null){
			synchronized (NetManager.udpDataList) {
				if(NetManager.udpDataList.size() > 0){
					UdpDataPackage packet = NetManager.udpDataList.remove(0);
					Object obj = rpcDic.get(packet.rpcPo.getRpc());
					try {
						// public void funcname(udpDataPacket) 格式
						obj.getClass().getMethod("on" + packet.rpcPo.getRpc(), UdpDataPackage.class).invoke(obj, packet);
					} catch (IllegalAccessException
							| IllegalArgumentException
							| InvocationTargetException
							| NoSuchMethodException | SecurityException e) {
						log.error(packet.rpcPo.getRpc(), e);
					}
					return 1;
				}
			}
		}
		return 0;
	}
	// 逻辑循环
	public ArrayList<Object[]> loopLogicFuncs = new ArrayList<Object[]>();
	public int loopLogic(){
		return runFuncs2(loopLogicFuncs);
	}
	// 关闭逻辑,后面到endLogic()
	public ArrayList<Object[]> shutdownFuncs = new ArrayList<Object[]>();
	public synchronized void shutdown(){
		runFuncs1(shutdownFuncs);
		
		NetManager.serverThread.quit = true;
		LogicManager.logicThread.quit = true;
		NetManager.quit = true;
		LogicManager.consoleThread.setQuit();
		if(NetManager.udpThread != null){
			NetManager.udpThread.quit = true;
		}
		if(NetManager.websocketThread != null){
			NetManager.websocketThread.quit();
		}
	}
	// 被动连接
	public ArrayList<Object[]> serverConnectFuncs = new ArrayList<Object[]>();
	public synchronized void serverConnect(ClientThread client){
		runFuncs3(serverConnectFuncs, client);
	}
	// 被动连接退出
	public ArrayList<Object[]> serverDisconnectFuncs = new ArrayList<Object[]>();
	public synchronized void serverDisconnect(ClientThread client){
		runFuncs3(serverDisconnectFuncs, client);
	}
	public ArrayList<Object[]> clientConnectFuncs = new ArrayList<Object[]>();
	public void clientConnect(SocketTerminal client){
		runFuncs4(clientConnectFuncs, client);
	}
	public ArrayList<Object[]> clientDisconnectFuncs = new ArrayList<Object[]>();
	public void clientDisconnect(SocketTerminal client){
		runFuncs4(clientDisconnectFuncs, client);
	}
	
	private void runFuncs1(ArrayList<Object[]> funcs){
		for(Object[] arr : funcs){
			String func = (String)arr[0];
			Object obj = arr[1];
			try {
				// void function();
				obj.getClass().getMethod(func).invoke(obj);
			} catch (IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException
					| SecurityException e) {
				log.error(e);
			}
		}
	}
	private int runFuncs2(ArrayList<Object[]> funcs){
		int busy = 0;
		for(Object[] arr : funcs){
			String func = (String)arr[0];
			Object obj = arr[1];
			try {
				// 必须要有返回值 int function()
				busy += (int)obj.getClass().getMethod(func).invoke(obj);
			} catch (IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException
					| SecurityException | NullPointerException e) {
				log.error("", e);
			}
		}
		return busy;
	}
	private void runFuncs3(ArrayList<Object[]> funcs, ClientThread ct){
		for(Object[] arr : funcs){
			String func = (String)arr[0];
			Object obj = arr[1];
			try {
				// void function(ClientThread)
				obj.getClass().getMethod(func, ClientThread.class).invoke(obj, ct);
			} catch (IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException
					| SecurityException e) {
				log.error(e);
			}
		}
	}
	private void runFuncs4(ArrayList<Object[]> funcs, SocketTerminal st){
		for(Object[] arr : funcs){
			String func = (String)arr[0];
			Object obj = arr[1];
			try {
				// void function(SocketTerminal)
				obj.getClass().getMethod(func, SocketTerminal.class).invoke(obj, st);
			} catch (IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException
					| SecurityException e) {
//				e.printStackTrace();
				log.error(e);
			}
		}
	}
	// 必须是 public void funcname(DataPackage) 格式
	public void onServerListRpc(DataPackage data){
		HashMap<String, Boolean> dic = new HashMap<String, Boolean>();
		synchronized (NetManager.targets) {
			for(int i=0; i<data.rpcPo.getServerListRpc().getServerListCount(); i++){
				AddressDat address = data.rpcPo.getServerListRpc().getServerList(i);
				dic.put(address.getName(), true);
				if(NetManager.targets.get(address.getName()) == null && !address.getName().equals(NetManager.name)){
					// 添加一个主动连接
					ConnectThread connect = new ConnectThread(address.getAddress(), address.getPort());
					connect.setSocketName(address.getName());
					connect.setSocketType(address.getType());
					connect.setWebsocketPort(address.getWebsocketPort());
					connect.setUdpPort(address.getUdpPort());
					NetManager.targets.put(address.getName(), connect);
					connect.start();
				}
			}
		}
		
		synchronized (NetManager.targets) {
			Iterator<ConnectThread> iter = NetManager.targets.values().iterator();
			while(iter.hasNext()){
				ConnectThread connect = iter.next();
				if(!dic.containsKey(connect.getSocketName())){
					iter.remove();
					connect.quit = true;
				}
			}
		}
	}
	
	public void onQuitRpc(DataPackage data){
		System.out.println("服务器已关闭，按回车退出控制台");
		shutdown();
	}
	
	// 逻辑层的发送接口,需要通过gateway转发的要重载
	public void sendToClient(String toname, RpcPo.Builder builder, SocketThread gateway){
		NetManager.sendToClient(toname, builder);
	}
	public void sendToServer(String toname, RpcPo.Builder builder, String username){
		NetManager.sendToServer(toname, builder);
	}
}
