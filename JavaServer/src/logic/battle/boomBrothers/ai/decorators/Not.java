package logic.battle.boomBrothers.ai.decorators;

import java.util.HashMap;

import core.behaviorTree.BTNode;
import core.behaviorTree.BehaviorTree;

/**
 * child结果的非
 * @author lihebin
 *
 */
public class Not extends BTNode {

	public Not(HashMap<String, String> args, BehaviorTree tree) {
		super(args, tree);
	}

	public boolean execute(){
		if(nodeList.size() > 0){
			return !nodeList.get(0).execute();
		}
		return false;
	}
}
