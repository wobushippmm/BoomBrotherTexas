package logic.battle.boomBrothers.equipment;

import java.util.ArrayList;

import logic.battle.boomBrothers.entity.BaseEntity;
import logic.battle.boomBrothers.rule.BaseRule;

public class BaseEquip {
	private ArrayList<BaseRule> rules = new ArrayList<BaseRule>();
	
	protected BaseEntity entity = null;
	
	public int cd = 0; // 毫秒计算
	public BaseEquip(BaseEntity entity){
		this.entity = entity;
	}
	
	// 添加时被动
	public void added(){
		
	}
	// 删除时被动
	public void removed(){
		
	}
	// 触发
	public void triggered(){
		
	}
}
