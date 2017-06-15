package core.udp;

import java.net.DatagramPacket;

import com.google.protobuf.InvalidProtocolBufferException;

import core.config.Constant;
import core.log.LoggerHelper;
import protocol.Protocol;

public class UdpDataPackage {
	public DatagramPacket packet = null;
	public Protocol.RpcPo rpcPo = null;
	public String address = "";
	public int port = 0;
	public int packID = 0;
	public UdpDataPackage(DatagramPacket packet){
		this.packet = packet;
		decode();
	}
	
	private void decode(){
		// 解码
		address = packet.getAddress().getHostAddress();
		port = packet.getPort();
		byte[] data = packet.getData();
		byte[] body = new byte[data.length - Constant.LENGTH_OF_NET_PACKAGE_HEADER];
		packID = ((data[0] & 0x000000ff) << 24) + // java只有有符号数，通过&0xff去掉符号
				((data[1] & 0x000000ff) << 16) + // 这里byte在&时转成了int类型
				((data[2] & 0x000000ff) << 8) +
				((data[3] & 0x000000ff) << 0);
		System.arraycopy(data, Constant.LENGTH_OF_NET_PACKAGE_HEADER, body, 0, body.length);
		// 解析数据
		try {
			rpcPo = Protocol.RpcPo.parseFrom(body);
		} catch (InvalidProtocolBufferException e) {
			LoggerHelper.getLogger().error(e);
		}
	}
}
