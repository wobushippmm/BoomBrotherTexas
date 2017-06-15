package logic.client;

import core.net.NetManager;
import logic.common.LogicManager;
import logic.common.LogicThread;

public class ClientLogic extends LogicThread {
	public void startLogic(){
	}
	
	public synchronized void shutdown(){
		LogicManager.consoleThread.setQuit();
		LogicManager.logicThread.quit = true;
		NetManager.quit = true;
	}

}
