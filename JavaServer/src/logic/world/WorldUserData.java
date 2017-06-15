package logic.world;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import protocol.GameData.EmailDat;
import protocol.GameData.EmailListRep;
import protocol.GameData.FriendDat;
import protocol.GameData.FriendListRep;

public class WorldUserData {
	public String username = "";
	public String nickname = "";
	public String portrait = "";
	public String gateway = "";
	
	public HashMap<String, FriendDat.Builder> friendList = new HashMap<String, FriendDat.Builder>();
	public LinkedHashMap<String, FriendDat.Builder> addReqSet = new LinkedHashMap<String, FriendDat.Builder>();
	public LinkedList<EmailDat.Builder> emailList = new LinkedList<EmailDat.Builder>();
	
	public EmailListRep.Builder toEmailListRep(){
		EmailListRep.Builder emailRep = EmailListRep.newBuilder();
		for(int i=0; i<emailList.size(); i++){
			emailRep.addEmailList(emailList.get(i));
		}
		return emailRep;
	}
	
	public FriendListRep.Builder toFriendListRep(){
		FriendListRep.Builder friendListRep = FriendListRep.newBuilder();
		for(FriendDat.Builder friend : friendList.values()){
			friendListRep.addFriendList(friend);
		}
		Iterator<FriendDat.Builder> iter = addReqSet.values().iterator();
		while(addReqSet.size() > 10){ // 保留10个申请
			iter.remove();
		}
		for(FriendDat.Builder rn : addReqSet.values()){
			friendListRep.addRequireList(rn);
		}
		return friendListRep;
	}
	
	public FriendDat.Builder toFriendDat(){
		FriendDat.Builder dat = FriendDat.newBuilder();
		dat.setUsername(username);
		dat.setNickname(nickname);
		dat.setPortrait(portrait);
		dat.setOnline(true);
		return dat;
	}
}
