package logic.world;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import core.log.LoggerHelper;
import logic.common.LogicConfig;
import logic.login.servlet.LoginCheckServlet;
import logic.world.servlet.GMServlet;
import logic.world.servlet.RechargeServlet;


public class WorldServletHandler {
	private Logger log = LoggerHelper.getLogger();
	public static WorldServletHandler instance = null;
	
	public Server server = null;
	public ServletContextHandler context = null;
	
	public WorldServletHandler(){
		instance = this;
		
		server = new Server(LogicConfig.worldServletPort);
		context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
		context.setContextPath("/");
		server.setHandler(context);
		
		context.addServlet(new ServletHolder(new RechargeServlet()), "/" + RechargeServlet.NAME);
		context.addServlet(new ServletHolder(new GMServlet()), "/" + GMServlet.NAME);
		
		try {
			server.start();
		} catch (Exception e) {
			log.error(e);
		}
	}
}
