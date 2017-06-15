package logic.battle.boomBrothers.rule.property;

import protocol.BoomGameData.PropertyUpdateRep;
import logic.battle.boomBrothers.entity.BaseEntity;
import logic.battle.boomBrothers.entity.BattleEntity;
import logic.battle.boomBrothers.rule.BaseRule;

// 将临时属性刷到entity上
public class FlushPropertyRule extends BaseRule {

	public FlushPropertyRule(BaseEntity entity) {
		super(entity);
	}
	
	public void update(){
		BattleEntity entity = (BattleEntity) this.entity;
		PropertyUpdateRep.Builder updateRep = PropertyUpdateRep.newBuilder();
		
		if(entity.deadCountTemp != 0){
			entity.deadCount += entity.deadCountTemp;
			entity.deadCountTemp = 0;
			
		}
		
		// 属性变化通知客户端
	}
}
