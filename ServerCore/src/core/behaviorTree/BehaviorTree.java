package core.behaviorTree;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import core.log.LoggerHelper;

/**
 * 
 * @author lihebin
 * 参数
 * Weight 
 * Class 
 * Descript
 * 
 * demo
 * tree = BehaviorTree(1, "DefaultAI", 0)
 * tree.execute()
 */
public class BehaviorTree {
	public static final HashMap<String, Document> xmlDic = new HashMap<String, Document>();
	protected static Logger getLog(){
		return LoggerHelper.getLogger();
	}
	public String classPath = ""; // ai逻辑代码路径
	public String aiPath = "";
	public String playerID = "";
	public long delayTime = 0;
	public BTNode root = null;
	protected long lastTime = 0;
	public BehaviorTree(String playerID, String aiPath, String clsPath, long delayTime){
		this.classPath = clsPath;
		this.playerID = playerID;
		this.aiPath = aiPath;
		this.delayTime = delayTime;
		
		Document doc = getDic(aiPath);
		root = new BTNode(new HashMap<String, String>(), this);
		Element rootElem = doc.getRootElement();
		root.args.put("Descript", aiPath + " Root");
		createNode(rootElem, root, this);
	}
	
	public boolean execute(){
		long now = System.currentTimeMillis();
		if(now - lastTime < delayTime){
			return false;
		}
		lastTime = now;
		return root.execute();
	}
	
	public static void createNode(Element rootElem, BTNode rootNode, BehaviorTree tree){
		HashMap<String, String> args = new HashMap<String, String>();
		List<Attribute> attrs = rootElem.attributes();
		for(Attribute attr : attrs){
			args.put(attr.getName(), attr.getValue());
		}
		BTNode node = null;
		switch(rootElem.getName()){
		case "Action":
			node = new BTAction(rootElem.attributeValue("Class"), args, tree);
			break;
		case "Condition":
			node = new BTCondition(rootElem.attributeValue("Class"), args, tree);
			break;
		case "Decorator":
			node = new BTDecorator(rootElem.attributeValue("Class"), args, tree);
			break;
		case "ParallelHybird":
			node = new BTParallelHybird(args, tree);
			break;
		case "ParallelSelector":
			node = new BTParallelSelector(args, tree);
			break;
		case "ParallelSequence":
			node = new BTParallelSequence(args, tree);
			break;
		case "Selector":
			node = new BTSelector(args, tree);
			break;
		case "Sequence":
			node = new BTSequence(args, tree);
			break;
		case "WeightRandomSelector":
			node = new BTWeightRandomSelector(args, tree);
			break;
		case "WeightRandomSequence":
			node = new BTWeightRandomSequence(args, tree);
			break;
		default:
			node = new BTNode(args, tree);
			break;
		}
		// 有weight的情况
		try{
			if(!rootElem.attributeValue("Weight").equals("")){
				node.weight = Float.parseFloat(rootElem.attributeValue("Weight"));
			}
		}catch(Exception e){
			// 啥也不干
		}
		rootNode.addNode(node);
		
		Iterator iter = rootElem.elementIterator();
		while(iter.hasNext()){
			Element elem = (Element)iter.next();
			createNode(elem, node, tree);
		}
	}
	
	public static Document getDic(String path){
		if(!xmlDic.containsKey(path)){
			SAXReader reader = new SAXReader();
			try {
				Document doc = reader.read(new File(path));
				xmlDic.put(path, doc);
			} catch (DocumentException e) {
				getLog().error(path, e);
			}
		}
		return xmlDic.get(path);
	}
}
