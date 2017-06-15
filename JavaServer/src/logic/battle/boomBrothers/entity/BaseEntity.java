package logic.battle.boomBrothers.entity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import core.pool.PoolItem;
import logic.battle.boomBrothers.rule.BaseRule;

public class BaseEntity extends PoolItem {
	public int id = 0;
	
	public LinkedList<BaseRule> deadRules = new LinkedList<BaseRule>(); // 控制死亡，复活
	public HashMap<Integer, LinkedList<BaseRule>> propertyRules = // key 是类型
			new HashMap<Integer, LinkedList<BaseRule>>(); // 控制属性变化，buff，debuff
	public LinkedList<BaseRule> aiRules = new LinkedList<BaseRule>(); // 控制ai
	public LinkedList<BaseRule> actionRules = new LinkedList<BaseRule>(); // 控制行动
	public LinkedList<BaseRule> moveRules = new LinkedList<BaseRule>(); // 控制移动
	public LinkedList<BaseRule> demageRules = new LinkedList<BaseRule>(); // 控制伤害结算
	public LinkedList<BaseRule> defenseRules = new LinkedList<BaseRule>(); // 防御结算
	
	// 添加时逻辑
	public void runAddedRules(){
		runAddedRules(deadRules);
		for(LinkedList<BaseRule> list : propertyRules.values()){
			runAddedRules(list);
		}
		runAddedRules(aiRules);
		runAddedRules(actionRules);
		runAddedRules(moveRules);
		runAddedRules(demageRules);
	}
	
	// 删除时逻辑
	public void runRemovedRules(){
		runRemovedRules(deadRules);
		for(LinkedList<BaseRule> list : propertyRules.values()){
			runRemovedRules(list);
		}
		runRemovedRules(aiRules);
		runRemovedRules(actionRules);
		runRemovedRules(moveRules);
		runRemovedRules(demageRules);
	}
	// 属性修改逻辑，扣血，加血
	public void runPropertyRules(){
		for(LinkedList<BaseRule> list : propertyRules.values()){
			runFirstRules(list);
		}
	}
	// 死亡逻辑
	public void runDeadRules(){
		runAllRules(deadRules);
	}
	// ai逻辑
	public void runAIRules(){
		runFirstRules(aiRules);
	}
	// 操作逻辑
	public void runActionRules(){
		runFirstRules(actionRules);
	}
	// 移动逻辑
	public void runMoveRules(){
		runFirstRules(moveRules);
	}
	// 伤害逻辑
	public void runDemageRules(){
		runAllRules(demageRules);
	}
	// 防御逻辑
	public void runDefenseFules(){
		runAllRules(defenseRules);
	}
	
	// 执行第一个
	private void runFirstRules(LinkedList<BaseRule> list){
		for(int i=0; i < list.size(); i++){
			if(list.get(i).isTakeEffect()){
				list.get(0).update();
				return;
			}
		}
	}
	// 执行全部
	private void runAllRules(LinkedList<BaseRule> list){
		for(int i=0; i<list.size(); i++){
			if(list.get(i).isTakeEffect()){
				list.get(i).update();
			}
		}
	}
	private void runAddedRules(LinkedList<BaseRule> list){
		for(int i=0; i<list.size(); i++){
			if(!list.get(i).isTakeEffect()){
				list.get(i).added();
				list.get(i).setTakeEffect();
			}
		}
	}
	private void runRemovedRules(LinkedList<BaseRule> list){
		Iterator<BaseRule> iter = list.iterator();
		while(iter.hasNext()){
			BaseRule rule = iter.next();
			if(rule.isInvalid()){
				rule.removed();
				iter.remove();
			}
		}
	}
	@Override
	public void init() {
		super.init();
		
	}

	@Override
	public void clear() {
		super.clear();
		deadRules.clear();
		propertyRules.clear();
		aiRules.clear();
		actionRules.clear();
		moveRules.clear();
		demageRules.clear();
	}
}
