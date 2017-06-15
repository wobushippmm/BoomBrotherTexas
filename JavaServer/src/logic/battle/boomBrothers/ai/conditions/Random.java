package logic.battle.boomBrothers.ai.conditions;

import java.util.HashMap;

import core.behaviorTree.BTNode;
import core.behaviorTree.BehaviorTree;

/**
 * 随机
 * @author Administrator
 * 参数
 * Rate 默认0.5
 */
public class Random extends BTNode {

	public Random(HashMap<String, String> args, BehaviorTree tree) {
		super(args, tree);
	}

	public boolean execute(){
		if(args.containsKey("rate")){
			if(Math.random() < Double.parseDouble(args.get("Rate"))){
				return true;
			}
		}else{
			if(Math.random() < 0.5d){
				return true;
			}
		}
		return false;
	}
}
