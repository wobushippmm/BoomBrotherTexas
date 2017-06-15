package core.behaviorTree;

import java.util.HashMap;

/**
 * 至少n个True时，返回True
 * 
 * 参数 
 * Counter 默认1
 */
public class BTParallelHybird extends BTNode {

	public BTParallelHybird(HashMap<String, String> args, BehaviorTree tree) {
		super(args, tree);
	}
	
	public boolean execute(){
		int num = 0;
		for(int i=0; i<nodeList.size(); i++){
			if(nodeList.get(i).execute()){
				num += 1;
			}
		}
		if(args.containsKey("Counter")){
			if(num >= Integer.parseInt(args.get("Counter"))){
				return true;
			}
		}else if(num >= 1){
			return true;
		}
		return false;
	}
}
