package logic.common.redis;

import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.protobuf.InvalidProtocolBufferException;

import protocol.GameData.EmailDat;
import protocol.GameData.EmailListRep;
import protocol.GameData.FriendDat;
import protocol.GameData.FriendListRep;
import core.log.LoggerHelper;
import core.net.NetManager;
import logic.common.data.UserData;
import logic.world.WorldUserData;

public class RedisHandler {
	private Logger log = LoggerHelper.getLogger();
	public static RedisHandler instance = null;
	
	public RedisHandler(){
		instance = this;
	}
	public WorldUserData queryWorldUser(String username){
		WorldUserData user = new WorldUserData();
		user.username = username;
		
		String userKey = RedisKey.UserKey(username);
		List<String> res = NetManager.redis.hmget(userKey, RedisKey.Nickname, RedisKey.Portrait);
		user.nickname = RedisHandler.parseString(res.get(0));
		user.portrait = RedisHandler.parseString(res.get(1));
		
		queryFriend(user);
		queryEmail(user);
		
		return user;
	}
	public WorldUserData queryEmail(String username){
		WorldUserData user = new WorldUserData();
		user.username = username;
		return queryEmail(user);
	}
	public WorldUserData queryEmail(WorldUserData user){
		String mailKey = RedisKey.EmailUserKey(user.username);
		String mailData = NetManager.redis.get(mailKey);
		
		try {
			EmailListRep mailList = EmailListRep.parseFrom(parseBytes(mailData));
			for(int i=0; i<mailList.getEmailListCount(); i++){
				user.emailList.add(mailList.getEmailList(i).toBuilder());
			}
		} catch (InvalidProtocolBufferException e) {
		}
		
		return user;
	}
	public WorldUserData queryFriend(String username){
		WorldUserData user = new WorldUserData();
		user.username = username;
		return queryFriend(user);
	}
	public WorldUserData queryFriend(WorldUserData user){
		String key = RedisKey.FriendKey(user.username);
		String friendData = NetManager.redis.get(key);
		
		try {
			FriendListRep friendList = FriendListRep.parseFrom(parseBytes(friendData));
			for(int i=0; i<friendList.getFriendListCount(); i++){
				List<String> res = NetManager.redis.hmget(RedisKey.UserKey(friendList.getFriendList(i).getUsername()), 
						RedisKey.Nickname, RedisKey.Portrait);
				FriendDat.Builder dat = friendList.getFriendList(i).toBuilder();
				dat.setNickname(RedisHandler.parseString(res.get(0)));
				dat.setPortrait(RedisHandler.parseString(res.get(1)));
				user.friendList.put(friendList.getFriendList(i).getUsername(), dat);
			}
			
			for(int i=0; i<friendList.getRequireListCount(); i++){
				user.addReqSet.put(friendList.getRequireList(i).getUsername(), friendList.getRequireList(i).toBuilder());
			}
		} catch (InvalidProtocolBufferException e) {
		}
		return user;
	}
	public void saveWorldUser(WorldUserData user){
		saveFriend(user);
		
		saveEmail(user);
	}
	public void saveEmail(WorldUserData user){
		EmailListRep.Builder mailListRep = user.toEmailListRep();
		NetManager.redis.set(RedisKey.EmailUserKey(user.username), new String(mailListRep.build().toByteArray()));
	}
	public void saveFriend(WorldUserData user){
		String key = RedisKey.FriendKey(user.username);
		FriendListRep.Builder friendListRep = user.toFriendListRep();
		NetManager.redis.set(key, new String(friendListRep.build().toByteArray()));
	}
	// 创建一个用户
	public UserData createUser(String username, String password){
		UserData user = new UserData();
		user.username = username;
		user.password = password;
		user.nickname = username; // 开始时nickname用username代替
		user.portrait = "1";
		// 初始化玩家数据

		NetManager.redis.sadd(RedisKey.UsernameSet, username);
		saveUser(user);
		NetManager.redis.hset(RedisKey.NicknameTable, user.nickname, username); // nickname -> username
		return user;
	}
	// 查询玩家数据
	public UserData queryUser(String username){
		UserData user = new UserData();
		String userkey = RedisKey.UserKey(username);
		HashMap<String, String> map = (HashMap<String, String>) NetManager.redis.hgetAll(userkey);
		user.username = parseString(map.get(RedisKey.Username));
		user.password = parseString(map.get(RedisKey.Password));
		user.clientID = parseLong(map.get(RedisKey.Clientid));
		user.level = parseInt(map.get(RedisKey.Level));
		user.gold = parseInt(map.get(RedisKey.Gold));
		user.battle = parseString(map.get(RedisKey.Battle));
		user.loginTime = parseLong(map.get(RedisKey.LoginTime)); // 只取不存
		user.logoutTime = parseLong(map.get(RedisKey.LogoutTime));
		user.dailyAwardTime = parseLong(map.get(RedisKey.DailyAwardTime));
		user.winCount = parseInt(map.get(RedisKey.WinCount));
		user.gameCount = parseInt(map.get(RedisKey.GameCount));
		user.nickname = parseString(map.get(RedisKey.Nickname));
		user.portrait = parseString(map.get(RedisKey.Portrait));
		
		return user;
	}
	public void saveUser(UserData user){
		if(user == null){
			return;
		}
		String userkey = RedisKey.UserKey(user.username);
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(RedisKey.Username, user.username);
		map.put(RedisKey.Password, user.password);
		map.put(RedisKey.Level, user.level + "");
		map.put(RedisKey.Gold, user.gold + "");
		map.put(RedisKey.DailyAwardTime, user.dailyAwardTime + "");
		map.put(RedisKey.WinCount, user.winCount + "");
		map.put(RedisKey.GameCount, user.gameCount + "");
		map.put(RedisKey.Nickname, user.nickname);
		map.put(RedisKey.Portrait, user.portrait);
		NetManager.redis.hmset(userkey, map);
	}
	public static byte[] parseBytes(String s){
		if(s == null){
			return "".getBytes();
		}
		return s.getBytes();
	}
	public static String parseString(String s){
		if(s == null){
			return "";
		}
		return s;
	}
	public static int parseInt(String s){
		if(s == null || s.equals("")){
			return 0;
		}
		return Integer.parseInt(s);
	}
	public static long parseLong(String s){
		if(s == null || s.equals("")){
			return 0;
		}
		return Long.parseLong(s);
	}
	public static float parseFloat(String s){
		if(s == null || s.equals("")){
			return 0;
		}
		return Float.parseFloat(s);
	}
	public static double parseDouble(String s){
		if(s == null || s.equals("")){
			return 0;
		}
		return Double.parseDouble(s);
	}
}
