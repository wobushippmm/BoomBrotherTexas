import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import redis.clients.jedis.Jedis;
import template.HeroTemplate;
import template.HeroTemplate.HeroTemp;
import template.TableModeTemplate;
import template.TemplateReader;
import ui.ConsoleThread;
import core.config.Constant;
import core.log.LoggerHelper;
import core.net.ConnectThread;
import core.net.NetManager;
import core.net.ServerThread;
import core.net.SocketThread;
import core.udp.UdpThread;
import core.websocket.MyWebSocketServer;
import logic.client.ClientHandler;
import logic.client.ClientLogic;
import logic.common.LogicManager;
import logic.common.data.DataManager;
import logic.common.redis.RedisHandler;
import logic.database.DBLoggerHandler;
import logic.database.DatabaseHandler;
import logic.database.DatabaseLogic;
import logic.gateway.GatewayHandler;
import logic.gateway.GatewayLogic;
import logic.login.LoginHandler;
import logic.login.LoginLogic;
import logic.login.LoginServletHandler;
import logic.scene.AllocBattleHandler;
import logic.scene.RechargeHandler;
import logic.scene.SceneHandler;
import logic.scene.SceneLogic;
import logic.sos.SOSLogic;
import logic.world.EmailHandler;
import logic.world.FriendsHandler;
import logic.world.WorldServletHandler;
import logic.world.gm.GMHandler;
import logic.world.WorldHandler;
import logic.world.WorldLogic;
import logic.battle.BattleLogic;
import logic.battle.boomBrothers.BoomBrothersHandler;
import logic.battle.boomBrothers.entity.EntityCreator;
import logic.battle.texasHoldem.TexasHoldemHandler;

public class Server {
	// 解析命令行参数
	private static void decodeArgs(String[] args){
		for(int i = 0; i<args.length; i++){
			switch(args[i]){
				case "-p": // 当前服务器port
					NetManager.port = Integer.parseInt(args[++i]);
					break;
				case "-t": // 当前服务器类型
					NetManager.serverType = args[++i];
					break;
				case "-sp": // sos port
					NetManager.sosPort = Integer.parseInt(args[++i]);
					break;
				case "-sa": // sos address
					NetManager.sosAddr = args[++i];
					break;
				case "-ac": // accept client
					NetManager.accClient = true;
					break;
				case "-n": // 服务器唯一id
					NetManager.name = args[++i];
					break;
				case "-up": // udp port
					NetManager.udpPort = Integer.parseInt(args[++i]);
					break;
				case "-ra": // redis address
					NetManager.redisAddr = args[++i];
					break;
				case "-rpw": // redis password
					NetManager.redisPass = args[++i];
					break;
				case "-rp": // redis port
					NetManager.redisPort = Integer.parseInt(args[++i]);
					break;
				case "-wp":
					NetManager.websocketPort = Integer.parseInt(args[++i]);
					break;
				case "-msa":
					NetManager.mysqlAddr = args[++i];
					break;
				case "-msp":
					NetManager.mysqlPort = Integer.parseInt(args[++i]);
					break;
				case "-msu":
					NetManager.mysqlUser = args[++i];
					break;
				case "-msw":
					NetManager.mysqlPass = args[++i];
					break;
				case "-msd":
					NetManager.mysqlDB = args[++i];
					break;
			}
		}
	}
	// port type [sos_address sos_port]
	public static void main(String[] args){
		decodeArgs(args);
		Logger log = LoggerHelper.getLogger(); // 初始化log

		
		if(NetManager.name.length() == 0){
			log.warn("Server nameless");
		}
		
		if(NetManager.port > 0){
			NetManager.serverThread = new ServerThread(NetManager.port);
			NetManager.serverThread.start();
		}
		
		if(NetManager.sosAddr != "" && NetManager.sosPort > 0){
			NetManager.sosConnect = new ConnectThread(NetManager.sosAddr, NetManager.sosPort);
			NetManager.sosConnect.setSocketType(Constant.TYPE_SOS);
			NetManager.sosConnect.setSocketName("Boss");
			NetManager.sosConnect.start();
		}
		
		if(NetManager.mysqlPort > 0){
			try {
				Class.forName("com.mysql.jdbc.Driver");
				String url = "jdbc:mysql://" + NetManager.mysqlAddr + ":" + NetManager.mysqlPort + "/" + NetManager.mysqlDB;
				NetManager.mysqlConn = DriverManager.getConnection(url, NetManager.mysqlUser, NetManager.mysqlPass);
				NetManager.mysqlStmt = NetManager.mysqlConn.createStatement();
				log.info("connect to mysql !");
			} catch (ClassNotFoundException | SQLException e) {
				log.error(e);
			}
		}
		
		if(NetManager.redisPort > 0){
			NetManager.redis = new Jedis(NetManager.redisAddr, NetManager.redisPort);
			log.info("Redis ping : " + NetManager.redis.ping());
		}
		
		if(NetManager.udpPort > 0){
			NetManager.udpThread = new UdpThread(NetManager.udpPort);
			NetManager.udpThread.start();
		}
		
		if(NetManager.websocketPort > 0){
			NetManager.websocketThread = new MyWebSocketServer(NetManager.websocketPort);
			NetManager.websocketThread.start();
		}
		
		LogicManager.consoleThread = new ConsoleThread();
		LogicManager.consoleThread.start();
		

		// 加载模板表
		TemplateReader.loadTemplate("template/HeroTemplate.xml", new HeroTemplate());
		TemplateReader.loadTemplate("template/TableModeTemplate.xml", new TableModeTemplate());
		
		// 启动逻辑线程，逻辑线程要在其他线程启动后最后启动
		switch(NetManager.serverType){
			case Constant.TYPE_CLIENT:
				LogicManager.logicThread = new ClientLogic();
				new ClientHandler();
				break;
			case Constant.TYPE_DATABASE:
				LogicManager.logicThread = new DatabaseLogic();
				new DatabaseHandler();
				new DBLoggerHandler();
				break;
			case Constant.TYPE_GATEWAY:
				LogicManager.logicThread = new GatewayLogic();
				new GatewayHandler();
				break;
			case Constant.TYPE_LOGIN:
				LogicManager.logicThread = new LoginLogic();
				new LoginHandler();
				new LoginServletHandler();
				break;
			case Constant.TYPE_SCENE:
				LogicManager.logicThread = new SceneLogic();
				new SceneHandler();
				new AllocBattleHandler();
				new RechargeHandler();
				break;
			case Constant.TYPE_SOS:
				LogicManager.logicThread = new SOSLogic();
				break;
			case Constant.TYPE_WORLD:
				LogicManager.logicThread = new WorldLogic();
				new WorldHandler();
				new FriendsHandler();
				new EmailHandler();
				new WorldServletHandler();
				new GMHandler();
				break;
			case Constant.TYPE_BATTLE:
				LogicManager.logicThread = new BattleLogic();
				if(NetManager.name.equals("Bard")){
					new TexasHoldemHandler();
				}else if(NetManager.name.equals("Bob")){
					new BoomBrothersHandler();
					new EntityCreator(); // 加载地形
				}
				break;
			default:
				log.warn("没有启动任何逻辑!");
				break;
		}
		// 公共的实例
		new DataManager();
		new RedisHandler();
		
		if(LogicManager.logicThread != null){
			LogicManager.logicThread.start();
		}
		
		log.info(NetManager.serverType + " " + NetManager.name + " 开始工作...");
		System.out.println(NetManager.serverType + " " + NetManager.name + ">");

	
		// 关闭服务器处理
		try {
			if(NetManager.serverThread != null){
				NetManager.serverThread.join();
			}
			if(NetManager.sosConnect != null){
				NetManager.sosConnect.join();
			}
			if(LogicManager.logicThread != null){
				LogicManager.logicThread.join();
			}
			if(NetManager.udpThread != null){
				NetManager.udpThread.join();
			}
			// 不管控制台线程了，直接忽略，这个2b线程
			// LogicManager.consoleThread.join();
			System.out.println("关闭服务器成功...");
			log.info("关闭服务器成功...");
			for(int i=3; i>0; i--){
				System.out.println("退出控制台倒计时..." + i);
				Thread.sleep(1000);
			}
			
			if(NetManager.redis != null){
				NetManager.redis.close();
			}
			
			if(NetManager.mysqlConn != null){
				try {
					NetManager.mysqlStmt.close();
					NetManager.mysqlConn.close();
				} catch (SQLException e) {
					log.error(e);
				}
			}
		} catch (InterruptedException e) {
			log.error(e);
		}


		if(LoginServletHandler.instance != null){
			try {
				LoginServletHandler.instance.server.stop();
			} catch (Exception e) {
				log.error(e);
			}
		}
		if(WorldServletHandler.instance != null){
			try {
				WorldServletHandler.instance.server.stop();
			} catch (Exception e) {
				log.error(e);
			}
		}
	}
}
