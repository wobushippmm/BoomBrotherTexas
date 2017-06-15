package logic.common;

import java.util.HashMap;

import core.config.Constant;
import core.net.NetManager;
import core.net.SocketTerminal;
import core.net.SocketThread;

public class ServerHelper {
	public static SocketThread getDatabase(){
		HashMap<String, SocketThread> hash = NetManager.getServersByType(Constant.TYPE_DATABASE);
		for(SocketThread server : hash.values()){
			return server;
		}
		return null;
	}
	
	public static SocketThread getWorld(){
		HashMap<String, SocketThread> hash = NetManager.getServersByType(Constant.TYPE_WORLD);
		for(SocketThread server : hash.values()){
			return server;
		}
		return null;
	}
}
