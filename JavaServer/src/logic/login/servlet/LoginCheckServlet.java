package logic.login.servlet;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;

import core.log.LoggerHelper;
import logic.common.SendLog;

// 等待anysdk验证通过的通知
public class LoginCheckServlet extends HttpServlet {
	private Logger logger = LoggerHelper.getLogger();
	private static final long serialVersionUID = 1L;
	
	public static final String NAME = "login";
	
	public LoginCheckServlet(){
		
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response){
		logger.info(JSON.toJSONString(SendLog.getArgs(request)));
		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);
		try {
			response.getWriter().println("<h1>" + NAME + "</h1>");
		} catch (IOException e) {
			LoggerHelper.getLogger().error(e);
		}
	}
	protected void doPost(HttpServletRequest request, HttpServletResponse response){
		logger.warn("receive http post");
		doGet(request, response);
	}
}
