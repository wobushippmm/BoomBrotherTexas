package logic.scene;

import java.util.HashMap;

import protocol.Protocol;
import protocol.Protocol.RpcPo;
import core.net.NetManager;
import core.net.SocketThread;
import logic.common.LogicManager;
import logic.common.LogicThread;
import logic.sos.SOSLogic;

public class SceneLogic extends LogicThread {

	
	public void startLogic(){
	}
	
	public void sendToClient(String username, RpcPo.Builder builder, SocketThread gateway){
		builder.setClientName(username);
		gateway.send(builder);;
	}
}
