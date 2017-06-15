package logic.battle.texasHoldem.ai.actions;

import java.util.HashMap;

import core.behaviorTree.BTNode;
import core.behaviorTree.BehaviorTree;
import logic.battle.texasHoldem.ai.TexasHoldemTree;
import protocol.TexasGameData.ActionEnm;

public class Call extends BTNode {
	public Call(HashMap<String, String> args, BehaviorTree tree) {
		super(args, tree);
	}

	public boolean execute(){
		TexasHoldemTree thTree = (TexasHoldemTree) tree;
		thTree.table.onActionReq(thTree.seat.username, ActionEnm.CALL, thTree.table.currBet - thTree.seat.bet);
		getLog().info("AI " + thTree.seat.username + " CALL ");
		return true;
	}
}
