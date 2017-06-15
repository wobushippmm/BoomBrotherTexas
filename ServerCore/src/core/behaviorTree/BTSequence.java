package core.behaviorTree;

import java.util.HashMap;

/**
 * 一个False，中断并返回False
 * @author lihebin
 *
 */
public class BTSequence extends BTNode {

	public BTSequence(HashMap<String, String> args, BehaviorTree tree) {
		super(args, tree);
	}

	public boolean execute(){
		for(int i=0; i<nodeList.size(); i++){
			if(nodeList.get(i).execute() == false){
				return false;
			}
		}
		return true;
	}
}
