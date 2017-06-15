package logic.battle.boomBrothers;

import core.net.NetManager;
import logic.battle.boomBrothers.entity.BattleEntity;
import logic.common.Sender;
import protocol.BoomGameData.BattlePlayerTypeEnm;
import protocol.BoomGameData.BattleUserInfoDat;
import protocol.ProtoUtil;
import protocol.Protocol.RpcPo;
import template.HeroTemplate;
import template.HeroTemplate.HeroTemp;

public class BattlePlayer {
	public String username = "";
	public String addr = "";
	public int port = 0;
	public long clientID = 0;
	
	public BattlePlayerTypeEnm type = BattlePlayerTypeEnm.UNDEFINE_BATTLEPLAYERTYPE;
	
	public boolean loaded = false; // 客户端是否加载完成
	
	public int heroID = 0; // 英雄id
	
	public boolean isOffline = false; // 掉线
	
	public int index = -1; // 标号
	
	public BattleField bf = null;
	// 战场中的实体
	public BattleEntity entity = null;
	
	public BattlePlayer(){
		
	}

	public BattleUserInfoDat.Builder toBattleUserInfoDat(){
		BattleUserInfoDat.Builder dat = BattleUserInfoDat.newBuilder();
		dat.setUsername(username);
		return dat;
	}
	// use udp
	public void udpSend(RpcPo.Builder rpcPo){
		if(type == BattlePlayerTypeEnm.PLAYER_BP){
			NetManager.udpSend(addr, port, rpcPo);
		}
	}
	// tcp
	public void send(RpcPo.Builder rpcPo){
		if(type == BattlePlayerTypeEnm.PLAYER_BP){
			Sender.sendToClient(username, rpcPo);
		}
	}
	
	public long requireActionTime = 0;
	public void setTurn(){
		requireActionTime = System.currentTimeMillis();
	}
	
	public void update(int turn){
		// 5s后执行ai
		if(index == turn && System.currentTimeMillis() - requireActionTime > 5000){
			requireActionTime = Long.MAX_VALUE;
			if(bf.state == BattleField.STATE_BAN){
				bf.setBanHero(username, 2);
			}
			if(bf.state == BattleField.STATE_SELECT){
				bf.setBanHero(username, 1);
			}
		}
	}
}
