package core.log;


import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Logger;

import core.net.NetManager;

public class LoggerHelper {
	private static Logger logger = null;
	
	// 直接返回logger，不要再包装，否则定位不到log输出位置
	public static Logger getLogger(){
		if(logger != null){
			return logger;
		}
		logger = Logger.getLogger(LoggerHelper.class);
		DailyRollingFileAppender appender = (DailyRollingFileAppender)Logger.getRootLogger().getAppender("D");
		appender.setFile( "./log_" + NetManager.port + "_" + NetManager.name + ".log");//动态地修改这个文件名 
		appender.activateOptions();
		return logger;
	}
}

