package core.net;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Vector;

import core.config.Constant;
import protocol.Protocol;

// 主动连接
public class ConnectThread extends SocketThread{
	protected String serverName = "";
	protected String serverAddr = "";
	protected int serverPort = 0;
	protected int serverWebsocketPort = 0;
	protected int serverUdpPort = 0;
	protected String serverType = ""; // 
	public boolean needSayHello = true; // 是否需要say hello
	protected boolean sayHello = true; // 是否要say hello
	private long heartTime = 0; // 上次心跳世间
	public int autoConnect = Integer.MAX_VALUE; // 自动连接次数 
	
	public ConnectThread(String addr, int port){
		serverAddr = addr;
		serverPort = port;
	}
	
	public void run(){
		while(!quit && !NetManager.quit){
			try {
				if(socket != null && socket.isConnected()){
					if(in == null){
						out = new DataOutputStream(socket.getOutputStream());
						in = new DataInputStream(socket.getInputStream());
					}
					if(needSayHello && sayHello){
						// 发送身份
						NetManager.sayHello(this);
						sayHello = false;
					}
					long now = System.currentTimeMillis();
					// 发送心跳
					if(now - heartTime > Constant.DELAY_OF_HEART){
						NetManager.heartBeat(this);
						heartTime = now;
					}
					int busy = 0;
					busy += receiveData(); // 接收数据
					busy += sendData(); // 发送数据
					if(busy == 0){
						sleep(10);
					}
				}else{
					sayHello = true;
					heartTime = System.currentTimeMillis();
					sendDataList.clear();
					if(socket != null){
						socket.close();
						sleep(Constant.DELAY_OF_RECONNECT); // 如果是断开连接，则要sleep一下
					}
					if(in != null){
						in.close();
						out.close();
						in = null;
						out = null;
					}
					
					if(autoConnect > 0){
						log.info("Try connect to " + serverAddr + " " + serverPort);
						autoConnect--;
						socket = new Socket(serverAddr, serverPort); // 重新连接目标
					}else{
						quit = true;
					}
				}
			} catch (IOException | InterruptedException e) {
				log.error(e);
			}	
		}
		while(sendDataList.size() > 0){
			sendData(); // 发送完已进入发送列表的数据
		}
		// 关闭连接
		try {
			if(socket != null){
				socket.close();
				in.close();
				out.close();
			}
			log.info("Quit ConnectThread " + getSocketName());
		} catch (IOException e) {
			log.error(e);
		}
	}
	
	// 发送异常
	protected void sendError(){
		// 直接判断为
		try {
			sayHello = true;
			heartTime = System.currentTimeMillis();
			sendDataList.clear();
			if(socket != null){
				socket.close();
			}
			if(in != null){
				in.close();
				out.close();
				in = null;
				out = null;
			}
		} catch (IOException e) {
			log.error(e);
		}
		socket = null; // 重新连接处理
	}
	public String socketInfo(){
		return "Target " + serverType + " "+ serverName;
	}
	public String getSocketType(){
		return serverType;
	}
	public void setSocketType(String type){
		serverType = type;
	}
	public String getSocketName(){
		return serverName;
	}
	public void setSocketName(String name){
		serverName = name;
	}
	public String getSocketAddress(){
		return serverAddr;
	}
	public void setSocketAddress(String addr){
		serverAddr = addr;
	}
	public int getSocketPort(){
		return serverPort;
	}
	public void setSocketPort(int port){
		serverPort = port;
	}
	public int getWebsocketPort(){
		return serverWebsocketPort;
	}
	public void setWebsocketPort(int port){
		serverWebsocketPort = port;
	}
	public int getUdpPort(){
		return serverUdpPort;
	}
	public void setUdpPort(int port){
		serverUdpPort = port;
	}
}
