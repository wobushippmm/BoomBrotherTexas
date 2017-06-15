package core.net;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.jar.Attributes.Name;

import core.config.Constant;
import logic.common.LogicManager;
import logic.common.LogicThread;

// 被动连接
public class ClientThread extends SocketThread{
	private long timeout = 0;
	protected String fromerType = ""; // 连接者类型
	protected int fromerPort = 0; // 这个是连接者接受连接的port
	protected int fromerWebsocketPort = 0;
	protected int fromerUdpPort = 0;
	protected String fromerName = ""; // 连接者唯一id
	public long heartTime = Long.MAX_VALUE; // 上次收到心跳时间
	
	public ClientThread(Socket sock){
		socket = sock;
		timeout = System.currentTimeMillis();
		try {
			out = new DataOutputStream(socket.getOutputStream());
			in = new DataInputStream(socket.getInputStream());
		} catch (IOException e) {
			log.error(e);
		}
	}
	
	public void run(){
		while(!quit && !NetManager.quit){
			if(socket.isConnected()){
				int busy = 0;
				busy += receiveData(); // 接收数据
				try {
					long now = System.currentTimeMillis();
					if(now - heartTime > Constant.TIME_OUT_OF_HEART){
						log.info("Heart beat time out !");
						quit = true;
					}
					if(fromerType.length() == 0){ // 如果未亮明身份
						// 服务器亮身份用 hello ，客户端亮身份看具体逻辑
						if(now - timeout > Constant.TIME_OUT_OF_SAY_HELLO){
							// 等待亮明身份超时，可能是服务器连接超时，可能是客户端连接超时
							log.info("Wait for say hello time out !");
							quit = true;
						}
						sleep(10);
					}else{
						busy += sendData(); // 发送数据
					}
					if(busy == 0){ // 空闲状态，sleep
						sleep(10);
					}
				} catch (InterruptedException e) {
					log.error(e);
				} 
			}else{
				quit = true;
			}
		}
		while(sendDataList.size() > 0){
			sendData(); // 发送完已进入发送列表的数据
		}
		
		// 退出连接
		try {
			if(fromerName.length() > 0){
				if(NetManager.fromers.containsKey(fromerName)){ // 服务端断开连接
					NetManager.fromers.remove(fromerName);
					LogicManager.logicThread.serverDisconnect(this);
				}else if(NetManager.clients.containsKey(fromerName)){ // 客户端断开连接
					NetManager.clients.remove(fromerName);
					if(terminal.callDisconnectFunc){
						LogicManager.logicThread.clientDisconnect(this.getTerminal());
					}
				}
			}else{
				NetManager.socketList.remove(this);
			}
			socket.close();
			in.close();
			out.close();
			log.info("Quit ClientThread " + getSocketName());
		} catch (IOException e) {
			log.error(e);
		}
	}

	
	public String socketInfo(){
		return "Fromer " + fromerType + " " + fromerName;
	}
	
	public String getSocketAddress(){
		if(socket.getInetAddress() != null){
			InetAddress ip = socket.getInetAddress();
			String str = ip.getHostAddress();
			// 如果是127.0.0.1或localhost
			if(ip.isLoopbackAddress() || str.indexOf(":") > -1){
				return getServerIp();
			}
			return str;
		}
		return "";
	}
	
	public String getSocketType(){
		return fromerType;
	}
	public void setSocketType(String type){
		fromerType = type;
	}
	public String getSocketName(){
		return fromerName;
	}
	public void setSocketName(String name){
		fromerName = name;
	}
	public int getSocketPort(){
		return fromerPort;
	}
	public void setSocketPort(int port){
		fromerPort = port;
	}
	public int getWebsocketPort(){
		return fromerWebsocketPort;
	}
	public void setWebsocketPort(int port){
		fromerWebsocketPort = port;
	}
	public int getUdpPort(){
		return fromerUdpPort;
	}
	public void setUdpPort(int port){
		fromerUdpPort = port;
	}
	public void setHeartTime(long time){
		heartTime = time;
	}
}
