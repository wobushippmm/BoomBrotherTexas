package protocol;

import java.util.HashMap;

import com.google.protobuf.GeneratedMessageV3.Builder;

public class ProtoUtil {
	public static Protocol.RpcPo.Builder packData(Builder<?> data){
		Protocol.RpcPo.Builder builder = Protocol.RpcPo.newBuilder(); 
		builder.setRpc(data.getDefaultInstanceForType().getClass().getSimpleName());
		builder.setAnyPo(data.build().toByteString());
		return builder;
	}
}
