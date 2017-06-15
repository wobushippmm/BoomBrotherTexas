package core.net;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.google.protobuf.GeneratedMessageV3.Builder;

import protocol.Protocol;
import protocol.Protocol.RpcPo;
import core.config.Constant;
import core.log.LoggerHelper;
import jdk.nashorn.internal.ir.Terminal;

// socket基类
public class SocketThread extends Thread{
	protected Socket socket = null;
	protected Logger log = LoggerHelper.getLogger(); 
	
	public volatile boolean quit = false;
	protected DataInputStream in = null;
	protected DataOutputStream out = null;
	protected Vector<byte[]> sendDataList = new Vector<byte[]>();
	
	protected SocketTerminal terminal = null;
	public void setTerminal(SocketTerminal value){
		terminal = value;
	}
	public SocketTerminal getTerminal(){
		if(terminal != null){
			return terminal;
		}
		return new SocketTerminal(this);
	}
	
	// 数据读取
	protected boolean readHead = true;
	protected int dataSize = 0;
	protected byte[] recData = null;
	protected int offset = 0; // 当前读取的偏移
	

	
	public SocketThread(){
	}
	
	protected int receiveData(){
		int busy = 0;
		try {
			if(readHead){
				if(in.available() >= Constant.LENGTH_OF_NET_PACKAGE_HEADER){ // 读取数据头
					byte[] header = new byte[Constant.LENGTH_OF_NET_PACKAGE_HEADER];
					in.readFully(header, 0, Constant.LENGTH_OF_NET_PACKAGE_HEADER);
//					log.info("Receive Data header " + header[0] +" " + header[1] +" "+ header[2] + " " +header[3], true);
					dataSize = ((header[0] & 0x000000ff) << 24) + // java只有有符号数，通过&0xff去掉符号
							((header[1] & 0x000000ff) << 16) + // 这里byte在&时转成了int类型
							((header[2] & 0x000000ff) << 8) +
							((header[3] & 0x000000ff) << 0); // 数据大小，包含数据头
					if(dataSize > Constant.MAX_SIZE_OF_NET_MESSAGE){
						// 数据尺寸过大，可能是受到攻击，粘包
						log.warn("Receive Data out of size ! ");
					}else if(dataSize <= 4){
						log.error("Receive Data length " + dataSize + " <= 4 ! " + socketInfo());
						socket.close(); // 数据异常，关闭socket
						return 0;
					}
					recData = new byte[dataSize];
					offset = Constant.LENGTH_OF_NET_PACKAGE_HEADER;
					System.arraycopy(header, 0, recData, 0, Constant.LENGTH_OF_NET_PACKAGE_HEADER); // 拼回去
					readHead = false;
					busy = 1;
				}
			}else{
				// 读取数据体
				int available = in.available();
				if(available >= dataSize - offset){ // 这边读完之后可能socket缓存中还有很多数据，应该立马再读取
					in.readFully(recData, offset, dataSize - offset);
					busy = 1;
					readHead = true;
					// 添加到数据列表
					DataPackage data = new DataPackage(recData, this.getTerminal());
					if(!data.rpcPo.getRpc().equals("HeartRpc") && !data.rpcPo.getRpc().equals("HeartReq")){
						log.info("receive " + recData.length + " bytes " + data.rpcPo.toString());
					}
					if(data.rpcPo.getRpc().equals("HelloRpc")){
						NetManager.hello(data, this); // 必须打了招呼才进入逻辑处理
					}else if(data.rpcPo.getRpc().equals("HeartRpc") // 服务端心跳
							|| data.rpcPo.getRpc().equals("HeartReq")){ // 客户端心跳
						NetManager.heart(data, this);
					}else{
						NetManager.dataList.add(data);
					}
				}else if(available > 0){ // 有就读，防止粘包
					in.readFully(recData, offset, available);
					offset += available;
					busy = 1;
				}
			}
		} catch (IOException e) {
			log.error(e);
		}
		return busy;
	}
	
	protected int sendData(){
		int busy = 0;
		try {
			synchronized (sendDataList) {
				if(sendDataList.size() > 0){
					out.write(sendDataList.remove(0));
					out.flush();
					busy = 1;
				}
			}
			
		} catch (IOException e) {
			log.error(e);
			sendError();
		}
		return busy;
	}
	
	// 发送时error, 可能是socket关闭
	protected void sendError(){
		// 我想我还可以拯救一下，不做任何处理
	}
	
	public void send(Builder<?> builder){
		byte[] body = builder.build().toByteArray();
		if(!((RpcPo.Builder)builder).getRpc().equals("HeartRpc") && !((RpcPo.Builder)builder).getRpc().equals("HeartReq")){
			log.info("send " + (body.length + 4) + " bytes " + builder.toString());
		}
		if(body.length + Constant.LENGTH_OF_NET_PACKAGE_HEADER > Constant.MAX_SIZE_OF_NET_MESSAGE){ // 尺寸超出
			log.warn("Send Data out of size !"); // 警告，检查发送的数据冗余
		}
		byte[] data = new byte[body.length + Constant.LENGTH_OF_NET_PACKAGE_HEADER];
		data[0] = (byte) ((data.length >> 24) & 0x000000ff);
		data[1] = (byte) ((data.length >> 16) & 0x000000ff);
		data[2] = (byte) ((data.length >> 8) & 0x000000ff);
		data[3] = (byte) ((data.length >> 0) & 0x000000ff);
		System.arraycopy(body, 0, data, 4, body.length);
		
		send(data);
	}
	public void send(byte[] data){
//		sendDataList.add(data);
		int off = 0;
		synchronized (sendDataList) {
			while(off < data.length){ // 分包处理
				byte[] pack = null;
				if(data.length - off < Constant.MAX_SIZE_OF_SINGLE_PACKAGE){
					pack = new byte[data.length - off];
				}else{
					pack = new byte[Constant.MAX_SIZE_OF_SINGLE_PACKAGE];
				}
				System.arraycopy(data, off, pack, 0, pack.length); // 函数参数要注意，顺序不要反
				off += pack.length;
				
				sendDataList.add(pack);	
			}
		}
	}
	
	public String socketInfo(){
		return "";
	}
	public String getSocketType(){
		return "";
	}
	public void setSocketType(String type){
		
	}
	public String getSocketName(){
		return "";
	}
	public void setSocketName(String name){
		
	}
	public String getSocketAddress(){
		return "";
	}
	public void setSocketAddress(String addr){
		
	}
	public int getSocketPort(){
		return 0;
	}
	public void setSocketPort(int port){
		
	}
	public void setWebsocketPort(int port){
	}
	public int getWebsocketPort(){
		return 0;
	}
	public void setUdpPort(int port){
	}
	public int getUdpPort(){
		return 0;
	}
	public void setHeartTime(long time){
		
	}
	// 获得本机ip
	private static String serverIP = "";
	public static String  getServerIp(){
		if(!serverIP.equals("")){
			return serverIP;
		}
		String localip = null;// 本地IP，如果没有配置外网IP则返回它
        String netip = null;// 外网IP
        try {
            Enumeration netInterfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress ip = null;
            boolean finded = false;// 是否找到外网IP
            while (netInterfaces.hasMoreElements() && !finded) {
                NetworkInterface ni = (NetworkInterface) netInterfaces.nextElement();
                Enumeration address = ni.getInetAddresses();
                while (address.hasMoreElements()) {
                    ip = (InetAddress) address.nextElement();
                    if (!ip.isSiteLocalAddress() && !ip.isLoopbackAddress() && ip.getHostAddress().indexOf(":") == -1) {// 外网IP
                        netip = ip.getHostAddress();
                        finded = true;
                        break;
                    } else if (ip.isSiteLocalAddress() && !ip.isLoopbackAddress() && ip.getHostAddress().indexOf(":") == -1) {// 内网IP
                        localip = ip.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }

        if (netip != null && !"".equals(netip)) {
        	serverIP = netip;
            return netip;
        } else {
        	serverIP = localip;
            return localip;
        }
    }
}
