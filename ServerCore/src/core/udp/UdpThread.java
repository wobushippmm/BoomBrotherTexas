package core.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.google.protobuf.GeneratedMessageV3.Builder;

import core.config.Constant;
import core.log.LoggerHelper;
import core.net.NetManager;

public class UdpThread extends Thread {
	private Logger log = LoggerHelper.getLogger();
	private int packetCounter = 0; // 当越界之后会出问题
	
	public int port = 0;
	protected DatagramSocket socket = null;
	public boolean quit = false;
	protected Vector<DatagramPacket> sendDataList = new Vector<DatagramPacket>();
	
	public UdpThread(int port){
		this.port = port;
		try {
			socket = new DatagramSocket(port);
			socket.setSoTimeout(Constant.TIME_OUT_OF_UDP_RECEIVE); // 防止receive阻塞
		} catch (SocketException e) {
			log.error(e);
		}
	}
	public void run(){
		while(!quit && !NetManager.quit){
			byte[] buf = new byte[Constant.SIZE_OF_UDP_CACHE];
			DatagramPacket recvPacket = new DatagramPacket(buf, buf.length);
			try {
				socket.receive(recvPacket);
				synchronized (NetManager.udpDataList) {
					NetManager.udpDataList.add(new UdpDataPackage(recvPacket));	
				}
				sendData();
				// 收到数据说明忙碌，不能sleep
			} catch (IOException e) {
				if(sendData() == 0){ // 空闲状态
					try {
						sleep(5); // 超时
					} catch (InterruptedException e1) {
						log.error(e1);
					}
				}
			}
		}
		while(sendDataList.size() > 0){
			sendData();
		}
		socket.close();
		log.info("Quit UdpThread");
	}
	private int sendData(){
		synchronized (sendDataList) {
			if(sendDataList.size() > 0){ // 发送数据
				try {
					socket.send(sendDataList.remove(0));
				} catch (IOException e) {
					log.error(e);
				}
				return 1;
			}
		}
		return 0;
	}
	
	public void send(DatagramPacket packet){
		synchronized (sendDataList) {
			sendDataList.add(packet);
		}
	}
}
