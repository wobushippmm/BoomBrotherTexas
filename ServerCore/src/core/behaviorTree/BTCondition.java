package core.behaviorTree;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;

// 条件基类
public class BTCondition extends BTNode {
	public BTNode condition = null;
	
	public BTCondition(String cond, HashMap<String, String> args, BehaviorTree tree){
		super(args, tree);
		try {
			Class<? extends BTCondition> cls = (Class<? extends BTCondition>) Class.forName(tree.classPath + ".conditions." + cond);
			condition = cls.getConstructor(new Class[]{(new HashMap<String, String>()).getClass(), BehaviorTree.class}).newInstance(args, tree);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			getLog().error(cond, e);
		}
	}
	
	public boolean execute(){
		if(condition != null){
			return condition.execute();
		}
		return true;
	}
}
