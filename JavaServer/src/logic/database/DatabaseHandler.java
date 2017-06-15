package logic.database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.GeneratedMessageV3.Builder;

import protocol.GameData.EmailDat;
import protocol.GameData.EmailRpc;
import protocol.GameData.GetEmailListReq;
import protocol.ProtoUtil;
import protocol.GameData.SendEmailReq;
import protocol.GameData.SetEmailReadReq;
import protocol.GameData.GetRankListRep;
import protocol.GameData.GetRankListReq;
import protocol.GameData.RankTypeEnm;
import protocol.GameData.JoinBattleRpc;
import protocol.GameData.LogRpc;
import protocol.GameData.RankItemDat;
import core.config.Constant;
import core.log.LoggerHelper;
import core.net.DataPackage;
import core.net.NetManager;
import core.net.SocketThread;
import logic.common.LogicManager;
import logic.common.Sender;
import logic.common.redis.RedisHandler;
import logic.common.redis.RedisKey;

public class DatabaseHandler {
	private Logger log = LoggerHelper.getLogger();
	public static DatabaseHandler instance = null;
	
	// 暂时直接存在内存中
	public ArrayList<RankItemDat.Builder> goldRank = new ArrayList<RankItemDat.Builder>();
	public ArrayList<RankItemDat.Builder> winRank = new ArrayList<RankItemDat.Builder>();
	private long goldRankTime = System.currentTimeMillis() - 3500000; // 启动后1分钟排序
	private long winRankTime = System.currentTimeMillis() - 3400000;
	private HashMap<Integer, GetRankListRep.Builder> goldRankRepDic = new HashMap<Integer, GetRankListRep.Builder>();
	private HashMap<Integer, GetRankListRep.Builder> winRankRepDic = new HashMap<Integer, GetRankListRep.Builder>();

	
	public DatabaseHandler(){
		instance = this;

		LogicManager.logicThread.loopLogicFuncs.add(new Object[]{"onUpdate", this});
		
		LogicManager.logicThread.setRpc(GetRankListReq.class, this);
		
	}

	public GetRankListRep.Builder toGoldRankRep(int startIndex){
		if(!goldRankRepDic.containsKey(startIndex)){
			GetRankListRep.Builder rankRep = GetRankListRep.newBuilder();
			rankRep.setStartIndex(startIndex);
			rankRep.setType(RankTypeEnm.GOLD_RANK);
			goldRankRepDic.put(startIndex, rankRep);
			for(int i=startIndex; i<goldRank.size() && i<startIndex + 10; i++){
				rankRep.addRankItems(goldRank.get(i));
			}
		}
		return goldRankRepDic.get(startIndex);
	}
	public GetRankListRep.Builder toWinRankRep(int startIndex){
		if(!winRankRepDic.containsKey(startIndex)){
			GetRankListRep.Builder rankRep = GetRankListRep.newBuilder();
			rankRep.setStartIndex(startIndex);
			rankRep.setType(RankTypeEnm.WIN_RANK);
			winRankRepDic.put(startIndex, rankRep);
			for(int i=startIndex; i<winRank.size() && i<startIndex + 10; i++){
				rankRep.addRankItems(winRank.get(i));
			}
		}
		return winRankRepDic.get(startIndex);
	}
	public void onGetRankListReq(DataPackage data){
		GetRankListReq req;
		try {
			req = GetRankListReq.parseFrom(data.rpcPo.getAnyPo());
			
			if(req.getType() == RankTypeEnm.GOLD_RANK){
				
				LogicManager.logicThread.sendToClient(data.rpcPo.getClientName(), ProtoUtil.packData(toGoldRankRep(req.getStartIndex())), data.termianl.socketThread);
			}else if(req.getType() == RankTypeEnm.WIN_RANK){
				
				LogicManager.logicThread.sendToClient(data.rpcPo.getClientName(), ProtoUtil.packData(toWinRankRep(req.getStartIndex())), data.termianl.socketThread);
			}
		} catch (InvalidProtocolBufferException e) {
			log.error(e);
		}
	}
	
	public int onUpdate(){
		sortGoldRank();
		sortWinRank(); // 获胜排行
		return 0;
	}

	private void sortGoldRank(){
		long now = System.currentTimeMillis();
		if(now - goldRankTime < 3600000){ // 一小时排一次
			return;
		}
		goldRankTime = now;
		
		goldRank.clear();
		goldRankRepDic.clear();;
		
		int rankSize = 100;
		// 假设玩家数量不多情况下，一次性取出全部玩家名列表
		Set<String> names = NetManager.redis.smembers(RedisKey.UsernameSet);
		for(String username : names){
			String key = RedisKey.UserKey(username);
			try{
				List<String> res = NetManager.redis.hmget(key, RedisKey.Gold, RedisKey.Nickname, RedisKey.Portrait);
				int gold = RedisHandler.parseInt(res.get(0));
				int i = 0;
				while(i < rankSize && i < goldRank.size()){ // 排名前一百
					if((int)goldRank.get(i).getGold() < gold){
						break;
					}
					i++;
				}
				if(i < rankSize){ 
					RankItemDat.Builder dat = RankItemDat.newBuilder();
					dat.setUsername(username);
					dat.setGold(gold);
					dat.setNickname(RedisHandler.parseString(res.get(1)));
					dat.setPortrait(RedisHandler.parseString(res.get(2)));
					goldRank.add(i, dat);
				}
				while(goldRank.size() > rankSize){
					goldRank.remove(goldRank.size() - 1);
				}
			}catch(Exception e){
			}
		}
	}
	
	private void sortWinRank(){
		long now = System.currentTimeMillis();
		if(now - winRankTime < 3600000){
			return;
		}
		winRankTime = now;
		
		winRank.clear();
		winRankRepDic.clear();;
		
		int rankSize = 100;
		// 假设玩家数量不多情况下，一次性取出全部玩家名列表
		Set<String> names = NetManager.redis.smembers(RedisKey.UsernameSet);
		for(String username : names){
			String key = RedisKey.UserKey(username);
			try{
				List<String> res = NetManager.redis.hmget(key, RedisKey.WinCount, RedisKey.GameCount, RedisKey.Nickname, RedisKey.Portrait);
				int winCount = RedisHandler.parseInt(res.get(0));
				int gameCount = RedisHandler.parseInt(res.get(1));
				int score = winCount * 3 - (gameCount - winCount); // 积分 胜一场积3分，属一场扣1分
				int i = 0;
				while(i < rankSize && i < winRank.size()){ // 排名前一百
					if((int)winRank.get(i).getScore() < score){
						break;
					}
					i++;
				}
				if(i < rankSize){ 
					RankItemDat.Builder dat = RankItemDat.newBuilder();
					dat.setUsername(username);
					dat.setScore(score);
					dat.setWinCount(winCount);
					dat.setGameCount(gameCount);
					dat.setNickname(RedisHandler.parseString(res.get(2)));
					dat.setPortrait(RedisHandler.parseString(res.get(3)));
					winRank.add(i, dat);
				}
				while(winRank.size() > rankSize){
					winRank.remove(winRank.size() - 1);
				}
			}catch(Exception e){
			}
		}
	}
	

	public void broadcastToScene(Builder<?> rpc){
		HashMap<String, SocketThread> scenes = NetManager.getServersByType(Constant.TYPE_SCENE);
		for(SocketThread scene : scenes.values()){
			LogicManager.logicThread.sendToServer(scene.getSocketName(), ProtoUtil.packData(rpc), "");
		}
	}
}
