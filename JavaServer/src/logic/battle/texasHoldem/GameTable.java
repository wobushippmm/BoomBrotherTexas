package logic.battle.texasHoldem;

import java.util.ArrayList;
import java.util.List;

import logic.battle.texasHoldem.ai.TexasHoldemTree;
import logic.common.Sender;
import logic.common.data.UserData;

import org.apache.log4j.Logger;

import com.google.protobuf.GeneratedMessageV3.Builder;

import core.log.LoggerHelper;
import protocol.TexasGameData.ActionEnm;
import protocol.TexasGameData.ActionRep;
import protocol.TexasGameData.ActionReq;
import protocol.TexasGameData.ActionResultEnm;
import protocol.TexasGameData.CallActionRep;
import protocol.TexasGameData.JoinTableRep;
import protocol.TexasGameData.JoinTableResultEnm;
import protocol.TexasGameData.LeaveTableRep;
import protocol.TexasGameData.LeaveTableReq;
import protocol.TexasGameData.RoundResultRep;
import protocol.TexasGameData.SeatCardDat;
import protocol.TexasGameData.SeatTypeEnm;
import protocol.TexasGameData.SendCardRep;
import protocol.TexasGameData.SitDownSeatInfoRep;
import protocol.TexasGameData.StartRoundRep;
import protocol.TexasGameData.TableInfoDat;
import protocol.TexasGameData.TableInfoRep;
import protocol.TexasGameData.TableModeEnm;
import protocol.TexasGameData.WinnerDat;
import template.TableModeTemplate;
import template.TableModeTemplate.TableModeTemp;
import protocol.Protocol.RpcPo;
import protocol.ProtoUtil;

// 一局游戏的基本信息
public class GameTable {
	private Logger log = LoggerHelper.getLogger();
	
	private static int idCounter = 0; // id生成计数器
	public static int newID(){
		return ++idCounter;
	}
	
	public static final int STATE_START = 0; // 进入牌桌
	public static final int STATE_ACTION = 1; // 押注
	public static final int STATE_SEND_CARD = 2; // 发牌
	public static final int STATE_PICK = 3; // 亮底牌
	
	public static final long ACTION_TIME = 15000; // 操作时间
	
	public int id = 0;
	public ArrayList<GameSeat> seats = null;
	private int state = 0; // 牌局状态
	private long updateTime = System.currentTimeMillis(); // 上次更新时间
	public TableModeTemp modeTemp = null;
	
	private int turn = 0; // 下注者
	private int butten = -1; // 庄家
	public int blinds = 0; // 小盲注,大盲注是小盲注两倍
	public int currBet = 0; // 当前押注
	public int currBetSeat = 0; // 当前注的下注者
	public ArrayList<Integer> cardSet = new ArrayList<Integer>(); // 牌组
	public ArrayList<Integer> pubCards = new ArrayList<Integer>(); // 公共牌
	
	public GameTable(TableModeEnm mode){
		this.modeTemp = TableModeTemplate.instance.TableModeTempDic.get(mode.getNumber());
		this.blinds = modeTemp.blinds;
		
		seats = new ArrayList<GameSeat>();
		for(int i=0; i<modeTemp.seatNum; i++){
			seats.add(new GameSeat(i));
		}
	}
	
	public void onUpdate(){
		switch(state){
			case STATE_START:
				stateStart();
				break;
			case STATE_ACTION:
				stateAction();
				break;
			case STATE_SEND_CARD:
				stateSendCard();
				break;
			case STATE_PICK:
				statePick();
				break;
		}
		for(int i=0; i<seats.size(); i++){
			seats.get(i).update(turn);
		}
	}
	// 洗牌，初始化
	private void stateStart(){
		long now = System.currentTimeMillis();
		if(now - updateTime < 1000){
			return;
		}
		updateTime = now;
		// 新开一局
		initRound();
		
		// 下小盲注
		turn = nextTurn(butten); // 下一个下注者
		betBlinks(blinds);
		// 下大盲注
		turn = nextTurn(turn);
		betBlinks(blinds * 2);
		
		// 盲注的时候就全都allin
		if(isActionOver()){
			state = STATE_SEND_CARD;
		}else{
			// 通知下注
			turn = nextTurn(turn);
			sendActionTurn();
			
			state = STATE_ACTION;
		}
	}
	// 下注状态
	private void stateAction(){
		long now = System.currentTimeMillis();
		if(now - updateTime < ACTION_TIME){ // 下注时间10秒
			return;
		}
		updateTime = now;
		
		// 超时弃牌
		seats.get(turn).action = ActionEnm.FOLD;
		broadcast(seats.get(turn).toActionRep(turn));
		
		if(!isActionOver()){
			turn = nextTurn(turn);
			sendActionTurn();
		}else{// 这一轮下注结束
			state = STATE_SEND_CARD;
		}
	}
	// 发牌状态
	private void stateSendCard(){
		long now = System.currentTimeMillis();
		if(now - updateTime < 1000){ // 1s
			return;
		}
		updateTime = now;
		
		// 本局打牌结束，进入结算
		if(isRoundOver()){
			state = STATE_PICK;
			return;
		}
		
		// 发牌时重置状态
		for(int i=0; i<seats.size(); i++){
			if(seats.get(i).action == ActionEnm.CALL ||
				seats.get(i).action == ActionEnm.CHECK ||
				seats.get(i).action == ActionEnm.RAISE){
				seats.get(i).action = ActionEnm.WAIT;
			}
		}

		currBetSeat = -1;
		// 发牌
		int n = 1;
		if(pubCards.size() == 0){
			n = 3;
		}
		while(n-- > 0){
			pubCards.add(cardSet.remove(0));
			SendCardRep.Builder sendCardRep = SendCardRep.newBuilder();
			sendCardRep.setCard(pubCards.get(pubCards.size() - 1));
			broadcast(sendCardRep);
		}
		if(!isActionOver()){
			// 通知下注
			turn = nextTurn(turn);
			sendActionTurn();
			state = STATE_ACTION;
		}
	}
	// 结算牌局
	private void statePick(){
		long now = System.currentTimeMillis();
		if(now - updateTime < 1000){ // 1s
			return;
		}
		updateTime = now;
		
		ArrayList<Integer> betArr = new ArrayList<>();
		betArr.add(0);
		// 把注额分档
		for(int i=0; i<seats.size(); i++){
			if(!seats.get(i).isOut && seats.get(i).action != ActionEnm.FOLD){
				int j = 1;
				for(; j<betArr.size(); j++){
					if(betArr.get(j) > seats.get(i).bet){
						betArr.add(j, seats.get(i).bet);
						break;
					}else if(betArr.get(j) == seats.get(i).bet){
						break;
					}
				}
				if(j >= betArr.size()){
					betArr.add(j, seats.get(i).bet);
				}
				seats.get(i).pickedCards = PickResult.pick(pubCards, seats.get(i).cards);
				seats.get(i).score = PickResult.score(seats.get(i).pickedCards);
				
				log.info("seat " + i + " score " + seats.get(i).score);
			}
			
		}
		
		RoundResultRep.Builder roundResultRep = RoundResultRep.newBuilder();
		for(int i=1; i<betArr.size(); i++){
			int gold = 0;
			int cb = betArr.get(i) - betArr.get(i-1);
			ArrayList<Integer> win = new ArrayList<Integer>();
			long winScore = 1; // 去掉0
			for(int j=0; j<seats.size(); j++){
				if(seats.get(j).bet >= cb){
					gold += cb;
					seats.get(j).bet -= cb;
					if(seats.get(j).score > winScore){
						win.clear();
						win.add(j);
						winScore = seats.get(j).score;
					}else if(seats.get(j).score == winScore){
						win.add(j);
					}
				}else if(seats.get(j).bet > 0){
					gold += seats.get(j).bet;
					seats.get(j).bet = 0;
				}
			}
			for(int w=0; w<win.size(); w++){
				WinnerDat.Builder winnerDat = WinnerDat.newBuilder();
				winnerDat.setSeat(win.get(w));
				winnerDat.setGold(gold/win.size());
				winnerDat.setBet(betArr.get(i)); // 下注额
				seats.get(win.get(w)).gold += gold/win.size();
				for(int k=0; k<seats.get(win.get(w)).pickedCards.length; k++){
					winnerDat.addCards(seats.get(win.get(w)).pickedCards[k]);
				}
				roundResultRep.addWinners(winnerDat);
			}
		}
		for(int i=0; i<seats.size(); i++){
			if(seats.get(i).gold <= 0){
				seats.get(i).isOut = true; // 出局	
			}
			
			SeatCardDat.Builder seatCardDat = SeatCardDat.newBuilder();
			seatCardDat.setSeat(i);
			for(int k=0; k<seats.get(i).cards.size(); k++){
				seatCardDat.addCards(seats.get(i).cards.get(k));
			}
			roundResultRep.addSeats(seatCardDat);
		}
		broadcast(roundResultRep);
		
		
		if(isGameOver()){
			TexasHoldemHandler.instance.gameOver(this);
		}else{
			state = STATE_START;
			updateTime = System.currentTimeMillis() + betArr.size() * 1000;
		}
	}
	// 开始一局
	private void initRound(){
		for(int i=0; i<seats.size(); i++){
			if(seats.get(i).gold > 0){
				seats.get(i).action = ActionEnm.WAIT; // 重置弃牌标记
				seats.get(i).bet = 0;
				seats.get(i).cards.clear();
				seats.get(i).score = 0;
				seats.get(i).pickedCards = null;
				seats.get(i).isOut = false;
			}else{
				seats.get(i).isOut = true;
				seats.get(i).bet = 0;
			}
		}
		
		shuffle();
		nextButten(); // 下一个庄家
		
		blinds += modeTemp.step; // 小盲注,大盲注是小盲注两倍
		currBet = 0; // 当前押注额
		
		// 发牌
		for(int i=0; i<seats.size(); i++){
			StartRoundRep.Builder startRoundRep = StartRoundRep.newBuilder();
			
			// 发牌
			if(!seats.get(i).isOut){
				seats.get(i).cards.add(cardSet.remove(0));
				seats.get(i).cards.add(cardSet.remove(0));
				
				startRoundRep.addSeatCards(seats.get(i).cards.get(0));
				startRoundRep.addSeatCards(seats.get(i).cards.get(1));
			}
			
			startRoundRep.setButten(butten);
			startRoundRep.setBlinds(blinds);
			
			seats.get(i).send(ProtoUtil.packData(startRoundRep));
		}
	}
	// 发送行动者
	private void sendActionTurn(){
		CallActionRep.Builder callActionRep = CallActionRep.newBuilder();
		callActionRep.setSeat(turn);
		broadcast(callActionRep);
		seats.get(turn).setTurn();
	}
	// 下盲注
	private void betBlinks(int blin){
		GameSeat seat = seats.get(turn);
		
		if(seat.gold <= blin){
			if(seat.gold > currBet){
				currBet = seat.gold;
				currBetSeat = turn;
			}
			seat.bet = seat.gold;
			seat.gold = 0;
			seat.action = ActionEnm.ALLIN;
			broadcast(seat.toActionRep(turn));
		}else{
			currBetSeat = turn;
			currBet = blin;
			seat.gold -= currBet;
			seat.bet = currBet;
			seat.action = ActionEnm.RAISE;
			broadcast(seat.toActionRep(turn));
		}
	}
	// 接受操作
	public void onActionReq(String username, ActionEnm act, int bet){
		int seatIndex = getSeatIndexByUsername(username);
		GameSeat seat = seats.get(seatIndex);
		if(seatIndex == turn){
			boolean validity = false;
			if(act == ActionEnm.CALL){
				if(seat.gold + seat.bet > currBet && currBet > seat.bet){
					seat.gold -= currBet - seat.bet;
					seat.bet = currBet;
					if( seats.get(currBetSeat).action == ActionEnm.ALLIN){
						currBetSeat = turn;
					}
					seat.action = ActionEnm.CALL;
					validity = true;
				}
			}else if(act == ActionEnm.ALLIN){
				if(seat.gold > 0){
					if(seat.bet + seat.gold > currBet){
						currBet = seat.bet + seat.gold;
						currBetSeat = turn;
					}
					seat.bet += seat.gold;
					seat.gold = 0;
					seat.action = ActionEnm.ALLIN;
					validity = true;
				}
			}else if(act == ActionEnm.CHECK){
				if(seat.bet == currBet){
					seat.action = ActionEnm.CHECK;
					validity = true;
					if(currBetSeat == -1){ // 第一个check的做标
						currBetSeat = turn;
					}
				}
			}else if(act == ActionEnm.FOLD){
				seat.action = ActionEnm.FOLD;
				validity = true;
			}else if(act == ActionEnm.RAISE){
				if(seat.gold > bet && seat.bet + bet > currBet){
					currBet = seat.bet + bet;
					currBetSeat = turn;
					seat.bet = currBet;
					seat.gold -= bet;
					seat.action = ActionEnm.RAISE;
					validity = true;
				}
			}
			if(validity){
				broadcast(seat.toActionRep(seatIndex));
				updateTime = System.currentTimeMillis();
				if(!isActionOver()){
					turn = nextTurn(turn);
					sendActionTurn();
				}else{// 这一轮下注结束
					state = STATE_SEND_CARD;
				}
			}else{
				// 操作失败
				ActionRep.Builder actionRep = ActionRep.newBuilder();
				actionRep.setResult(ActionResultEnm.ACTION_INVALIDITY);
				actionRep.setSeat(seatIndex);
				seat.send(ProtoUtil.packData(actionRep));
				log.error("action 操作非法 " + act + " " + bet);
			}
		}else{
			// 操作失败
			ActionRep.Builder actionRep = ActionRep.newBuilder();
			actionRep.setResult(ActionResultEnm.ACTION_INVALIDITY);
			actionRep.setSeat(seatIndex);
			seat.send(ProtoUtil.packData(actionRep));
			log.error("action 没有轮到");
		}
	}
	public int getSeatIndexByUsername(String username){
		for(int i=0; i<seats.size(); i++){
			if(seats.get(i).username.equals(username)){
				return i;
			}
		}
		return -1;
	}
	public GameSeat getSeatByUsername(String username){
		for(int i=0; i<seats.size(); i++){
			if(seats.get(i).username.equals(username)){
				return seats.get(i);
			}
		}
		return null;
	}
	// 洗牌
	private void shuffle(){
		cardSet.clear();
		for(int i=0; i<PickResult.FlowerCount; i++){
			for(int j=1; j<PickResult.PointCount; j++){
				cardSet.add((int) Math.round(Math.random() * cardSet.size()), i * PickResult.Shift + j);
			}
		}
		pubCards.clear();
	}
	// 本轮下注是否结束
	private boolean isActionOver(){
		int nturn = nextTurn(turn);
		if(nturn == -1 || nturn == currBetSeat){
			return true;
		}
		int n = 0; // 还没下注
		int m = 0; // 还没出局
		for(int i=0; i<seats.size(); i++){
			if(!seats.get(i).isOut &&
				seats.get(i).action != ActionEnm.FOLD){
				if(seats.get(i).action != ActionEnm.ALLIN){
					n++;
				}
				m++;
			}
		}
		boolean r = (currBetSeat == -1 && n <= 1) || n == 0 || m <= 1;
		return r;
	}
	// 本局游戏是否结束
	private boolean isRoundOver(){
		if(pubCards.size() >= 5){
			return true;
		}
		int n = 0;
		for(int i=0; i<seats.size(); i++){
			if(!seats.get(i).isOut && seats.get(i).action != ActionEnm.FOLD){
				n++;
			}
		}
		if(n < 2){
			return true;
		}
		return false;
	}
	// 游戏是否结束
	private boolean isGameOver(){
		int n = 0; // 玩家的数量
		int m = 0; // 未出局座位数
		for(int i=0; i<seats.size(); i++){
			// 在线玩家
			if(seats.get(i).type == SeatTypeEnm.PLAYER && !seats.get(i).isOffline){
				n++;
			}
			// 未出局位置
			if(!seats.get(i).username.equals("") && !seats.get(i).isOut){
				m++;
			}
		}
		// 如果有玩家并且未出局人数大于1个，继续游戏
		if(n > 0 && m > 1){
			return false;
		}
		// 如果没有玩家，或者未出局人数少于2，结束
		return true;
	}
	// 下一个庄家
	private int nextButten(){
		butten = (butten + 1) % seats.size(); // 庄家
		int c = 0;
		while(seats.get(butten).isOut){
			butten = (butten + 1) % seats.size(); // 下注者
			if(++c > 100){
				log.error("dead loop");
				break;
			}
		}
		return butten;
	}
	// 下一个行动
	private int nextTurn(int start){
		int t = (start + 1) % seats.size();
		int c = 0;
		while(seats.get(t).isOut 
				|| seats.get(t).action == ActionEnm.ALLIN 
				|| seats.get(t).action == ActionEnm.FOLD){
			t = (t + 1) % seats.size();
			if(++c > seats.size()){
				return -1;
			}
		}
		return t;
	}
	// 离开桌子
	public void onLeaveTableReq(String username){
		int index = getSeatIndexByUsername(username);
		if(index > -1){
			// 主动离开，筹码不能带走
			// 要先广播，不然广播不到玩家自己
			LeaveTableRep.Builder leaveTableBuilder = LeaveTableRep.newBuilder();
			leaveTableBuilder.setSeat(index);
			broadcast(leaveTableBuilder);
			
			GameSeat seat = seats.get(index);
			seat.isOut = true;
			seat.type = SeatTypeEnm.NOBODY;
			seat.username = "";
			
			if(currBetSeat == index){
				currBetSeat = nextTurn(index); // 调到下一个人
			}
			
			if(isActionOver()){
				state = STATE_SEND_CARD;
			}
		}
	}
	public void sitDown(UserData user, boolean isAdd){
		for(int i=0; i<seats.size(); i++){
			if(seats.get(i).type == SeatTypeEnm.NOBODY){
				user.tableID = id;
				user.gameCount += 1;
				seats.get(i).username = user.username;
				seats.get(i).nickname = user.nickname;
				seats.get(i).portrait = user.portrait;
				user.gold -= modeTemp.gold;
				seats.get(i).gold = modeTemp.gold;
				seats.get(i).type = SeatTypeEnm.PLAYER;
				seats.get(i).isOut = false;
				
				if(isAdd){ // 是中途加入的
					seats.get(i).action = ActionEnm.FOLD; // 刚进来先等一局
					seats.get(i).send(ProtoUtil.packData(toTableInfoRep(i)));
					
					SitDownSeatInfoRep.Builder sitRep = SitDownSeatInfoRep.newBuilder();
					sitRep.setSeatIndex(i);
					sitRep.setSeatInfo(seats.get(i).toSeatInfoPo(i));
					broadcast(sitRep);
				}
				return;
			}
		}
		if(isAdd){ // 没有空位
			JoinTableRep.Builder joinTableRep = JoinTableRep.newBuilder();
			joinTableRep.setResult(JoinTableResultEnm.NO_EMPTY_SEAT);
			Sender.sendToClient(user.username, ProtoUtil.packData(joinTableRep));
		}
	}
	public void sitDownAI(String username, int gold){
		for(int i=0; i<seats.size(); i++){
			if(seats.get(i).type == SeatTypeEnm.NOBODY){
				seats.get(i).username = username;
				seats.get(i).nickname = username;
				seats.get(i).portrait = "1";
				seats.get(i).gold = gold;
				seats.get(i).type = SeatTypeEnm.AI;
				seats.get(i).isOut = false;
				try{
					seats.get(i).aiTree = new TexasHoldemTree(username, "ai/texasHoldem/DefaultAI.xml", "logic.battle.texasHoldem.ai", 0);
					seats.get(i).aiTree.table = this;
					seats.get(i).aiTree.seat = seats.get(i);
				}catch(Exception e){
					log.error("AI init", e);
				}
				
				break;
			}
		}
	}
	public void broadcast(Builder<?> b){
		log.info("BattleTable " + b.toString());
		RpcPo.Builder builder = ProtoUtil.packData(b);
		for(int i=0; i<seats.size(); i++){
			seats.get(i).send(builder);
		}
	}
	public TableInfoRep.Builder toTableInfoRep(int seat){
		TableInfoRep.Builder tableInfoRep = TableInfoRep.newBuilder();
		tableInfoRep.setId(id);
		for(int i=0; i<seats.size(); i++){
			tableInfoRep.addSeats(seats.get(i).toSeatInfoPo(seat));
		}
		tableInfoRep.setCurrBet(currBet);
		for(int i=0; i<pubCards.size(); i++){
			tableInfoRep.addPubCards(pubCards.get(i));
		}
		tableInfoRep.setCurrBet(currBet);
		tableInfoRep.setTurn(turn);
		if(state == STATE_ACTION){
			tableInfoRep.setTime(ACTION_TIME + updateTime - System.currentTimeMillis());
		}
		tableInfoRep.setMode(TableModeEnm.forNumber(modeTemp.id));
		return tableInfoRep;
	}
	public TableInfoDat.Builder toTableInfoDat(){
		TableInfoDat.Builder tableInfoDat = TableInfoDat.newBuilder();
		tableInfoDat.setId(id);
		tableInfoDat.setEmptySeat(countEmptySeats());
		tableInfoDat.setMode(TableModeEnm.forNumber(modeTemp.id));
		return tableInfoDat;
	}
	public int countEmptySeats(){
		int n = 0;
		for(int i=0; i<seats.size(); i++){
			if(seats.get(i).type == SeatTypeEnm.NOBODY){
				n++;
			}
		}
		return n;
	}
}
