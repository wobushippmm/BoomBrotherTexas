package logic.common.data;

import java.util.HashMap;

public class DataManager {
	public static DataManager instance;
	
	public HashMap<String, UserData> userDic = new HashMap<String, UserData>(); // 用户列表
	
	public DataManager(){
		instance = this;
	}
	public UserData getUser(String username){
		return userDic.get(username);
	}
	public void addUser(UserData user){
		userDic.put(user.username, user);
	}
	public UserData delUser(String username){
		return userDic.remove(username);
	}
}
