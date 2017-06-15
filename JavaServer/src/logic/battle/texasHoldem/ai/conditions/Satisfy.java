package logic.battle.texasHoldem.ai.conditions;

import java.util.HashMap;

import core.behaviorTree.BTNode;
import core.behaviorTree.BehaviorTree;
import logic.battle.texasHoldem.ai.TexasHoldemTree;

// 满足行动条件
// act 行动类型
public class Satisfy extends BTNode {

	public Satisfy(HashMap<String, String> args, BehaviorTree tree) {
		super(args, tree);
	}
	
	public boolean execute(){
		if(args.containsKey("Act")){
			TexasHoldemTree thTree = (TexasHoldemTree) tree;
			if(thTree.seat.gold <= 0){
				return false;
			}
			switch(args.get("Act")){
			case "Check":
				if(thTree.table.currBet == thTree.seat.bet){
					return true;
				}
				break;
			case "Call":
				if(thTree.table.currBet < thTree.seat.bet + thTree.seat.gold
						&& thTree.table.currBet > thTree.seat.bet){ // 第一个叫牌时
					return true;
				}
				break;
			case "Raise":
				if(thTree.table.currBet < thTree.seat.bet + thTree.seat.gold - 1){
					return true;
				}
				break;
			case "Allin":
				if(thTree.seat.gold > 0){
					return true;
				}
				break;
			}
		}
		return false;
	}
}
