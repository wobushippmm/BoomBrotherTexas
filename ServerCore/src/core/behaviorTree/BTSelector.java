package core.behaviorTree;

import java.util.HashMap;

/**
 * 
 * 一个True，中断并返回True
 * @author lihebin
 *
 */
public class BTSelector extends BTNode {

	public BTSelector(HashMap<String, String> args, BehaviorTree tree) {
		super(args, tree);
	}

	public boolean execute(){
		for(int i=0; i<nodeList.size(); i++){
			if(nodeList.get(i).execute()){
				return true;
			}
		}
		return false;
	}
}
