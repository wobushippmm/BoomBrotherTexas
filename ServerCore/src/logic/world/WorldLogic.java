package logic.world;

import protocol.Protocol.RpcPo;
import core.net.SocketThread;
import logic.common.LogicThread;

public class WorldLogic extends LogicThread {
	// 使gateway转发
	public void sendToClient(String username, RpcPo.Builder builder, SocketThread gateway){
		builder.setClientName(username); // 转发到客户端
		gateway.send(builder);;
	}
}
