package logic.battle.boomBrothers.ai.actions;

import java.util.HashMap;

import core.behaviorTree.BTNode;
import core.behaviorTree.BehaviorTree;

public class Follow extends BTNode {
	public Follow(HashMap<String, String> args, BehaviorTree tree) {
		super(args, tree);
	}

	public boolean execute(){
		getLog().info("Follow");
		return true;
	}
}
