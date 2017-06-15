package logic.world.gm;

import java.util.Map;

// GM命令
public class GMCommand {
	public String cmd = "";
	public Object args = null; // 可以是任意数据类型，根据需求
	
	public GMCommand(String cmd, Object args){
		this.cmd = cmd;
		this.args = args;
	}
}
