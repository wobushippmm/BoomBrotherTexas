package logic.battle.boomBrothers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.protobuf.GeneratedMessageV3.Builder;

import core.log.LoggerHelper;
import core.net.NetManager;
import core.udp.UdpDataPackage;
import logic.battle.boomBrothers.entity.BaseEntity;
import logic.battle.boomBrothers.entity.BattleEntity;
import logic.battle.boomBrothers.entity.EntityCreator;
import logic.common.data.DataManager;
import logic.common.data.UserData;
import protocol.BoomGameData.BanHeroRep;
import protocol.BoomGameData.BanHeroResultEnm;
import protocol.BoomGameData.BattleActionReq;
import protocol.BoomGameData.BattleFieldInfoRep;
import protocol.BoomGameData.BattleLoadCompletedRep;
import protocol.BoomGameData.BattleLoadCompletedReq;
import protocol.BoomGameData.BattlePlayerTypeEnm;
import protocol.BoomGameData.BattleResultRep;
import protocol.BoomGameData.CallBanHeroRep;
import protocol.BoomGameData.CallSelectHeroRep;
import protocol.BoomGameData.BattleFieldEntityRep;
import protocol.BoomGameData.CampTypeEnm;
import protocol.BoomGameData.HeroOfflineRep;
import protocol.BoomGameData.LeaveBattleRep;
import protocol.BoomGameData.ReadyForFightingRep;
import protocol.BoomGameData.SelectHeroRep;
import protocol.BoomGameData.SelectHeroResultEnm;
import protocol.BoomGameData.StartFightingRep;
import protocol.ProtoUtil;
import protocol.Protocol.RpcPo;

public class BattleField {
	private static Logger log = null;
	private static int idCounter = 0; // id生成计数器
	public static int newID(){
		return ++idCounter;
	}
	
	private int _localCounter = 0; // 本战场id生成技术器
	public int newLocalID(){
		return ++_localCounter;
	}
	
	public int id = 0; // 战场id
	public ArrayList<BattlePlayer> playerList = new ArrayList<BattlePlayer>(); // 所有英雄
	public HashMap<String, BattlePlayer> playerDic = new HashMap<String, BattlePlayer>();
	public int opIndex = 0;
	public HashMap<String, BattlePlayer> team1 = new HashMap<String, BattlePlayer>(); // 队伍一
	public HashMap<String, BattlePlayer> team2 = new HashMap<String, BattlePlayer>(); // 队伍二
	public ArrayList<Integer> banList = new ArrayList<Integer>(); // ban掉的英雄
	public ArrayList<Integer> selectList = new ArrayList<Integer>();
	public int teamSize = 0; // n vs n
	
	public HashSet<String> loadSet = new HashSet<String>(); // 加载完成
	
	// 全部entity表
	public HashMap<Integer, BattleEntity> entityDic = new HashMap<Integer, BattleEntity>();
	public BattleEntity baseCamp1 = null;
	public BattleEntity baseCamp2 = null;
	
	public static final int STATE_START = 0; // 进入战斗，初始化各种数据
	public static final int STATE_BAN = 1; // ban hero
	public static final int STATE_SELECT = 2; // select hero
	public static final int STATE_LOADING = 3;
	public static final int STATE_READY = 4; // 准备出门
	public static final int STATE_FIGHT = 5; // 战斗
	public static final int STATE_OVER = 6; // 结算
	public int state = 0; // 牌局状态
	
	private long gameStartTime = 0;
	public BattleField(int teamSize){
		log = LoggerHelper.getLogger();
		this.teamSize = teamSize;
	}
	
	private long updateTime = System.currentTimeMillis(); // 上次更新时间
	
	private long frameTime = System.currentTimeMillis(); // 上次帧更新时间
	private static final long FRAME_INTERVAL = 1000 / 60; // 帧间隔
	public void onUpdate(){
		long now = System.currentTimeMillis();
		if(now - frameTime < FRAME_INTERVAL){
			return;
		}
		long delay = now - frameTime;
		frameTime = now;
		
		switch(state){
		case STATE_START:
			stateStart();
			break;
		case STATE_BAN:
			stateBan();
			break;
		case STATE_SELECT:
			stateSelect();
			break;
		case STATE_LOADING:
			stateLoading();
			break;
		case STATE_READY:
			stateReady();
			break;
		case STATE_FIGHT:
			stateFight();
			break;
		case STATE_OVER:
			stateOver();
			break;
		}
	}
	private void enterFrame(){
		Iterator<BattleEntity> iter = null;
		BattleEntity entity = null;
		
		iter = entityDic.values().iterator();
		while(iter.hasNext()){
			entity = iter.next();
			entity.runAddedRules();
		}
		iter = entityDic.values().iterator();
		while(iter.hasNext()){
			entity = iter.next();
			entity.runPropertyRules();
		}
		iter = entityDic.values().iterator();
		while(iter.hasNext()){
			entity = iter.next();
			entity.runDeadRules();
		}
		iter = entityDic.values().iterator();
		while(iter.hasNext()){
			entity = iter.next();
			entity.runAIRules();
		}
		iter = entityDic.values().iterator();
		while(iter.hasNext()){
			entity = iter.next();
			entity.runActionRules();
		}
		iter = entityDic.values().iterator();
		while(iter.hasNext()){
			entity = iter.next();
			entity.runMoveRules();
		}
		iter = entityDic.values().iterator();
		while(iter.hasNext()){
			entity = iter.next();
			entity.runDemageRules();
		}
		iter = entityDic.values().iterator();
		while(iter.hasNext()){
			entity = iter.next();
			entity.runDeadRules();
		}
		iter = entityDic.values().iterator();
		while(iter.hasNext()){
			entity = iter.next();
			entity.runRemovedRules();
		}
		// 删除失效的rule
		iter = entityDic.values().iterator();
		while(iter.hasNext()){
			entity = iter.next();
			if(entity.toRemove){
				iter.remove();
				EntityCreator.instance.entityPool.returnObj(entity);
			}
		}
	}
	private void stateStart(){
		long now = System.currentTimeMillis();
		if(now - updateTime < 1000){
			return;
		}
		updateTime = now;
		
		for(int i=0; i<teamSize; i++){
			team1.get(i).index = playerList.size();
			playerList.add(team1.get(i));
			playerDic.put(team1.get(i).username, team1.get(i));
			
			team2.get(i).index = playerList.size();
			playerList.add(team2.get(i));
			playerDic.put(team2.get(i).username, team2.get(i));
		}
		opIndex = 0;
		
		// 开始ban英雄
		sendBanTurn();
		state = STATE_BAN;
	}
	private void stateBan(){
		long now = System.currentTimeMillis();
		if(now - updateTime < 10000){ // 10s
			return;
		}
		updateTime = now;
		
		// 有人未做操作
		BoomBrothersHandler.instance.rematch(this, playerList.get(opIndex).username);
	}
	private void stateSelect(){
		long now = System.currentTimeMillis();
		if(now - updateTime < 10000){ // 10s
			return;
		}
		updateTime = now;
		
		// 有人未做操作
		BoomBrothersHandler.instance.rematch(this, playerList.get(opIndex).username);
	}
	private void stateLoading(){
		long now = System.currentTimeMillis();
		if(now - updateTime < 300000){ // 5分钟
			return;
		}
		updateTime = now;
		
		// 5分钟还加载不完成， 判有人逃跑
		// 结束战斗
		log.warn("有人加载失败");
		gameOver();
	}
	private void stateReady(){
		enterFrame();
		
		long now = System.currentTimeMillis();
		if(now - updateTime < 30000){ // 出门时间30s
			return;
		}
		updateTime = now;
		
		state = STATE_FIGHT;
		StartFightingRep.Builder fightRep = StartFightingRep.newBuilder();
		udpBroadcast(fightRep);
	}
	private void stateFight(){
		enterFrame();
	}
	private void stateOver(){
		
	}
	public void loadComplete(UdpDataPackage data, BattleLoadCompletedReq completeReq){
		if(state != STATE_LOADING){
			return;
		}
		BattlePlayer player = getBattlePlayerByUsername(completeReq.getUsername());
		if(player.addr.equals("")){
			player.addr = data.address;
			player.port = data.port;
		}else if(!player.addr.equals(data.address)){
			// 有外挂？但是不能确定是哪个地址，暂时不处理
			log.warn("检测到可能有人使用外挂！！！！！");
			return;
		}
		// 验证通过，返回消息
		// 可能客户端未收到消息，重新发送
		BattleLoadCompletedRep.Builder completeRep = BattleLoadCompletedRep.newBuilder();
		completeRep.setUsername(player.username);
		completeRep.setPercent(completeReq.getPercent());
		udpBroadcast(completeRep);
		
		if(completeReq.getPercent() >= 100){
			loadSet.add(player.username);
			// 加载完成
			if(loadSet.size() >= playerList.size()){
				state = STATE_READY;
				
				ReadyForFightingRep.Builder readyRep = ReadyForFightingRep.newBuilder();
				udpBroadcast(readyRep);
			}
		}
		// 有外挂？但是不能确定是哪个地址，暂时不处理
		log.warn("检测到可能有人使用外挂！！！！！");
	}
	
	
	public BattlePlayer getBattlePlayerByUsername(String username){
		for(int i=0; i<playerList.size(); i++){
			if(playerList.get(i).username.equals(username)){
				return playerList.get(i);
			}
		}
		return null;
	}
	// 发送ban通知
	public void sendBanTurn(){
		CallBanHeroRep.Builder callRep = CallBanHeroRep.newBuilder();
		callRep.setUsername(playerList.get(opIndex).username);
		broadcast(callRep);
		playerList.get(opIndex).setTurn();
	}
	// 搬掉的英雄
	public void setBanHero(String username, int heroTempID){
		BanHeroRep.Builder banRep = BanHeroRep.newBuilder();
		if(playerList.get(opIndex).username.equals(username)){
			if(!banList.contains(heroTempID)){
				banList.add(heroTempID);
				opIndex++;
				
				banRep.setResult(BanHeroResultEnm.OK_BANHERORESULT);
				banRep.setUsername(username);
				banRep.setHeroTempID(heroTempID);
				broadcast(banRep);

				updateTime = System.currentTimeMillis();
				if(opIndex >= playerList.size()){
					state = STATE_SELECT;
				}else{
					sendBanTurn();
				}
				return;
			}
		}
		// 操作失败
		banRep.setResult(BanHeroResultEnm.FAIL_BANHERORESULT);
		playerList.get(opIndex).send(ProtoUtil.packData(banRep));
	}
	// 发送select通知
	public void sendSelectTurn(){
		CallSelectHeroRep.Builder callRep = CallSelectHeroRep.newBuilder();
		callRep.setUsername(playerList.get(opIndex).username);
		broadcast(callRep);
		playerList.get(opIndex).setTurn();
	}
	// 选择的英雄
	public void setSelectHero(String username, int heroTempID){
		SelectHeroRep.Builder selectRep = SelectHeroRep.newBuilder();
		if(playerList.get(opIndex).username.equals(username)){
			if(!selectList.contains(heroTempID)){
				selectList.add(heroTempID);
				playerList.get(opIndex).heroID = heroTempID;
				opIndex++;
				
				selectRep.setResult(SelectHeroResultEnm.OK_SELECTHERORESULT);
				selectRep.setUsername(username);
				selectRep.setHeroTempID(heroTempID);
				broadcast(selectRep);

				updateTime = System.currentTimeMillis();
				if(opIndex >= playerList.size()){
					initBattleField(); // 初始化战场
					state = STATE_LOADING;
				}else{
					sendSelectTurn();
				}
				return;
			}
		}
		// 操作失败
		selectRep.setResult(SelectHeroResultEnm.FAIL_SELECTHERORESULT);
		playerList.get(opIndex).send(ProtoUtil.packData(selectRep));
	}
	// 初始化战场信息
	public void initBattleField(){
		// 创建玩家
		for(int i=0; i<teamSize; i++){
			BattleEntity entity = EntityCreator.instance.createHero(this, playerList.get(i));
			entity.camp = CampTypeEnm.BOOM_CAMP;
			entityDic.put(entity.id, entity);
			team1.get(i).entity = entity;
		}
		
		for(int i=0; i<teamSize; i++){
			BattleEntity entity = EntityCreator.instance.createHero(this, playerList.get(i));
			entity.camp = CampTypeEnm.RULER_CAMP;
			entityDic.put(entity.id, entity);
			team2.get(i).entity = entity;
		}
		
		// 创建基地
		baseCamp1 = EntityCreator.instance.createBaseCamp(this);
		baseCamp1.camp = CampTypeEnm.BOOM_CAMP;
		entityDic.put(baseCamp1.id, baseCamp1);
		
		baseCamp2 = EntityCreator.instance.createBaseCamp(this);
		baseCamp2.camp = CampTypeEnm.RULER_CAMP;
		broadcast(toBattleFieldEntityRep());
	}
	// 客户端操作
	public void setActionReq(BattleActionReq actReq){
		if(state == STATE_READY || state == STATE_FIGHT){
			playerDic.get(actReq.getUsername()).entity.action = actReq;
		}
	}
	public BattleFieldEntityRep.Builder toBattleFieldEntityRep(){
		BattleFieldEntityRep.Builder rep = BattleFieldEntityRep.newBuilder();
		for(BattleEntity entity : entityDic.values()){
			rep.addEntityList(entity.toBattleEntityDat());
		}
		return rep;
	}
	public void gameOver(){
		BattleResultRep.Builder resultRep = BattleResultRep.newBuilder();
		broadcast(resultRep);
		for(BattlePlayer player : playerList){
			player.entity = null;
			UserData user = DataManager.instance.getUser(player.username);
			if(user != null){
				user.battleFieldID = 0;
			}
		}
		playerList.clear();
		playerDic.clear();
		for(BattleEntity entity : entityDic.values()){
			EntityCreator.instance.entityPool.returnObj(entity);
		}
		entityDic.clear();
		BoomBrothersHandler.instance.gameOver(this);
	}
	public void leaveBattleField(String username){
		setIsOffline(username, true); // 跟掉线一样处理
		
		LeaveBattleRep.Builder leaveRep = LeaveBattleRep.newBuilder();
		leaveRep.setUsername(username);
		broadcast(leaveRep);
	}

	public void addUser(UserData user){
		user.battleFieldID = id;
		BattlePlayer hero = new BattlePlayer();
		hero.username = user.username;
		hero.heroID = 1;
		hero.clientID = user.clientID;
		hero.type = BattlePlayerTypeEnm.PLAYER_BP;
		hero.bf = this;
		if(team1.size() < teamSize){
			team1.put(hero.username, hero);
		}else if(team1.size() < teamSize){
			team2.put(hero.username, hero);
		}
	}
	
	public void addAI(String name){
		BattlePlayer hero = new BattlePlayer();
		hero.username = name;
		hero.heroID = 1;
		hero.clientID = newLocalID();
		hero.type = BattlePlayerTypeEnm.AI_BP;
		hero.bf = this;
		if(team1.size() < teamSize){
			team1.put(hero.username, hero);
		}else if(team1.size() < teamSize){
			team2.put(hero.username, hero);
		}
	}
	
	public BattlePlayer getHeroByUsername(String username){
		if(team1.containsKey(username)){
			return team1.get(username);
		}
		return team2.get(username);
	}
	
	public BattleFieldInfoRep.Builder toBattleFieldInfoRep(){
		// 发送战场信息
		BattleFieldInfoRep.Builder bfInfoRep = BattleFieldInfoRep.newBuilder();
		for(int i=0; i<team1.size(); i++){
			bfInfoRep.addTeam1(team1.get(i).toBattleUserInfoDat());
		}
		for(int i=0; i<team2.size(); i++){
			bfInfoRep.addTeam1(team2.get(i).toBattleUserInfoDat());
		}
		return bfInfoRep;
	}
	
	public void setIsOffline(String username, boolean value){
		getHeroByUsername(username).isOffline = value;
		HeroOfflineRep.Builder offlineRep = HeroOfflineRep.newBuilder();
		offlineRep.setUsername(username);
		// 如果全部掉线了，gameover
		for(int i=0; i<team1.size(); i++){
			if(!team1.get(i).isOffline){
				return;
			}
		}
		for(int i=0; i<team2.size(); i++){
			if(!team2.get(i).isOffline){
				return;
			}
		}
		state = STATE_OVER;
	}

	public void udpBroadcast(Builder<?> anyPo){
		RpcPo.Builder rpcPo = ProtoUtil.packData(anyPo);
		for(BattlePlayer hero : team1.values()){
			hero.udpSend(rpcPo);
		}
		for(BattlePlayer hero : team1.values()){
			hero.udpSend(rpcPo);
		}
	}
	
	public void broadcast(Builder<?> anyPo){
		RpcPo.Builder rpcPo = ProtoUtil.packData(anyPo);
		for(BattlePlayer hero : team1.values()){
			hero.send(rpcPo);
		}
		for(BattlePlayer hero : team1.values()){
			hero.send(rpcPo);
		}
	}
}
