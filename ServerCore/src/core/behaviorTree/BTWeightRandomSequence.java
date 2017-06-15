package core.behaviorTree;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 按权重随机排序，然后队列
 * @author lihebin
 *
 */
public class BTWeightRandomSequence extends BTNode {

	public BTWeightRandomSequence(HashMap<String, String> args,
			BehaviorTree tree) {
		super(args, tree);
	}
	
	public boolean execute(){
		ArrayList<BTNode> list = resort();
		for(int i=0; i<list.size(); i++){
			if(list.get(i).execute() == false){
				return false;
			}
		}
		return true;
	}
}
