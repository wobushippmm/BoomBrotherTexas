package core.behaviorTree;

import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.soap.Node;

import org.apache.log4j.Logger;

import core.log.LoggerHelper;

public class BTNode {
	protected static Logger getLog(){
		return LoggerHelper.getLogger();
	}
	
	public float weight = 1;
	public ArrayList<BTNode> nodeList = new ArrayList<BTNode>();
	public HashMap<String, String> args = null;
	public BehaviorTree tree = null;
	
	public BTNode(HashMap<String, String> args, BehaviorTree tree){
		this.args = args;
		this.tree = tree;
	}
	
	public void addNode(BTNode node){
		nodeList.add(node);
	}
	
	public boolean execute(){
		boolean flag = false;
		for(int i=0; i<nodeList.size(); i++){
			flag = nodeList.get(i).execute();
		}
		return flag;
	}
	
	// 按权重随机排序
	public ArrayList<BTNode> resort(){
		ArrayList<BTNode> list = new ArrayList<BTNode>();
		float totalWeight = 0;
		for(int i=0; i<nodeList.size(); i++){
			totalWeight += nodeList.get(i).weight;
			list.add(nodeList.get(i));
		}
		for(int i=0; i<nodeList.size()-1; i++){
			double rand = Math.random() * totalWeight;
			for(int j=i; j<list.size(); j++){
				rand -= list.get(i).weight;
				if(rand <= 0){
					BTNode node = list.get(j);
					list.remove(j);
					list.add(i, node);
				}
			}
		}
		return list;
	}
}
