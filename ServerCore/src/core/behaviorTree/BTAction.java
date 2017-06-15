package core.behaviorTree;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;

// 行为基类
public class BTAction extends BTNode {
	public BTNode action = null;
	
	public BTAction(String act, HashMap<String, String> args, BehaviorTree tree) {
		super(args, tree);
		try {
			Class<? extends BTAction> cls = (Class<? extends BTAction>) Class.forName(tree.classPath + ".actions." + act);
			action = cls.getConstructor(new Class[]{(new HashMap<String, String>()).getClass(), BehaviorTree.class}).newInstance(args, tree);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			getLog().error(act);
			getLog().error(e);
		}
	}

	public boolean execute(){
		if(action != null){
			return action.execute();
		}
		return true;
	}
}
