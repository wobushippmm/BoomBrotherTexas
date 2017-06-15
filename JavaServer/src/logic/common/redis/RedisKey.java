package logic.common.redis;

// redis数据的key
public class RedisKey {
	public static final String IDCounter = "IDCounter"; // id生成的自增计数器
	public static String UserKey(String name){return "user:" + name;} // 用户名
	//public static String ClientID(String name){return "cid:" + name;} // client id
	public static String EmailUserKey(String name){return "email:" + name;} // email key
	public static String FriendKey(String name){return "fri:" + name;} // 好友
	public static String TempGold(String name){return "tgold:" + name;} // 临时gold，用来加金币的临时变量 list
	public static String Recharge(String name){return "charge:" + name;} // 已经充值 list

	public static final String OrderList = "OrderList"; // 订单号->username
	public static final String NicknameTable = "nickname"; // 昵称表 nickname : username
	public static final String UsernameSet = "usernameset";
	public static final String WinRank = "winrank"; // 
	public static final String GoldRank = "goldrank";
	// 用户信息
	public static final String Username = "name";
	public static final String Password = "pw";
	public static final String Clientid = "cid";
	public static final String Battle = "battle";
	public static final String Scene = "scene";
	public static final String Gateway = "gateway";
	public static final String Level = "lv";
	public static final String Gold = "gold";
	public static final String LoginTime = "lgit"; // 登陆时间
	public static final String LogoutTime = "lgot"; // 登出时间
	public static final String DailyAwardTime = "dadt"; // 上次每日奖励时间
	public static final String WinCount = "wcnt"; // 胜利场数
	public static final String GameCount = "gcnt"; // 游戏总场数
	public static final String Nickname = "nick"; // 昵称
	public static final String Portrait = "portr";
	public static final String Freeze = "freeze"; // 冻结到时间，毫秒
}
