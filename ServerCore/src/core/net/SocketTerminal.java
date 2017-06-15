package core.net;

import java.util.Hashtable;

import org.apache.log4j.Logger;

import com.google.protobuf.GeneratedMessageV3.Builder;

import core.config.Constant;
import core.log.LoggerHelper;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import logic.common.LogicManager;
import protocol.Protocol.RpcPo;

public class SocketTerminal {
	protected Logger log = LoggerHelper.getLogger(); 
	public SocketThread socketThread = null;
	public Channel channel = null;
	
	public SocketTerminal(SocketThread socket){
		socketThread = socket;
		socket.setTerminal(this);
	}
	
	public void setHeartTime(long time){
		if(socketThread != null){
			socketThread.setHeartTime(time);
		}
	}
	
	// gateway 转发列表 type -> name
	private Hashtable<String, String> type2NameDic = new Hashtable<String, String>();
	public void setType2Name(String type, String name){
		if(name == null){
			type2NameDic.remove(type);
		}else{
			type2NameDic.put(type, name);
		}
	}
	public String getType2Name(String type){
		return type2NameDic.get(type);
	}
	
	public String clientName = "";
	public long heartTime = Long.MAX_VALUE; // 上次收到心跳时间
	public ByteBuf buf = null;
	public boolean readHead = true;
	public int dataSize = 0;
	public byte[] recData = null;
	public int offset = 0;
	
	public boolean callDisconnectFunc = true; // 是否调用断线回调，如顶号时不需要回调
	
	public SocketTerminal(ChannelHandlerContext client){
		channel = client.channel();
		buf = client.alloc().buffer();
	}
	
	public void quit(){
		if(socketThread != null){
			socketThread.quit = true;
		}
		if(channel != null){
			if(channel.isOpen()){
				channel.close();
				buf.release();
			}
			if(callDisconnectFunc){
				LogicManager.logicThread.clientDisconnect(this);
			}
		}
	}
	
	public void send(Builder<?> builder){
		if(socketThread != null){
			socketThread.send(builder);
		}
		if(channel != null){
			byte[] body = builder.build().toByteArray();
			if(!((RpcPo.Builder)builder).getRpc().equals("heart")){
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
	}
	
	public void send(byte[] data){
		if(socketThread != null){
			socketThread.send(data);
		}
		if(channel != null){
			int off = 0;
			while(off < data.length){ // 分包处理
				byte[] pack = null;
				if(data.length - off < Constant.MAX_SIZE_OF_SINGLE_PACKAGE){
					pack = new byte[data.length - off];
				}else{
					pack = new byte[Constant.MAX_SIZE_OF_SINGLE_PACKAGE];
				}
				System.arraycopy(data, off, pack, 0, pack.length); // 函数参数要注意，顺序不要反
				off += pack.length;
				
				ByteBuf b = channel.alloc().buffer();
				b.writeBytes(pack);
				channel.writeAndFlush(new BinaryWebSocketFrame(b)); // 直接发送,会回收b
			}
		}
	}
	
	public String getSocketType(){
		if(socketThread != null){
			return socketThread.getSocketType();
		}
		if(channel != null){ // websocket目前只支持客户端
			return Constant.TYPE_CLIENT;
		}
		return "";
	}
	public void setSocketType(String type){
		if(socketThread != null){
			socketThread.setSocketType(type);
		}
		if(channel != null){
			
		}
	}
	public String getSocketName(){
		if(socketThread != null){
			return socketThread.getSocketName();
		}
		if(channel != null){
			return clientName;
		}
		return "";
	}
	public void setSocketName(String name){
		if(socketThread != null){
			socketThread.setSocketName(name);
		}
		if(channel != null){
			clientName = name;
		}
	}
	public int getSocketPort(){
		if(socketThread != null){
			return socketThread.getSocketPort();
		}
		if(channel != null){
			
		}
		return 0;
	}
	public void setSocketPort(int port){
		if(socketThread != null){
			socketThread.setSocketPort(port);
		}
		if(channel != null){
			
		}
	}
	public String getSocketAddress(){
		if(socketThread != null){
			return socketThread.getSocketAddress();
		}
		if(channel != null){
			if(channel.remoteAddress().equals("127.0.0.1")){
				return SocketThread.getServerIp();
			}
		}
		return "";
	}
	public void setSocketAddress(String addr){
		if(socketThread != null){
			socketThread.setSocketAddress(addr);
		}
		if(channel != null){
			
		}
	}
}
