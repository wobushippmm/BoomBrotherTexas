package logic.login;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.google.protobuf.InvalidProtocolBufferException;

import protocol.GameData;
import protocol.GameData.AnySdkLoginRep;
import protocol.GameData.AnySdkLoginReq;
import protocol.GameData.LoginReq;
import protocol.GameData.LoginResultEnm;
import protocol.GameData.UserDat;
import protocol.ProtoUtil;
import protocol.GameData.LoginRep;
import logic.common.LogicManager;
import logic.common.data.UserData;
import logic.common.redis.RedisHandler;
import logic.common.redis.RedisKey;
import core.config.Constant;
import core.log.LoggerHelper;
import core.net.ClientThread;
import core.net.DataPackage;
import core.net.NetManager;
import core.net.SocketTerminal;
import core.net.SocketThread;

public class LoginHandler {
	private Logger log = LoggerHelper.getLogger();
	public static LoginHandler instance = null;
	public static Pattern pattern = Pattern.compile("^[a-zA-Z0-9_\u4e00-\u9fa5]+$");
	
	public LoginHandler(){
		instance = this;
		LogicManager.logicThread.setRpc(LoginReq.class, this);
		LogicManager.logicThread.setRpc(AnySdkLoginReq.class, this);
	}
	
	public void onAnySdkLoginReq(DataPackage data){
		// 判断用户名
		AnySdkLoginReq req;
		try {
			req = AnySdkLoginReq.parseFrom(data.rpcPo.getAnyPo());
			AnySdkLoginRep.Builder loginRep = AnySdkLoginRep.newBuilder();
			if(req.getUsername().length() == 0 || req.getUsername().length() > 12){
				loginRep.setResult(LoginResultEnm.USERNAME_ERROR);
			}else if(!pattern.matcher(req.getUsername()).matches()){
				loginRep.setResult(LoginResultEnm.USERNAME_ERROR);
			}else if(req.getKeysList().indexOf("channel") < 0 
					|| req.getKeysList().indexOf("uapi_key") < 0
					|| req.getKeysList().indexOf("uapi_secret") < 0){
				loginRep.setResult(LoginResultEnm.LACK_PARAMETER);
			}else{
				// 验证通过
				URL url;
				try {
					url = new URL("http://oauth.anysdk.com/api/User/LoginOauth/");
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					conn.setRequestProperty( "User-Agent", "px v1.0" );
					conn.setReadTimeout(30 * 1000);
					conn.setConnectTimeout(30 * 1000);
					conn.setRequestMethod("POST");
					conn.setDoInput(true);
					conn.setDoOutput(true);
					
					OutputStream os = conn.getOutputStream();
		            BufferedWriter writer = new BufferedWriter( new OutputStreamWriter(os, "UTF-8") );
		            
		            String queryStr = "";
		            for(int i=0; i<req.getKeysCount(); i++){
		            	queryStr += req.getKeys(i) + "=" + req.getValues(i) + "&";
		            }
		            queryStr = queryStr.substring(0, queryStr.length() - 1);
		            writer.write(queryStr);
		            writer.flush();
		            writer.close();
		            os.close();
		            
		            InputStream is = conn.getInputStream();
		    		
	    			BufferedReader br = new BufferedReader( new java.io.InputStreamReader( is ));	
	    			String line = "";
	    			StringBuilder sb = new StringBuilder();
	    			while( ( line = br.readLine() ) != null ) {
	    				sb.append( line );
	    			}
	    			loginRep.setSdkMsg(sb.toString());
		    		
				} catch (IOException e) {
					log.error(e);
				}
			}
			
			// 满足发送条件
			data.termianl.setSocketType(Constant.TYPE_CLIENT);
			// 设置超时时间 ，一次心跳超时
			data.termianl.setHeartTime(System.currentTimeMillis());
			data.termianl.setSocketName(req.getUsername());
			data.termianl.send(ProtoUtil.packData(loginRep));
			// 这里不退出，还要等sdk返回，超时和心跳能保证闲置占用
		} catch (InvalidProtocolBufferException e) {
			log.error(e);
		}
		
	}
	
	// 收到客户端的登录请求
	// 登录的时候如果redis中没有数据，再去mysql中查询，把mysql中的数据移动到redis
	public void onLoginReq(DataPackage data){
		try {
			GameData.LoginReq req = GameData.LoginReq.parseFrom(data.rpcPo.getAnyPo().toByteArray());
			GameData.LoginRep.Builder loginRep = GameData.LoginRep.newBuilder();
			
			// 判断用户名
			if(req.getUsername().length() == 0 || req.getUsername().length() > 12){
				loginRep.setResult(LoginResultEnm.USERNAME_ERROR);
			}else if(!pattern.matcher(req.getUsername()).matches()){
				loginRep.setResult(LoginResultEnm.USERNAME_ERROR);
			}else{
				// 查询数据库，核对密码
				String key1 = RedisKey.UserKey(req.getUsername());
				if(NetManager.redis.exists(key1)){
					String pw = NetManager.redis.hget(key1, RedisKey.Password);
					if(pw.length() > 0 && pw.equals(req.getPassword())){
						long now = System.currentTimeMillis();
						String freeze = NetManager.redis.hget(key1, RedisKey.Freeze);
						if(now < RedisHandler.parseLong(freeze)){
							loginRep.setResult(LoginResultEnm.ACCOUNT_FREEZE);
						}else{
							// 在调用之前还不能使用sendToClient
							NetManager.setClient(req.getUsername(), data.termianl); // 切换为非临时连接
							UserData userData = RedisHandler.instance.queryUser(req.getUsername());
							loginRep.setResult(LoginResultEnm.OK_LOGINRESULT);
							long cid = NetManager.redis.incr(RedisKey.IDCounter);
							loginRep.setCid(cid);
							NetManager.redis.hset(key1, RedisKey.Clientid, cid + "");
							NetManager.redis.hset(key1, RedisKey.LoginTime, System.currentTimeMillis() + "");
						}
					}else{
						loginRep.setResult(LoginResultEnm.PASSWORD_ERROR);
					}
				}else{
					// 是nickname
					if(NetManager.redis.hexists(RedisKey.NicknameTable, req.getUsername())){
						loginRep.setResult(LoginResultEnm.USERNAME_ERROR);
					}else{
						// 在调用之前还不能使用sendToClient
						NetManager.setClient(req.getUsername(), data.termianl); // 切换为非临时连接
						// 不存在用户名，创建新用户
						UserData userData = RedisHandler.instance.createUser(req.getUsername(), req.getPassword());
						loginRep.setResult(LoginResultEnm.CREATE_ACCOUNT);
						long cid = NetManager.redis.incr(RedisKey.IDCounter);
						loginRep.setCid(cid);
						NetManager.redis.hset(key1, RedisKey.Clientid, cid + "");
						NetManager.redis.hset(key1, RedisKey.LoginTime, System.currentTimeMillis() + "");
					}
				}
			}
			// 这里不能用这个，因为如果验证未通过的话，还未移到cients列表
			//NetManager.sendToClient(req.getUsername(), ProtoUtil.packData("onLogin", builder));
			// 不需要打招呼，满足发送条件
			data.termianl.setSocketType(Constant.TYPE_CLIENT);

			SocketThread gateway = allocate();
			loginRep.setGatewayHost(gateway.getSocketAddress());
			if(data.termianl.channel != null){
				loginRep.setGatewayPort(gateway.getWebsocketPort());
			}else{
				loginRep.setGatewayPort(gateway.getSocketPort());
			}
			data.termianl.send(ProtoUtil.packData(loginRep));
		} catch (InvalidProtocolBufferException e) {
			log.error(e);
		}
		data.termianl.quit(); // 不管登录成功与否都退出
	}
	
	private SocketThread allocate(){
		// 分配一个scene
		HashMap<String, SocketThread> hash = NetManager.getServersByType(Constant.TYPE_GATEWAY);
		for(SocketThread server : hash.values()){
			return server;
		}
		return null;
	}
}
