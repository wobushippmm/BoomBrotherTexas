package logic.gateway;

import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;

import com.google.protobuf.GeneratedMessageV3.Builder;

import protocol.Protocol.RpcPo;
import core.net.ClientThread;
import core.net.DataPackage;
import core.net.NetManager;
import logic.common.LogicManager;
import logic.common.LogicThread;

// 保持与客户端的连接，阻挡客户端连接的异常情况，保证gate内稳定
public class GatewayLogic extends LogicThread {
	// rpc转发表，rpc -> type
	private Hashtable<String, String> rpc2TypeDic = new Hashtable<String, String>();
	
	public void setRpc2Type(Class<?> rpc, String type){
		rpc2TypeDic.put(rpc.getSimpleName(), type);
	}
	
	public String getRpc2Type(String rpc){
		return rpc2TypeDic.get(rpc);
	}
	
	public void startLogic(){
		
	}
	

	
	// 处理tcp调用,转发rpc
	public int dealTcpRpc(){
		synchronized (NetManager.dataList) {
			if(NetManager.dataList.size() > 0){
				DataPackage data = NetManager.dataList.remove(0);
				// 解析数据
				// gateway也可以处理转发消息，形成对消息的过滤或附加处理
				Object obj = getRpc(data.rpcPo.getRpc());
				if(obj != null){
					try {
						// 必须是 public void funcname(DataPackage) 格式
						obj.getClass().getMethod("on" + data.rpcPo.getRpc(), DataPackage.class).invoke(obj, data); // 执行rpc
					} catch (SecurityException | NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						log.error(data.rpcPo.getRpc(), e);
					}
				}
				// 转发数据
				if(!data.rpcPo.getClientName().equals("")){ // 如果是发送到客户端
					NetManager.sendToClient(data.rpcPo.getClientName(), data.data);
				}else{ // 转发到服务器
					String type = getRpc2Type(data.rpcPo.getRpc());
					if(type != null){
						// 转发对象
						String toName = data.termianl.getType2Name(type);
						if(toName != null){
							if(NetManager.getServer(toName) != null){
								sendToServer(toName, data.rpcPo.toBuilder(), data.termianl.getSocketName());
							}
						}
					}
				}
				return 1;
			}
		}
		return 0;
	}
	
	public void clientDisconnect(ClientThread client){
		log.info("..........................clientDisconnect");
	}
	
	// 给协议添加客户端用户名
	public void sendToServer(String toname, RpcPo.Builder builder, String username){
		builder.setClientName(username);
		NetManager.sendToServer(toname, builder);
	}
}
