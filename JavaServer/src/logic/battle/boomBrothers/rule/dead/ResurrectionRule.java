package logic.battle.boomBrothers.rule.dead;

import logic.battle.boomBrothers.entity.BaseEntity;
import logic.battle.boomBrothers.entity.BattleEntity;
import logic.battle.boomBrothers.rule.BaseRule;

// 英雄复活
public class ResurrectionRule extends BaseRule {

	public ResurrectionRule(BaseEntity entity) {
		super(entity);
	}
	
	public void update() {
		BattleEntity entity = (BattleEntity) this.entity;
		if(entity.deadCountTemp > 0){
			entity.deadCountTemp = 0; // 清除死亡记录
			entity.toRemove = false; // 清除移除标记
			entity.resurrectionDelay = entity.level * 10; // 复活时间
			
			// 发送客户端
		}else if(entity.hp <= 0){
			// 复活
			long now = System.currentTimeMillis();
			if(now - entity.deadTime >= entity.resurrectionDelay){
				entity.hp = entity.hpMax;
				entity.setPosition(100, 100);
				
				// 发送客户端
			}
		}
	}
}
