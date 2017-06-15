package logic.battle.boomBrothers.entity;

import java.util.ArrayList;
import java.util.LinkedList;

import core.pool.PoolItem;
import protocol.BoomGameData.BattleActionReq;
import protocol.BoomGameData.BattleEntityDat;
import protocol.BoomGameData.BattleEntityTypeEnm;
import protocol.BoomGameData.CampTypeEnm;
import logic.battle.boomBrothers.BattleField;
import logic.battle.boomBrothers.BattlePlayer;

// 战场对象，所有战场中的东西
public class BattleEntity extends BaseEntity {
	public boolean toRemove = false; // 移除
	
	public BattleField bf = null;
	public BattlePlayer player = null;
	public CampTypeEnm camp = CampTypeEnm.NEUTRAL_CAMP; // 阵营
	public int id = 0;
	public BattleEntityTypeEnm type = BattleEntityTypeEnm.UNDEFINE_BATTLEENTITYTYP;
	
	// 行动请求
	public BattleActionReq action = null;
	
	// 死亡相关变量
	public long deadTime = Long.MAX_VALUE; // 死亡时间
	public int deadCount = 0;
	public int deadCountTemp = 0; // 临时变量，可能有复活
	public int resurrectionDelay = 0; // 复活时间
	
	public int radius = 0; // 半径
	public int x = 0;
	public int y = 0;
	//////////////// 属性 ///////////////
	public String heroName = "";
	public int level = 0;
	public int exp = 0;
	public int gold = 0; // 钱
	public int hp = 0;
	public int hpMax = 0;
	public int mp = 0;
	public int mpMax = 0;
	public int speed = 0;
	public int attack = 0;
	public int attackDefense = 0;
	public int attackSpeed = 0;
	public int attackShield = 0;
	public int magic = 0;
	public int magicDefense = 0;
	public int magicCoolDown = 0;
	public int magicShield = 0;
	public int critRate = 0;
	public int critDemage = 0;
	public int critShield = 0;
	public int realDemage = 0;
	
	@Override
	public void init() {
		super.init();
		toRemove = false;
		camp = CampTypeEnm.NEUTRAL_CAMP;
		id = 0;
	}

	@Override
	public void clear() {
		super.clear();
		bf = null;
	}
	
	public void setPosition(int x, int y){
		this.x = x;
		this.y = y;
		
		// 分区域处理
	}
	
	public BattleEntityDat.Builder toBattleEntityDat(){
		BattleEntityDat.Builder entityDat = BattleEntityDat.newBuilder();
		if(player != null){
			entityDat.setUsername(player.username);
		}
		entityDat.setX(x);
		entityDat.setY(y);
		entityDat.setId(id);
		entityDat.setHp(hp);
		entityDat.setHpMax(hpMax);
		entityDat.setSpeed(speed);
		return entityDat;
	}
}
