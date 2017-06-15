package logic.world.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.eclipse.jetty.util.security.Credential.MD5;

import com.alibaba.fastjson.JSON;

import core.log.LoggerHelper;
import logic.world.gm.GMCommand;
import logic.world.gm.GMHandler;

public class GMServlet extends HttpServlet {
	private Logger logger = LoggerHelper.getLogger();
	
	private static final long serialVersionUID = 1L;
	
	public static final String NAME = "gm";
	
	// gm口令
	public static final String PASSWORD = "gmpw";//MD5.digest("gmpw");
	
	public GMServlet(){
		
	}
	
	// 固定参数
	// password
	// cmd
	// URL地址直接传递数组参数  
	// http://localhost/Api/Public/yanglao/index.php?
	// service=User.PostServicePayment&elder_id=17&user_id=1592&pay_points=1&pay_money=0&charge_ids[]=1&charge_ids[]=2  
	protected void doGet(HttpServletRequest request, HttpServletResponse response){
		response.setHeader("Access-Control-Allow-Origin", "*");
		
		String password = request.getParameter("password");
		String cmd = request.getParameter("cmd");
		if(password != null && cmd != null){
			// 需要执行口令
			if(PASSWORD.equals(password)){
					PrintWriter writer;
					try {
						writer = response.getWriter();
						
						if(GMHandler.instance != null){
							HashMap<String, String> res = new HashMap<String, String>();
							res.put("msg", "GM Command " + request.getParameter("cmd") +":\n");
							try {
								GMHandler.instance.getClass().getMethod(cmd, 
										HttpServletRequest.class, HttpServletResponse.class, res.getClass())
									.invoke(GMHandler.instance, request, response, res);
								
								String str = JSON.toJSONString(res);
								writer.print(str);
								logger.info(str);
							} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
									| NoSuchMethodException | SecurityException e) {
								LoggerHelper.getLogger().error(e);
							}
						}
						
						writer.flush();
						writer.close();
					} catch (IOException e) {
						LoggerHelper.getLogger().error(e);
					}
			}
		}
	}
}
