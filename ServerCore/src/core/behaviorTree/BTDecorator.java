package core.behaviorTree;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;

// 修饰基类，对child返回结果做修饰
public class BTDecorator extends BTNode {
	public BTNode decorator = null;
	
	public BTDecorator(String dec, HashMap<String, String> args, BehaviorTree tree){
		super(args, tree);
		try {
			Class<? extends BTDecorator> cls = (Class<? extends BTDecorator>) Class.forName(tree.classPath + ".decorators." + dec);
			decorator = cls.getConstructor(new Class[]{(new HashMap<String, String>()).getClass(), BehaviorTree.class}).newInstance(args, tree);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			getLog().error(dec, e);
		}
	}
	
	public boolean execute(){
		if(decorator != null){
			return decorator.execute();
		}
		return true;
	}
}
