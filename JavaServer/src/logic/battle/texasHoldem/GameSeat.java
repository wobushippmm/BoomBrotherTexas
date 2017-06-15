package logic.battle.texasHoldem;

import java.util.ArrayList;

import logic.battle.texasHoldem.ai.TexasHoldemTree;
import logic.common.Sender;
import protocol.TexasGameData.ActionEnm;
import protocol.TexasGameData.ActionRep;
import protocol.TexasGameData.ActionResultEnm;
import protocol.TexasGameData.SeatInfoDat;
import protocol.TexasGameData.SeatTypeEnm;
import protocol.Protocol.RpcPo;

// 牌局座位
public class GameSeat {
	public String username = "";
	public SeatTypeEnm type = SeatTypeEnm.NOBODY; // 玩家类型
	public int gold = 0;
	public int index = -1; // 标号
	public String nickname = "";
	public String portrait = "";
	
	public boolean isOffline = false; // 掉线
	public int bet = 0; // 下注额
	public boolean isOut = true; // 出局
	public ActionEnm action = ActionEnm.WAIT; // 行动
	
	public ArrayList<Integer> cards = new ArrayList<Integer>(); // 底牌
	public int[] pickedCards = null; // 最终牌
	public long score = 0;
	
	public TexasHoldemTree aiTree = null;
	
	public GameSeat(int i){
		index = i;
	}
	
	public SeatInfoDat.Builder toSeatInfoPo(int seat){
		SeatInfoDat.Builder bui = SeatInfoDat.newBuilder();
		bui.setUsername(username);
		bui.setGold(gold);
		bui.setAction(action);
		bui.setIsOut(isOut);
		bui.setBet(bet);
		bui.setNickname(nickname);
		bui.setPortrait(portrait);
		bui.setIsOffline(isOffline);
		if(index == seat || seat == -1){ // 如果是当前位置，可以发手牌，或者是非比赛位
			for(int i=0; i<cards.size(); i++){
				bui.addCards(cards.get(i));
			}
		}
		while(bui.getCardsCount() < 2){
			bui.addCards(-1);
		}
		return bui;
	}
	public ActionRep.Builder toActionRep(int seatIndex){
		ActionRep.Builder actionRep = ActionRep.newBuilder();
		actionRep.setResult(ActionResultEnm.OK_ACTIONRESULT);
		actionRep.setSeat(seatIndex);
		actionRep.setGold(gold);
		actionRep.setBet(bet);
		actionRep.setAction(action);
		return actionRep;
	}
	public void send(RpcPo.Builder builder){
		if(type == SeatTypeEnm.PLAYER){
			Sender.sendToClient(username, builder);
		}
	}

	public long requireActionTime = 0;
	public void setTurn(){
		requireActionTime = System.currentTimeMillis();
	}
	
	public void update(int turn){
		// 5s后执行ai
		if(aiTree != null && index == turn && System.currentTimeMillis() - requireActionTime > 5000){
			requireActionTime = Long.MAX_VALUE;
			aiTree.execute();
		}
	}
}
