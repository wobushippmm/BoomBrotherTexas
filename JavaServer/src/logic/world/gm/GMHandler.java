package logic.world.gm;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.Endpoint;

import logic.common.LogicManager;
import logic.common.SendLog;
import logic.common.redis.RedisHandler;
import logic.common.redis.RedisKey;
import logic.database.DatabaseHandler;
import logic.world.EmailHandler;
import logic.world.WorldHandler;
import logic.world.WorldUserData;

import org.apache.log4j.Logger;

import protocol.GameData.AddTempGoldRpc;
import protocol.GameData.EmailDat;
import protocol.GameData.EnterWorldRpc;
import protocol.GameData.ExitWorldRpc;
import protocol.GameData.KickOutRpc;
import protocol.GameData.LogRpc;
import protocol.GameData.LogTypeEnm;
import protocol.ProtoUtil;

import com.alibaba.fastjson.JSON;
import com.google.protobuf.GeneratedMessageV3.Builder;

import core.config.Constant;
import core.log.LoggerHelper;
import core.net.NetManager;
import core.net.SocketThread;

// GM工具接口
// 注意：GM和逻辑不在同一个线程，要考虑线程同步问题
public class GMHandler {
	private Logger log = LoggerHelper.getLogger();
	public static GMHandler instance = null;
	
	public LinkedList<GMCommand> commandList = new LinkedList<GMCommand>();
	// 接口调用时需要提供GM帐号和密码
	// 数据库根据帐号开放接口权限
	public GMHandler(){
		instance = this;

		LogicManager.logicThread.loopLogicFuncs.add(new Object[]{"onUpdate", this});
	}
	// 在逻辑线程中执行
	public int onUpdate(){
		synchronized (commandList) {
			if(commandList.size() > 0){
				GMCommand command = commandList.removeFirst();
				try {
					this.getClass().getMethod(command.cmd, GMCommand.class).invoke(this, command);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
						| NoSuchMethodException | SecurityException e) {
					log.error(e);
				}
			}
		}
		return 0;
	}
	// ?password=&cmd=&username=
	public void playerInfo(HttpServletRequest request, HttpServletResponse response, HashMap<String, String> res){
		String username = request.getParameter("username");
		String key = RedisKey.UserKey(username);
		if(!NetManager.redis.exists(key)){
			res.put("result", "fail ! no body named " + username);
		}
		List<String> list = NetManager.redis.hmget(key, 
				RedisKey.Nickname, 
				RedisKey.Gold, 
				RedisKey.Level);
		
		res.put(RedisKey.Username, username);
		res.put(RedisKey.Nickname, list.get(0));
		res.put(RedisKey.Gold, list.get(1));
		res.put(RedisKey.Level, list.get(2));
	}
	// ?password=&cmd=&username=&time=
	// time=0 解冻 time>0 冻结毫秒数
	public void freeze(HttpServletRequest request, HttpServletResponse response, HashMap<String, String> res){
		String username = request.getParameter("username");
		long time = RedisHandler.parseLong(request.getParameter("time"));
		
		String key = RedisKey.UserKey(username);
		
		if(NetManager.redis.exists(key)){
			if(time > 0){
				NetManager.redis.hset(key, RedisKey.Freeze, (System.currentTimeMillis() + time) + "");
				// 通知gateway断开连接
				WorldUserData user = WorldHandler.instance.userDic.get(username);
				if(user != null){
					KickOutRpc.Builder rpc = KickOutRpc.newBuilder();
					rpc.setUsername(username);
					LogicManager.logicThread.sendToServer(user.gateway, ProtoUtil.packData(rpc), "");
				}
			}else{
				NetManager.redis.hset(key, RedisKey.Freeze, "0");
			}
			
			SendLog.sendGMLog(username, request);
		}
		
		res.put("result", "success");
	}
	
	public void sendEmail(GMCommand command){
		EmailHandler.instance.saveEmail((EmailDat) command.args);
	}
	// ?password=&cmd=&target=,&msg=&gold=
	public void sendEmail(HttpServletRequest request, HttpServletResponse response, HashMap<String, String> res){
		// 做一些必要判断
		// 做不需要线程同步的事
		String targets = request.getParameter("target");
		if(targets == null){
			res.put("result", "fail ! no target");
		}
		String msg = request.getParameter("msg");
		if(msg == null){
			res.put("result",  "fail ! no message");
		}
		EmailDat.Builder email = EmailDat.newBuilder();
		email.setFrom("GM");
		String[] target = targets.split(",");
		for(int i=0; i<target.length; i++){
			email.addTarget(target[i]);
		}
		email.setMsg(msg);
		int gold = RedisHandler.parseInt(request.getParameter("gold"));
		email.setGold(gold);
		
		synchronized (commandList) {
			commandList.add(new GMCommand(request.getParameter("cmd"), email.build()));
		}
		
		res.put("result", "success");
		
		SendLog.sendGMLog(targets, request);
	}
	// ?password=&cmd=&username=&gold=
	public void addGold(HttpServletRequest request, HttpServletResponse response, HashMap<String, String> res){
		String username = request.getParameter("username");
		int gold = RedisHandler.parseInt(request.getParameter("gold"));
	
		// 存入缓存队列
		String key = RedisKey.TempGold(username);
		NetManager.redis.lpush(key, gold + "");
		
		// 通知领取临时金币
		AddTempGoldRpc.Builder rpc = AddTempGoldRpc.newBuilder();
		rpc.setUsername(username);
		rpc.setGold(gold);
		SendLog.broadcastToScene(rpc);
		
		res.put("result", "success");
		
		SendLog.sendGMLog(username, request);
	}
	

}
