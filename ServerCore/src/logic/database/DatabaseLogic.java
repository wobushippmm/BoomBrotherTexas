package logic.database;

import core.net.SocketThread;
import logic.common.LogicThread;
import protocol.Protocol.RpcPo;

// 最终目标redis作为数据缓存使用
// 控制数据库和redis的数据平衡，热点数据存入redis，冷点数据存入数据库
// 支持大注册量的方法：登录时将数据库数据转入redis
public class DatabaseLogic extends LogicThread {
	public void startLogic(){
	}

	// 使gateway转发
	public void sendToClient(String username, RpcPo.Builder builder, SocketThread gateway){
		builder.setClientName(username); // 转发到客户端
		gateway.send(builder);
	}
}
