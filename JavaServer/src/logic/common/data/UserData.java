package logic.common.data;

import protocol.GameData.UserDat;
import protocol.TexasGameData.TableModeEnm;


// 用户信息
public class UserData {
	public String username = "";
	public String password = "";
	public long clientID = 0; // 客户端的校验数字，每次登录重新生成
	public int level = 0; // 等级
	public int gold = 10000; // 金币
	public String scene = ""; // 战斗服
	public String battle = ""; // 战斗服
	public String gateway = ""; 
	public long loginTime = 0;
	public long logoutTime = 0;
	public long dailyAwardTime = 0;
	public int winCount = 0; // 胜场
	public int gameCount = 0; // 总次数
	public String nickname = ""; // 昵称
	public String portrait = ""; // 头像
	
	//////////////////// 德州 //////////////
	public int tableID = 0; // 牌局id，battle服有用
	public TableModeEnm mode = TableModeEnm.UNDEFINE_TABLEMODE;
	
	//////////////////// 爆炸兄弟 ////////////////////////////
	public int battleFieldID = 0; // 战场id
	// 转换到UserPo
	public UserDat.Builder toUserDat(){
		UserDat.Builder userBuilder = UserDat.newBuilder();
		userBuilder.setUsername(username);
		userBuilder.setCid(clientID);
		userBuilder.setLevel(level);
		userBuilder.setGold(gold);
		userBuilder.setGateway(gateway);
		userBuilder.setScene(scene);
		userBuilder.setBattle(battle);
		userBuilder.setWinCount(winCount);
		userBuilder.setGameCount(gameCount);
		userBuilder.setNickname(nickname);
		userBuilder.setPortrait(portrait);
		return userBuilder;
	}
	
	public void fromUserPo(UserDat po){
		username = po.getUsername();
		clientID = po.getCid();
		level = po.getLevel();
		gold = po.getGold();
		scene = po.getScene();
		battle = po.getBattle();
		gateway = po.getGateway();
		winCount = po.getWinCount();
		gameCount = po.getGameCount();
		nickname = po.getNickname();
		portrait = po.getPortrait();
	}
}
