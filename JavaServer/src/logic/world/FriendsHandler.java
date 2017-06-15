package logic.world;

import java.util.HashMap;
import java.util.Iterator;

import logic.common.LogicManager;
import logic.common.LogicThread;
import logic.common.Sender;
import logic.common.redis.RedisHandler;
import logic.common.redis.RedisKey;

import org.apache.log4j.Logger;

import com.google.protobuf.GeneratedMessageV3.Builder;
import com.google.protobuf.InvalidProtocolBufferException;

import protocol.GameData.AcceptFriendRep;
import protocol.GameData.AcceptFriendReq;
import protocol.GameData.AcceptFriendResultEnm;
import protocol.GameData.AddFriendBeRequiredRep;
import protocol.GameData.AddFriendRequireReq;
import protocol.GameData.DeleteFriendRep;
import protocol.GameData.DeleteFriendReq;
import protocol.GameData.ExitWorldRpc;
import protocol.GameData.FriendDat;
import protocol.GameData.FriendOfflineRep;
import protocol.GameData.FriendOnlineRep;
import protocol.GameData.RefuseFriendReq;
import protocol.ProtoUtil;
import core.config.Constant;
import core.log.LoggerHelper;
import core.net.DataPackage;
import core.net.NetManager;
import core.net.SocketThread;

// 好友系统
public class FriendsHandler {
	private Logger log = LoggerHelper.getLogger();
	public static FriendsHandler instance = null;
	
	
	public FriendsHandler(){
		instance = this;

		LogicManager.logicThread.setRpc(AddFriendRequireReq.class, this);
		LogicManager.logicThread.setRpc(AcceptFriendReq.class, this);
		LogicManager.logicThread.setRpc(RefuseFriendReq.class, this);
		LogicManager.logicThread.setRpc(DeleteFriendReq.class, this);
	}
	public void onDeleteFriendReq(DataPackage data){
		try {
			DeleteFriendReq deleteReq = DeleteFriendReq.parseFrom(data.rpcPo.getAnyPo());
			WorldUserData user = WorldHandler.instance.userDic.get(data.rpcPo.getClientName());
			if(user == null){
				return;
			}
			user.friendList.remove(deleteReq.getFriend());
			
			DeleteFriendRep.Builder deleteRep1 = DeleteFriendRep.newBuilder();
			deleteRep1.setFriend(deleteReq.getFriend());
			LogicManager.logicThread.sendToClient(user.username, ProtoUtil.packData(deleteRep1), data.termianl.socketThread);
		
			WorldUserData friend = WorldHandler.instance.userDic.get(deleteReq.getFriend());
			if(friend == null){
				friend = RedisHandler.instance.queryFriend(deleteReq.getFriend());
				if(friend != null){
					friend.friendList.remove(user.username);
					RedisHandler.instance.saveFriend(friend);
				}
			}else{
				// 在线 
				friend.friendList.remove(user.username);
				DeleteFriendRep.Builder deleteRep2 = DeleteFriendRep.newBuilder();
				deleteRep2.setFriend(user.username);
				LogicManager.logicThread.sendToClient(friend.username, ProtoUtil.packData(deleteRep2), data.termianl.socketThread);
			}
		} catch (InvalidProtocolBufferException e) {
			log.error(e);
		}
	}
	public void onRefuseFriendReq(DataPackage data){
		try {
			RefuseFriendReq refuseReq = RefuseFriendReq.parseFrom(data.rpcPo.getAnyPo());
			WorldUserData user = WorldHandler.instance.userDic.get(data.rpcPo.getClientName());
			if(user == null){
				return;
			}
			user.addReqSet.remove(refuseReq.getFrom());
			// 这里不返还
		} catch (InvalidProtocolBufferException e) {
			log.error(e);
		}
	}
	// 接收请求
	public void onAcceptFriendReq(DataPackage data){
		AcceptFriendReq acceptReq;
		try {
			acceptReq = AcceptFriendReq.parseFrom(data.rpcPo.getAnyPo());

			WorldUserData user = WorldHandler.instance.userDic.get(data.rpcPo.getClientName());
			if(user == null){
				return;
			}

			AcceptFriendRep.Builder failRep = AcceptFriendRep.newBuilder();
			if(!user.addReqSet.containsKey(acceptReq.getFrom())){
				failRep.setResult(AcceptFriendResultEnm.NOBODY_REQUIRED);
				LogicManager.logicThread.sendToClient(user.username, ProtoUtil.packData(failRep), data.termianl.socketThread);
				return;
			}
			
			user.addReqSet.remove(acceptReq.getFrom());
			
			if(user.friendList.size() >= 20){
				failRep.setResult(AcceptFriendResultEnm.FRIEND_NUMBER_ALREADY_20);
				LogicManager.logicThread.sendToClient(user.username, ProtoUtil.packData(failRep), data.termianl.socketThread);
				return;
			}
			
			// 必须都在线才能加好友，简单做法
			WorldUserData from = WorldHandler.instance.userDic.get(acceptReq.getFrom());
			boolean offline = false;
			if(from == null){
				// 如果没有，从数据库中找
				offline = true;
				from = RedisHandler.instance.queryFriend(acceptReq.getFrom());
				if(from == null){
					// 不存在邀请
					failRep.setResult(AcceptFriendResultEnm.NOBODY_REQUIRED);
					LogicManager.logicThread.sendToClient(user.username, ProtoUtil.packData(failRep), data.termianl.socketThread);
					return;
				}
			}
			
			// 在线
			if(!offline){
				FriendDat.Builder userDat = user.toFriendDat();
				from.friendList.put(user.username, userDat);
				AcceptFriendRep.Builder successRep1 = AcceptFriendRep.newBuilder();
				successRep1.setResult(AcceptFriendResultEnm.OK_ACCEPTFRIENDRESULT);
				successRep1.setFriend(userDat);
				WorldHandler.instance.sendToClient(from.username, successRep1);
			}else{
				RedisHandler.instance.saveFriend(from);
			}
			
			FriendDat.Builder fromDat = from.toFriendDat();
			user.friendList.put(from.username, fromDat);
			AcceptFriendRep.Builder successRep2 = AcceptFriendRep.newBuilder();
			successRep2.setResult(AcceptFriendResultEnm.OK_ACCEPTFRIENDRESULT);
			successRep2.setFriend(fromDat);
			WorldHandler.instance.sendToClient(user.username, successRep2);
		} catch (InvalidProtocolBufferException e) {
			log.error(e);
		}
		
	}
	// 请求添加好友
	public void onAddFriendRequireReq(DataPackage data){
		try {
			AddFriendRequireReq addReq = AddFriendRequireReq.parseFrom(data.rpcPo.getAnyPo());
			WorldUserData user = WorldHandler.instance.userDic.get(data.rpcPo.getClientName());
			if(user == null){
				return;
			}
			
			if(addReq.getTarget().equals(data.rpcPo.getClientName())){
				return;
			}
			
			WorldUserData target = WorldHandler.instance.userDic.get(addReq.getTarget());
			// 在线
			if(target != null && !target.friendList.containsKey(user.username)){
				target.addReqSet.put(user.username, user.toFriendDat());
				AddFriendBeRequiredRep.Builder addRep = AddFriendBeRequiredRep.newBuilder();
				addRep.setFrom(user.toFriendDat());
				
				// 这里通知客户端
				WorldHandler.instance.sendToClient(target.username, addRep);
			}else{// 离线
				String targetKey = RedisKey.UserKey(addReq.getTarget());
				target = RedisHandler.instance.queryFriend(addReq.getTarget());
				
				if(target != null){
					if(!target.friendList.containsKey(user.username)){
						target.addReqSet.put(user.username, user.toFriendDat());
						RedisHandler.instance.saveFriend(target);
					}
				}
			}
		} catch (InvalidProtocolBufferException e) {
			log.error(e);
		}
		
	}
	
	public void friendOnline(WorldUserData user){
		// 通知朋友上线
		FriendOnlineRep.Builder onlineRep = FriendOnlineRep.newBuilder();
		onlineRep.setUsername(user.username);
		Iterator<FriendDat.Builder> iter = user.friendList.values().iterator();
		while(iter.hasNext()){
			FriendDat.Builder friend = iter.next();
			WorldUserData friendData = WorldHandler.instance.userDic.get(friend.getUsername());
			if(friendData != null){
				if(friendData.friendList.containsKey(user.username)){
					friend.setOnline(true);
					// 好友修改了nickname
					friend.setNickname(friendData.nickname);
					friend.setPortrait(friendData.portrait);
					friendData.friendList.get(user.username).setNickname(user.nickname);
					onlineRep.setNickname(user.nickname);
					onlineRep.setPortrait(user.portrait);
					WorldHandler.instance.sendToClient(friend.getUsername(), onlineRep);
				}else{ // 已经解除好友关系，处理一下
					iter.remove();
				}
			}else{
				friend.setOnline(false);
			}
		}
		// 返回数据
		WorldHandler.instance.sendToClient(user.username, user.toFriendListRep());
	}
	
	public void friendOffline(WorldUserData user){	
		RedisHandler.instance.saveWorldUser(user);
		
		FriendOfflineRep.Builder offlineRep = FriendOfflineRep.newBuilder();
		offlineRep.setUsername(user.username);
		for(FriendDat.Builder friend : user.friendList.values()){
			WorldHandler.instance.sendToClient(friend.getUsername(), offlineRep);
		}
	}

}
