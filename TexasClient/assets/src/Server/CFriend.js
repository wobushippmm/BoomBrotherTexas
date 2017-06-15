var CEmail = require('CEmail');
var GameData = require('GameData_pb');

var CFriend = CEmail.extend({
    ctor:function(){
        this._super();
        this.friendList = null;
        this.requireList = null;
    },

    /////////////////////////////////////////////////////////////////////////////////////
    // 推送
    /////////////////////////////////////////////////////////////////////////////////////
    FriendListRep:function(obj){
        var friendList = obj.friendlistList;
        var requireList = obj.requirelistList;
        this.friendList = friendList;
        this.requireList = requireList;
        this.event.fire('onFriendList', friendList);
    },

    AddFriendBeRequiredRep:function(obj){
        var from = obj.from;
        this.requireList.push(from);
        this.event.fire('onAddFriendBeRequired', from);
    },

    AcceptFriendRep:function(obj){
        var result = obj.result;
        var friend = obj.friend;
        var enm = GameData.AcceptFriendResultEnm;
        if(result == enm.OK_ACCEPTFRIENDRESULT){
            this.friendList.push(friend);
        }
        this.event.fire('onAcceptFriend', result, friend);
    },

    DeleteFriendRep:function(obj){
        var friend = obj.friend;
        for(var i = 0; i < this.friendList.length; i++){
            var data = this.friendList[i];
            if(data.username == friend){
                this.friendList.splice(i, 1);
                break;
            }
        }
        this.event.fire('onDeleteFriend', friend);
    },

    FriendOnlineRep:function(obj){
        if(this.friendList == null){
            return;
        }
        var username = obj.username;
        var nickname = obj.nickname;
        var portrait = obj.portrait;
        for(var i = 0; i < this.friendList.length; i++){
            var data = this.friendList[i];
            if(data.username == username){
                data.online = true;
                if(nickname != ""){
                    data.nickname = nickname;
                }
                data.portrait = portrait;
            }
        }
        this.event.fire('onFriendOnline', username, nickname, portrait);
    },

    FriendOfflineRep:function(obj){
        if(this.friendList == null){
            return;
        }
        var username = obj.username;
        var nickname = "";
        for(var i = 0; i < this.friendList.length; i++){
            var data = this.friendList[i];
            if(data.username == username){
                data.online = false;
                nickname = data.nickname;
            }
        }
        this.event.fire('onFriendOffline', username, nickname);
    },

    FriendChangeNicknameRep:function(obj){
        var username = obj.username;
        var nickname = obj.nickname;
        for(var i = 0; i < this.friendList.length; i++){
            var friend = this.friendList[i];
            if(friend.username == username){
                friend.nickname = nickname;
            }
        }
        this.event.fire('onFriendChangeNickname', username, nickname);
    },

    FriendChatRep:function(from, channel, msg){
        for(var i = 0; i < this.friendList.length; i++){
            var friend = this.friendList[i];
            if(friend.username == from){
                if(!friend.msgList){
                    friend.msgList = [];
                }
                var msgInfo = [friend.nickname, this.nickname, from, msg, false];
                friend.msgList.push(msgInfo);
                this.event.fire('onFriendChat', msgInfo);
                this.event.fire('checkRedPoint');
                return;
            }
        }
    },
    
    /////////////////////////////////////////////////////////////////////////////////////
    // 请求
    /////////////////////////////////////////////////////////////////////////////////////

    AddFriendRequireReq:function(target){
        this.clientCall('AddFriendRequireReq', target);
    },

    AcceptFriendReq:function(from){
        for(var i = 0; i < this.requireList.length; i++){
            var name = this.requireList[i].username;
            if(name == from){
                this.requireList.splice(i, 1);
                break;
            }
        }
        this.clientCall('AcceptFriendReq', from);
        this.event.fire('onAcceptFriendRequire', from);
    },

    RefuseFriendReq:function(from){
        for(var i = 0; i < this.requireList.length; i++){
            var name = this.requireList[i].username;
            if(name == from){
                this.requireList.splice(i, 1);
                break;
            }
        }
        this.clientCall('RefuseFriendReq', from);
        this.event.fire('onRefuseFriendRequire', from);
    },

    DeleteFriendReq:function(target){
        this.clientCall('DeleteFriendReq', target);
    },

    FriendChatReq:function(target, msg){
        for(var i = 0; i < this.friendList.length; i++){
            var friend = this.friendList[i];
            if(friend.username == target){
                if(!friend.msgList){
                    friend.msgList = [];
                }
                var msgInfo = [this.nickname, friend.nickname, target, msg, true];
                friend.msgList.push(msgInfo);
                this.event.fire('onFriendChat', msgInfo);
                var targets = new Array();
                targets.push(target);
                this.ChatReq(targets, GameData.ChatChannelTypeEnm.PERSONAL_CHANNEL, msg);
                return;
            }
        }
    },
    
    /////////////////////////////////////////////////////////////////////////////////////
    // 辅助函数
    /////////////////////////////////////////////////////////////////////////////////////
    getFriendChatList:function(username){
        for(var i = 0; i < this.friendList.length; i++){
            var friend = this.friendList[i];
            if(friend.username == username){
                if(!friend.msgList){
                    friend.msgList = [];
                }
                return friend.msgList;
            }
        }
    },

    hasNotReadMsg:function(username){
        if(this.friendList == null){
            return false;
        }
        if(username == null){
            for(var i = 0; i < this.friendList.length; i++){
                var friend = this.friendList[i];
                if(friend.msgList && friend.msgList.length >= 1 && friend.msgList[friend.msgList.length-1][4] == false){
                    return true;
                }
            }
            return false;
        }
        else{
            for(var i = 0; i < this.friendList.length; i++){
                var friend = this.friendList[i];
                if(friend.username == username){
                    if(friend.msgList && friend.msgList.length >= 1 && friend.msgList[friend.msgList.length-1][4] == false){
                        return true;
                    }
                    else{
                        return false;
                    }
                }
            }
            return false;
        }
    },

    setMsgRead:function(info){
        info[4] = true;
        this.event.fire('checkRedPoint');
    },
});

module.exports = CFriend;