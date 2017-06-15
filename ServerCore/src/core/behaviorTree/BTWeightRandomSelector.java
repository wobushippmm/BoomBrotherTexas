package core.behaviorTree;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 按权重随机排序，然后选择
 * @author lihebin
 *
 */
public class BTWeightRandomSelector extends BTNode {

	public BTWeightRandomSelector(HashMap<String, String> args,
			BehaviorTree tree) {
		super(args, tree);
	}

	public boolean execute(){
		ArrayList<BTNode> list = resort();
		for(int i=0; i<list.size(); i++){
			if(list.get(i).execute()){
				return true;
			}
		}
		return false;
	}
}
