package core.behaviorTree;

import java.util.HashMap;

/**
 * 全部True返回True
 * @author lihebin
 *
 */
public class BTParallelSelector extends BTNode {

	public BTParallelSelector(HashMap<String, String> args, BehaviorTree tree) {
		super(args, tree);
	}

	public boolean execute(){
		boolean flag = true;
		for(int i=0; i<nodeList.size(); i++){
			if(nodeList.get(i).execute() == false){
				flag = false;
			}
		}
		return flag;
	}
}
