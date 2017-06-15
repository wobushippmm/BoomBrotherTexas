package core.behaviorTree;

import java.util.HashMap;

/**
 * 一个True返回True
 * @author lihebin
 *
 */
public class BTParallelSequence extends BTNode {

	public BTParallelSequence(HashMap<String, String> args, BehaviorTree tree) {
		super(args, tree);
	}

	public boolean execute(){
		boolean flag = false;
		for(int i=0; i<nodeList.size(); i++){
			if(nodeList.get(i).execute()){
				flag = true;
			}
		}
		return flag;
	}
}
