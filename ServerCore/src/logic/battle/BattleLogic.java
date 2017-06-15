package logic.battle;

import core.net.NetManager;
import core.net.SocketThread;
import logic.common.LogicManager;
import logic.common.LogicThread;
import protocol.Protocol.RpcPo;

public class BattleLogic extends LogicThread {
	public void startLogic(){
	}
	
	// 使gateway转发
	public void sendToClient(String username, RpcPo.Builder builder, SocketThread gateway){
		builder.setClientName(username); // 转发到客户端
		gateway.send(builder);;
	}
}
