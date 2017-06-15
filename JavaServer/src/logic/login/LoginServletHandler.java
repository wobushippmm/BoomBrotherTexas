package logic.login;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import core.log.LoggerHelper;
import logic.common.LogicConfig;
import logic.login.servlet.LoginCheckServlet;


public class LoginServletHandler {
	private Logger log = LoggerHelper.getLogger();
	public static LoginServletHandler instance = null;
	
	public Server server = null;
	public ServletContextHandler context = null;
	
	public LoginServletHandler(){
		instance = this;
		
		server = new Server(LogicConfig.loginServletPort);
		context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
		context.setContextPath("/");
		server.setHandler(context);
		
		context.addServlet(new ServletHolder(new LoginCheckServlet()), "/" + LoginCheckServlet.NAME);
		
		try {
			server.start();
		} catch (Exception e) {
			log.error(e);
		}
	}
}
