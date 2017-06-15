package ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import core.log.LoggerHelper;
import core.net.DataPackage;
import logic.common.LogicManager;

public class ConsoleThread extends Thread {
	private Logger log = LoggerHelper.getLogger();
	
	private Hashtable<String, Object> cmdDic = new Hashtable<String, Object>();
	
	public void setCmd(String rpc, Object obj){
		cmdDic.put(rpc, obj);
	}
	
	public Object getCmd(String rpc){
		return cmdDic.get(rpc);
	}
	
	private BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	private boolean quit = false;
	public boolean showCmd = false; // 显示命令开关
	public synchronized void setQuit(){
		quit = true;
		interrupt();
		try {
			System.in.close();
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void run(){
		while(!quit){
			try {
				if(showCmd || br.ready()){ // 加这句就成异步了，但是不显示输入
					String str = br.readLine(); // 阻塞状态
					String[] cmd = str.split(" ");
					switch(cmd[0]){
						case "quit": // 退出
							if(LogicManager.logicThread != null){
								LogicManager.logicThread.shutdown();
							}else{
								quit = true;
							}
							break;
						case "c": // 进入cmd模式
							showCmd = true;
							System.out.print("cmd show >>>>>>>>>>>>>>>>");
							break;
						case "q":
							showCmd = false;
							System.out.print("<<<<<<<<<<<<<<<<< cmd hide");
							break;
						default:
							Object obj = getCmd(cmd[0]);
							if(obj != null){
								try {
									// public void funcname(String[]) 格式
									obj.getClass().getMethod(cmd[0], String[].class).invoke(obj, (Object)cmd);//这里必须加(Object)否则异常
								} catch (IllegalAccessException
										| IllegalArgumentException
										| InvocationTargetException
										| NoSuchMethodException
										| SecurityException e) {
									log.error(e);
								} // 执行rpc
							}
							
							break;
					}
				}else{
					sleep(10);
				}
			} catch (IOException | InterruptedException e) {
				log.error(e);
			}
		}
		
		log.info("Quit ConsoleThread");
	}
}
