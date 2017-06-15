package logic.battle.texasHoldem.ai;

import core.behaviorTree.BehaviorTree;
import logic.battle.texasHoldem.GameSeat;
import logic.battle.texasHoldem.GameTable;

public class TexasHoldemTree extends BehaviorTree {
	public GameTable table = null;
	public GameSeat seat = null;
	
	public TexasHoldemTree(String playerID, String aiPath, String clsPath, long delayTime) {
		super(playerID, aiPath, clsPath, delayTime);
	}

}
