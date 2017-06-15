package core.behaviorTree;

import java.util.HashMap;

import org.dom4j.Document;
import org.dom4j.Element;

/**
 * 
 * 树枝
 * @author lihebin
 * 参数 AIPath 
 */
public class BTBranch extends BTNode {

	public BTBranch(HashMap<String, String> args, BehaviorTree tree) {
		super(args, tree);
		if(args.containsKey("AIPath")){
			Document doc = BehaviorTree.getDic(args.get("AIPath"));
			nodeList.add(new BTNode(new HashMap<String, String>(), tree));
			Element rootElem = doc.getRootElement();
			nodeList.get(0).args.put("Descript", args.get("AIPath") + " Branch");
			BehaviorTree.createNode(rootElem, nodeList.get(0), tree);
		}
	}
	public boolean execute(){
		if(nodeList.size() > 0){
			return nodeList.get(0).execute();
		}
		return true;
	}
}
