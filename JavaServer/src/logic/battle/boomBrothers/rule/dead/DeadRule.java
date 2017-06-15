package logic.battle.boomBrothers.rule.dead;

import logic.battle.boomBrothers.entity.BaseEntity;
import logic.battle.boomBrothers.entity.BattleEntity;
import logic.battle.boomBrothers.rule.BaseRule;

// 死亡
public class DeadRule extends BaseRule {

	public DeadRule(BaseEntity entity) {
		super(entity);
	}
	
	public void update() {
		BattleEntity entity = (BattleEntity) this.entity;
		if(entity.hp <= 0 && entity.deadTime == Long.MAX_VALUE){ // 还未标记死亡时间
			entity.deadTime = System.currentTimeMillis(); // 标记死亡时间
			entity.deadCountTemp = 1; // 记死亡一次
			entity.toRemove = true; // 移除
			
			// 发送客户端
		}
	}
}
