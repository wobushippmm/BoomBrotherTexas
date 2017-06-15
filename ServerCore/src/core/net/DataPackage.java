package core.net;

import java.lang.reflect.Array;
import java.net.Socket;

import com.google.protobuf.InvalidProtocolBufferException;

import core.config.Constant;
import core.log.LoggerHelper;
import io.netty.channel.Channel;
import protocol.Protocol;

public class DataPackage {
	public byte[] data = null; // 包完整数据 header 4 byte
	public SocketTerminal termianl = null; // 发送者socket
	public int bodySize = 0;
	public Protocol.RpcPo rpcPo = null;
	
	public DataPackage(byte[] data, SocketTerminal termianl){
		this.data = data;
		this.termianl = termianl;
		this.bodySize = data.length - Constant.LENGTH_OF_NET_PACKAGE_HEADER;
		
		decode();
	}
	
	public void decode(){
		byte[] body = new byte[bodySize];
		System.arraycopy(data, Constant.LENGTH_OF_NET_PACKAGE_HEADER, body, 0, bodySize);
		// 解析数据
		try {
			rpcPo = Protocol.RpcPo.parseFrom(body);
		} catch (InvalidProtocolBufferException e) {
			LoggerHelper.getLogger().error(e);
		}
	}
}
