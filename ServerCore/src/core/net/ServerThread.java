package core.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.Logger;

import core.config.Constant;
import core.log.LoggerHelper;

public class ServerThread extends Thread{
	protected ServerSocket serverSocket = null;
	protected Logger log = LoggerHelper.getLogger();
	public volatile boolean quit = false;
	public ServerThread(int port){
		try {
			if(NetManager.accClient){
				serverSocket = new ServerSocket(port, Constant.MAX_LINK_OF_CLIENT); // 最大接收连接数
			}else{
				serverSocket = new ServerSocket(port, Constant.MAX_LINK_OF_SERVER); // 最大接收连接数
			}
			serverSocket.setSoTimeout(Constant.TIME_OUT_OF_SERVER_SOCKET_WAIT); // 接收超时，使线程非阻塞
		} catch (IOException e) {
			log.error(e);
		}
	}
	
	public void run(){
		log.info("Start Server on port " + serverSocket.getLocalPort() + "...");
		while(!quit && !NetManager.quit){
			Socket socket;
			try {
				socket = serverSocket.accept(); //阻塞状态
				log.info("Client connect " + socket.getRemoteSocketAddress().toString());
				ClientThread client = new ClientThread(socket);
				NetManager.socketList.add(client);
				client.start(); // 开始一个socket线程
			} catch (IOException e) {
				try {
					// 接收超时
					sleep(100);
				} catch (InterruptedException e1) {
					log.error(e1);
				}
			}
		}
		
		try {
			serverSocket.close();
			log.info("Quit ServerThread");
		} catch (IOException e) {
			log.error(e);;
		}
		
	}
}
