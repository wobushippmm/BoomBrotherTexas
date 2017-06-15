package logic.battle.texasHoldem.ai.actions;

import java.util.HashMap;

import core.behaviorTree.BTNode;
import core.behaviorTree.BehaviorTree;
import logic.battle.texasHoldem.ai.TexasHoldemTree;
import protocol.TexasGameData.ActionEnm;

// 加
// bet
public class Raise extends BTNode {

	public Raise(HashMap<String, String> args, BehaviorTree tree) {
		super(args, tree);
	}

	public boolean execute(){
		TexasHoldemTree thTree = (TexasHoldemTree) tree;
		thTree.table.onActionReq(thTree.seat.username, ActionEnm.RAISE, 
				(int)((thTree.seat.gold + thTree.seat.bet - thTree.table.currBet) * 0.5 + 
						thTree.table.currBet - thTree.seat.bet));
		getLog().info("AI " + thTree.seat.username + " RAISE ");
		return true;
	}
}
