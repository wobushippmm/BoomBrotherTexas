package logic.battle.boomBrothers.skill;

import java.util.ArrayList;
import java.util.HashMap;

import logic.battle.boomBrothers.entity.BaseEntity;
import logic.battle.boomBrothers.rule.BaseRule;

public class BaseSkill {
	private ArrayList<BaseRule> rules = new ArrayList<BaseRule>();

	protected BaseEntity entity = null;
	
	public int cd = 0; // 毫秒计算
	public BaseSkill(BaseEntity entity){
		this.entity = entity;
	}
	// 添加时被动
	public void added(){
		
	}
	// 触发
	public void triggered(){
		
	}
}
