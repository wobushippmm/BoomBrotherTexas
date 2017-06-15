/**
 * AnySDK 支付通知签名算法描述
 *
 *	1. 对所有不为空的参数按照参数名字母升序排列，sign参数不参与签名；
 *	2. 将排序后的参数名对应的参数值字符串方式按顺序拼接在一起；
 *	3. 做一次md5处理并转换成小写，得到的加密串1；
 *	4. 在加密串1末尾追加private_key，做一次md5加密并转换成小写，得到的字符串就是签名sign的值
 *	3. 得到的签名值与参数中的sign对比，相同则验证成功
 */
package logic.world.servlet;

import com.alibaba.fastjson.JSON;
import com.chukong.anysdk.*;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Collections;
import java.io.*;
import java.util.Comparator;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import logic.common.LogicManager;
import logic.common.SendLog;
import logic.common.redis.RedisKey;

import org.apache.log4j.Logger;

import com.chukong.anysdk.PayNotify;
import com.google.protobuf.GeneratedMessageV3.Builder;

import core.config.Constant;
import core.log.LoggerHelper;
import core.net.NetManager;
import core.net.SocketThread;
import protocol.GameData.LogRpc;
import protocol.GameData.RechargeRpc;
import protocol.ProtoUtil;

public class RechargeServlet extends HttpServlet {
	private Logger logger = LoggerHelper.getLogger();
	
	private static final long serialVersionUID = 1L;
	
	public static final String NAME = "recharge";
	

	private static String paramValues = "";

	/**
	 * 从通知参数里面获取到的签名值
	 */
	private static String originSign = "";
	
	public RechargeServlet(){
		
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response){
		response.setHeader("Access-Control-Allow-Origin", "*");
		
		HashMap<String, String> args = new HashMap<String, String>();
		
		paramValues = getValues(request, args);		
		
		PayNotify paynotify = new PayNotify();
		
		try {
			PrintWriter writer = response.getWriter();

			/**
			 * AnySDK分配的 PrivateKey
			 * 
			 * 正式使用时记得用AnySDK分配的正式的PrivateKey
			 */
			paynotify.setPrivateKey("9855AE4D9C89AFCE7DA14A088C162C52");
			
			if (paynotify.checkSign(paramValues, originSign)){
				logger.info("验证签名成功");
				
				// 存入充值队列
				String username = NetManager.redis.hget(RedisKey.OrderList, args.get("order_id"));

				if(username == null){
					SendLog.sendRechargeLog("", request);
					logger.error("Do not find username by order " + args.get("order_id"));
				}else if(username.equals("0")){
					// 已经领取，可能是sdk未收到ok消息
				}else{
					SendLog.sendRechargeLog(username, request);
					NetManager.redis.lpush(RedisKey.Recharge(username), args.get("amount"));
					NetManager.redis.hset(RedisKey.OrderList, args.get("order_id"), "0"); // 删除订单
					
					// 通知scene领取
					RechargeRpc.Builder chargeRpc = RechargeRpc.newBuilder();
					chargeRpc.setUsername(username);
					SendLog.broadcastToScene(chargeRpc);
				}
				
				writer.println("ok");
			} else {
				logger.info("验证签名失败");
				writer.println("failed");
			}
			
			writer.flush();
			writer.close();
		} catch (IOException e) {
			logger.info(e);
		}
	}
	

	protected void doPost(HttpServletRequest request, HttpServletResponse response){
		logger.warn("receive http post");
		doGet(request, response);
	}
	
	public static void sendToDatabase(Builder<?> rpc){
		HashMap<String, SocketThread> servers = NetManager.getServersByType(Constant.TYPE_DATABASE);
		for(SocketThread server : servers.values()){
			server.send(ProtoUtil.packData(rpc));
			break;
		}
	}
	
	public void broadcastToScene(Builder<?> rpc){
		HashMap<String, SocketThread> scenes = NetManager.getServersByType(Constant.TYPE_SCENE);
		for(SocketThread scene : scenes.values()){
			LogicManager.logicThread.sendToServer(scene.getSocketName(), ProtoUtil.packData(rpc), "");
		}
	}
	
	/**
	 * 将参数名从小到大排序，结果如：adfd,bcdr,bff,zx
	 * 
	 * @param List<String> paramNames 
	 */
	public static void sortParamNames(List<String> paramNames) {
		Collections.sort(paramNames, new Comparator<String>() {
			public int compare(String str1,String str2) {
				return str1.compareTo(str2);
			}
		});
	}
	
	/**
	 * 从 HTTP请求参数 生成待签字符串, 此方法需要在 serverlet 下测试, 测试的时候取消注释, 引入该引入的类
	 */
	public static String getValues(HttpServletRequest request, HashMap<String, String> args){
		Enumeration<String> requestParams=request.getParameterNames();//获得所有的参数名
		List<String> params=new ArrayList<String>();
		while (requestParams.hasMoreElements()) {
			params.add((String) requestParams.nextElement());
		}
		sortParamNames(params);// 将参数名从小到大排序，结果如：adfd,bcdr,bff,zx
		String paramValues="";
		for (String param : params) {//拼接参数值
			String paramValue=request.getParameter(param);
			
			args.put(param, paramValue);
			
			if (param.equals("sign")) {
				originSign = request.getParameter(param);
				continue;
			}
			if (paramValue!=null) {
				paramValues+=paramValue;
			}
		}
		
		return paramValues;
	}
}
