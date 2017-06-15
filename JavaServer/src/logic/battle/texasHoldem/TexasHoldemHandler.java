package logic.battle.texasHoldem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import logic.common.LogicManager;
import logic.common.Sender;
import logic.common.data.DataManager;
import logic.common.data.UserData;
import logic.common.redis.RedisKey;
import protocol.TexasGameData.ActionReq;
import protocol.TexasGameData.CancelMatchTableRep;
import protocol.TexasGameData.CancelMatchTableReq;
import protocol.GameData.ClientDisconnectRpc;
import protocol.GameData.ExitBattleRep;
import protocol.GameData.ExitBattleReq;
import protocol.GameData.ExitBattleRpc;
import protocol.TexasGameData.GetTableListRep;
import protocol.TexasGameData.GetTableListReq;
import protocol.GameData.JoinBattleRep;
import protocol.GameData.JoinBattleResultEnm;
import protocol.GameData.JoinBattleRpc;
import protocol.TexasGameData.JoinTableRep;
import protocol.TexasGameData.JoinTableReq;
import protocol.TexasGameData.JoinTableResultEnm;
import protocol.TexasGameData.LeaveTableRep;
import protocol.TexasGameData.LeaveTableReq;
import protocol.TexasGameData.MatchTableRep;
import protocol.TexasGameData.MatchTableReq;
import protocol.TexasGameData.MatchTableResultEnm;
import protocol.TexasGameData.TableModeEnm;
import template.TableModeTemplate;
import template.TableModeTemplate.TableModeTemp;
import protocol.GameData.RejoinBattleRpc;
import protocol.GameData.SetGoldCauseEnm;
import protocol.GameData.SetGoldRpc;
import protocol.TexasGameData.SeatInfoDat;
import protocol.TexasGameData.SeatOfflineRep;
import protocol.TexasGameData.SeatOnlineRep;
import protocol.TexasGameData.SeatTypeEnm;
import protocol.GameData.SetServerRpc;
import protocol.TexasGameData.TableInfoDat;
import protocol.TexasGameData.TableInfoRep;
import protocol.GameData.UserDat;
import protocol.ProtoUtil;
import protocol.Protocol.RpcPo;

import org.apache.log4j.Logger;

import com.google.protobuf.GeneratedMessageV3.Builder;
import com.google.protobuf.InvalidProtocolBufferException;

import core.config.Constant;
import core.log.LoggerHelper;
import core.net.ClientThread;
import core.net.DataPackage;
import core.net.NetManager;
import core.net.SocketThread;

// 德州扑克
public class TexasHoldemHandler {
	private Logger log = LoggerHelper.getLogger();
	public static TexasHoldemHandler instance = null;

	// 匹配更新时间
	private long updateTime = 0;
	
	// [username time]
	private HashMap<TableModeEnm, ArrayList<Object[]>> queueDic = new HashMap<TableModeEnm, ArrayList<Object[]>>();
	
	public HashMap<Integer, GameTable> tableDic = new HashMap<Integer, GameTable>();
	
	public TexasHoldemHandler(){
		instance = this;
		
		// 初始化所有模式队列
		for(TableModeTemp modeTemp : TableModeTemplate.instance.TableModeTempDic.values()){
			queueDic.put(TableModeEnm.forNumber(modeTemp.id), new ArrayList<Object[]>());
		}
		
		// 公用接口
		LogicManager.logicThread.setRpc(JoinBattleRpc.class, this);
		LogicManager.logicThread.setRpc(ExitBattleReq.class, this);
		LogicManager.logicThread.setRpc(RejoinBattleRpc.class, this);
		LogicManager.logicThread.setRpc(ClientDisconnectRpc.class, this);
		// 德州接口
		LogicManager.logicThread.setRpc(ActionReq.class, this);
		LogicManager.logicThread.setRpc(GetTableListReq.class, this);
		LogicManager.logicThread.setRpc(LeaveTableReq.class, this);
		LogicManager.logicThread.setRpc(JoinTableReq.class, this);
		LogicManager.logicThread.setRpc(MatchTableReq.class, this);
		LogicManager.logicThread.setRpc(CancelMatchTableReq.class, this);
		
		LogicManager.logicThread.loopLogicFuncs.add(new Object[]{"onUpdate", this});
	}
	public void onClientDisconnectRpc(DataPackage data){
		// 玩家掉线
		// 
		UserData user = DataManager.instance.getUser(data.rpcPo.getClientName());
		if(user != null){
			GameTable table = tableDic.get(user.tableID);
			if(table != null){
				table.getSeatByUsername(user.username).isOffline = true;
				SeatOfflineRep.Builder offlineRep = SeatOfflineRep.newBuilder();
				offlineRep.setSeat(table.getSeatIndexByUsername(user.username));
				table.broadcast(offlineRep);
			}
		}
	}
	// 定时器触发
	// 一直判定为闲
	private int onQueueUpdate(){
		long now = System.currentTimeMillis();
		if(now - updateTime < 1000){ // 每一秒执行一次
			return 0;
		}
		updateTime = now;
		
		for(TableModeEnm mode : queueDic.keySet()){
			ArrayList<Object[]> queue = queueDic.get(mode);
			TableModeTemp modeTemp = TableModeTemplate.instance.TableModeTempDic.get(mode.getNumber());
			int seatNum = modeTemp.seatNum;
			if(queue.size() >= seatNum){ // 人数满
				GameTable table = new GameTable(mode);
				table.id = GameTable.newID();
				tableDic.put(table.id, table);
				int i = 0;
				while(i++ < seatNum){
					Object[] arr = queue.remove(0);
					UserData user = DataManager.instance.userDic.get(arr[0]);
					table.sitDown(user, false);
				}
				table.broadcast(table.toTableInfoRep(-1));
			}else if(queue.size() > 0){ // 人数不满
				GameTable table = new GameTable(mode);
				table.id = GameTable.newID();
				int n = 0;
				if(now - (long)queue.get(0)[1] > 10000){ // 大于1分钟则进入游戏
					while(queue.size() > 0){
						Object[] arr = queue.remove(0);
						UserData user = DataManager.instance.userDic.get(arr[0]);
						table.sitDown(user, false);
						n++;
					}
				}
				if(n > 0){
					tableDic.put(table.id, table);
					if(mode == TableModeEnm.MODE_RICH_5){ // 5人场测试用
						while(n < 3){
							table.sitDownAI("AI" + n++, modeTemp.gold);
						}
					}else{
						while(n < seatNum){
							table.sitDownAI("AI" + n++, modeTemp.gold);
						}
					}
					table.broadcast(table.toTableInfoRep(-1));
				}
			}
		}
		return 0;
	}
	// 退出battle
	public void onExitBattleReq(DataPackage data){
		try {
			ExitBattleReq req = ExitBattleReq.parseFrom(data.rpcPo.getAnyPo());
			UserData user = DataManager.instance.delUser(data.rpcPo.getClientName());
			if(user != null && tableDic.get(user.tableID) == null){
				// 通知客户端
				ExitBattleRep.Builder exitBattleRep = ExitBattleRep.newBuilder();
				// 不能用sender了，user已经删
				LogicManager.logicThread.sendToClient(data.rpcPo.getClientName(), ProtoUtil.packData(exitBattleRep), data.termianl.socketThread);
				
				// 通知scene
				ExitBattleRpc.Builder exitBattleRpc = ExitBattleRpc.newBuilder();
				exitBattleRpc.setUsername(data.rpcPo.getClientName());
				Sender.sendToServer(user.scene, ProtoUtil.packData(exitBattleRpc));
			}
		} catch (InvalidProtocolBufferException e) {
			log.error(e);
		}
	}
	// 离开牌局
	public void onLeaveTableReq(DataPackage data){
		UserData user = DataManager.instance.getUser(data.rpcPo.getClientName());
		if(user != null){
			GameTable gameTable = tableDic.get(user.tableID);
			if(gameTable != null){
				gameTable.onLeaveTableReq(user.username);
				
				// 同步gold到scene
				SetGoldRpc.Builder setGoldRpc = SetGoldRpc.newBuilder();
				setGoldRpc.setUsername(user.username);
				setGoldRpc.setCause(SetGoldCauseEnm.AFTER_BATTLE);
				setGoldRpc.setGold(user.gold);
				Sender.sendToServer(user.scene, ProtoUtil.packData(setGoldRpc));
			}
		}
	}
	// 客户端操作
	public void onActionReq(DataPackage data){
		try {
			ActionReq actionReq = ActionReq.parseFrom(data.rpcPo.getAnyPo());
			UserData user = DataManager.instance.getUser(data.rpcPo.getClientName());
			if(user != null){
				GameTable gameTable = tableDic.get(user.tableID);
				if(gameTable != null){
					gameTable.onActionReq(data.rpcPo.getClientName(), actionReq.getAction(), actionReq.getBet());
				}
			}
		} catch (InvalidProtocolBufferException e) {
			log.error(e);
		}
		
	}
	// 开始游戏
	public void onJoinBattleRpc(DataPackage data){
		JoinBattleRpc rpc;
		try {
			rpc = JoinBattleRpc.parseFrom(data.rpcPo.getAnyPo());
			UserData user = new UserData();
			user.fromUserPo(rpc.getUser());
			DataManager.instance.addUser(user);
			user.battle = NetManager.name;
			
			JoinBattleRep.Builder joinBattleRep = JoinBattleRep.newBuilder();
			joinBattleRep.setResult(JoinBattleResultEnm.OK_JOINBATTLERESULT);
			joinBattleRep.setBattleName(NetManager.name);
			// 注意 Req的协议包才可以保证 data.rpcPo.getClientName()存在值
			Sender.sendToClient(user.username, ProtoUtil.packData(joinBattleRep));
		} catch (InvalidProtocolBufferException e) {
			log.error(e);
		}
		
	}
	
	public int onUpdate(){
		Iterator<GameTable> iter = tableDic.values().iterator();
		// 更新牌局逻辑
		while(iter.hasNext()){
			iter.next().onUpdate();
		}
		return onQueueUpdate();
	}
	
	// 离开桌子
	public void gameOver(GameTable table){
		tableDic.remove(table.id);
		for(int i=0; i<table.seats.size(); i++){
			if(table.seats.get(i).type == SeatTypeEnm.PLAYER){
				UserData user = DataManager.instance.getUser(table.seats.get(i).username);
				if(user != null){
					// 只要赢钱就记胜场
					if(table.seats.get(i).gold > user.gold){
						user.winCount += 1;
					}
					user.gold += table.seats.get(i).gold;
					user.tableID = 0;
					
					// 同步gold到scene
					SetGoldRpc.Builder setGoldRpc = SetGoldRpc.newBuilder();
					setGoldRpc.setUsername(user.username);
					setGoldRpc.setGold(user.gold);
					setGoldRpc.setCause(SetGoldCauseEnm.AFTER_BATTLE);
					Sender.sendToServer(user.scene, ProtoUtil.packData(setGoldRpc));
					
					LeaveTableRep.Builder leaveTableRep = LeaveTableRep.newBuilder();
					leaveTableRep.setSeat(i);
					Sender.sendToClient(user.username, ProtoUtil.packData(leaveTableRep));
				}
			}
		}
	}
	// 获取桌子列表
	public void onGetTableListReq(DataPackage data){
		try {
			GetTableListReq req = GetTableListReq.parseFrom(data.rpcPo.getAnyPo());
			Iterator<GameTable> iter = tableDic.values().iterator();
			int i=0;
			while(i<req.getStartIndex() && iter.hasNext()){
				i++;
				iter.next();
			}
			GetTableListRep.Builder rep = GetTableListRep.newBuilder();
			while(i < req.getStartIndex() + 20 && iter.hasNext()){
				i++;
				GameTable table = iter.next();
				rep.addTableList(table.toTableInfoDat());
			}
			Sender.sendToClient(data.rpcPo.getClientName(), ProtoUtil.packData(rep));
		} catch (InvalidProtocolBufferException e) {
			log.error(e);
		}
		
	}
	public void onJoinTableReq(DataPackage data){
		JoinTableReq req;
		try {
			req = JoinTableReq.parseFrom(data.rpcPo.getAnyPo());
			GameTable table = tableDic.get(req.getId());
			UserData user = DataManager.instance.getUser(data.rpcPo.getClientName());
			if(user != null){
				if(table != null){
					TableModeTemp mode = table.modeTemp;
					if(user.gold < mode.gold){
						JoinTableRep.Builder joinTableRep = JoinTableRep.newBuilder();
						joinTableRep.setResult(JoinTableResultEnm.GOLD_NOT_ENOUGH_JOINTABLERESULT);
						Sender.sendToClient(user.username, ProtoUtil.packData(joinTableRep));
						return;
					}
					
					table.sitDown(user, true);
				}else{
					JoinTableRep.Builder joinTableRep = JoinTableRep.newBuilder();
					joinTableRep.setResult(JoinTableResultEnm.TABLE_DISTROYED);
					Sender.sendToClient(user.username, ProtoUtil.packData(joinTableRep));
				}
			}
		} catch (InvalidProtocolBufferException e) {
			log.error(e);
		}
		
	}
	// 匹配
	public void onMatchTableReq(DataPackage data){
		try {
			UserData user = DataManager.instance.getUser(data.rpcPo.getClientName());
			if(user == null){
				return;
			}
			MatchTableReq req = MatchTableReq.parseFrom(data.rpcPo.getAnyPo());
			ArrayList<Object[]> queue = queueDic.get(req.getMode());;
			if(queue == null){
				return;
			}
			
			TableModeTemp mode = TableModeTemplate.instance.TableModeTempDic.get(req.getMode().getNumber());
			if(user.gold < mode.gold){
				MatchTableRep.Builder rep = MatchTableRep.newBuilder();
				rep.setResult(MatchTableResultEnm.GOLD_NOT_ENOUGH_MATCHTABLERESULT);
				Sender.sendToClient(data.rpcPo.getClientName(), ProtoUtil.packData(rep));
				return;
			}
			
			for(int i=0; i<queue.size(); i++){
				if(queue.get(i)[0].equals(data.rpcPo.getClientName())){
					MatchTableRep.Builder rep = MatchTableRep.newBuilder();
					rep.setResult(MatchTableResultEnm.ALREADY_IN_QUEUE);
					Sender.sendToClient(data.rpcPo.getClientName(), ProtoUtil.packData(rep));
					return;
				}
			}
			queue.add(new Object[]{data.rpcPo.getClientName(), System.currentTimeMillis()});
			user.mode = req.getMode();
			
			MatchTableRep.Builder rep = MatchTableRep.newBuilder();
			rep.setResult(MatchTableResultEnm.OK_MATCHTABLERESULT);
			Sender.sendToClient(data.rpcPo.getClientName(), ProtoUtil.packData(rep));
		} catch (InvalidProtocolBufferException e) {
			log.error(e);
		}
		
	}
	// 取消匹配
	public void onCancelMatchTableReq(DataPackage data){
		try {
			CancelMatchTableReq req = CancelMatchTableReq.parseFrom(data.rpcPo.getAnyPo());
			UserData user = DataManager.instance.getUser(data.rpcPo.getClientName());
			if(user == null){
				return;
			}
			for(ArrayList<Object[]> queue : queueDic.values()){
				for(int i=0; i<queue.size(); i++){
					if(queue.get(i)[0].equals(data.rpcPo.getClientName())){
						queue.remove(i);
						
						CancelMatchTableRep.Builder rep = CancelMatchTableRep.newBuilder();
						Sender.sendToClient(data.rpcPo.getClientName(), ProtoUtil.packData(rep));
						return;
					}
				}
			}
		} catch (InvalidProtocolBufferException e) {
			log.error(e);
		}
	}
	// 断线重连
	public void onRejoinBattleRpc(DataPackage data){
		try {
			RejoinBattleRpc req = RejoinBattleRpc.parseFrom(data.rpcPo.getAnyPo());
			UserData user = DataManager.instance.getUser(req.getUser().getUsername());
			if(user != null){
				GameTable table = tableDic.get(user.tableID);
				if(table != null){
					// 先设置进牌局，再发送牌局信息
					JoinBattleRep.Builder joinBattleRep = JoinBattleRep.newBuilder();
					joinBattleRep.setResult(JoinBattleResultEnm.OK_JOINBATTLERESULT);
					joinBattleRep.setBattleName(NetManager.name);
					Sender.sendToClient(user.username, ProtoUtil.packData(joinBattleRep));
					
					table.getSeatByUsername(user.username).isOffline = false;
					TableInfoRep.Builder tableRep = table.toTableInfoRep(table.getSeatIndexByUsername(user.username));
					tableRep.setIsRejoin(true);
					table.getSeatByUsername(user.username).send(ProtoUtil.packData(tableRep));
					
					SeatOnlineRep.Builder onlineRep = SeatOnlineRep.newBuilder();
					onlineRep.setSeat(table.getSeatIndexByUsername(user.username));
					table.broadcast(onlineRep);
					return;
				}
			}
			// 通知scene客户端已经退出战斗服
			ExitBattleRpc.Builder exitBattleRpc = ExitBattleRpc.newBuilder();
			exitBattleRpc.setUsername(req.getUser().getUsername());
			Sender.sendToServer(req.getUser().getScene(), ProtoUtil.packData(exitBattleRpc));
		} catch (InvalidProtocolBufferException e) {
			log.error(e);
		}
		
	}
	
}
