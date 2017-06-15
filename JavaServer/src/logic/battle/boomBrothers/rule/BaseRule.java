package logic.battle.boomBrothers.rule;

import logic.battle.boomBrothers.entity.BaseEntity;

public class BaseRule {
	public int id = 0;
	public int type = 0;
	
	protected boolean _isInvalid = false; // 失效
	protected boolean _takeEffect = false; // 生效
	protected BaseEntity entity = null;
	
	public BaseRule(BaseEntity entity){
		this.entity = entity;
	}
	// 是否有效
	public boolean isInvalid() {
		return _isInvalid;
	}
	public void setInvalid() {
		_isInvalid = true;
	}
	// 是否已经生效
	public boolean isTakeEffect(){
		return _takeEffect;
	}
	public void setTakeEffect(){
		_takeEffect = true;
	}
	// 更新
	public void update() {
		// 执行一次失效
		_isInvalid = true;
	}
	// 当加入时
	public void added(){
		// 加入后生效
		_takeEffect = true;
	}
	// 当移除时
	public void removed(){
		
	}
}
