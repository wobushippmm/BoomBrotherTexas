var LayerUI = require('LayerUI');
var Player = require('Player');
var GameData = require('GameData_pb');
var HeadTemplate = require('HeadTemplate');
var g_utility = require('g_utility');
var g_global = require('g_global');
var g_const = require('g_const');

cc.Class({
    extends: LayerUI,

    properties: {
        friend_content:cc.Node,
        friend_btn_prefab:cc.Prefab,
        add_input:cc.EditBox,

        text_bg:cc.Node,
        head_img:cc.Sprite,
        name_label:cc.Label,
        state_label:cc.Label,
        mail_input:cc.EditBox,
        chat_input:cc.EditBox,
        content_scroll:cc.ScrollView,
        chat_content:cc.Node,
        chat_label_prefab:cc.Prefab,
        delete_btn:cc.Button,

        text_bg2:cc.Node,
        add_friend_content:cc.Node,
        add_friend_item:cc.Prefab,

        red_point:cc.SpriteFrame,
    },

    onLoad: function () {
        this._super();
        this.easyHide = true;
        this.currentFriend = null;
        Player.register('onFriendList', this, 'onFriendList');
        Player.register('onAddFriendBeRequired', this, 'onAddFriendBeRequired');
        Player.register('onAcceptFriend', this, 'onAcceptFriend');
        Player.register('onDeleteFriend', this, 'onDeleteFriend');
        Player.register('onFriendOnline', this, 'onFriendOnline');
        Player.register('onFriendOffline', this, 'onFriendOffline');
        Player.register('onFriendChat', this, 'onFriendChat');
        Player.register('onAcceptFriendRequire', this, 'onAcceptFriendRequire');
        Player.register('onRefuseFriendRequire', this, 'onRefuseFriendRequire');
        Player.register('checkRedPoint', this, 'checkRedPoint');
        Player.register('onFriendChangeNickname', this, 'onFriendChangeNickname');
    },

    onDestroy:function(){
        Player.deregister('onFriendList', this, 'onFriendList');
        Player.deregister('onAddFriendBeRequired', this, 'onAddFriendBeRequired');
        Player.deregister('onAcceptFriend', this, 'onAcceptFriend');
        Player.deregister('onDeleteFriend', this, 'onDeleteFriend');
        Player.deregister('onFriendOnline', this, 'onFriendOnline');
        Player.deregister('onFriendOffline', this, 'onFriendOffline');
        Player.deregister('onFriendChat', this, 'onFriendChat');
        Player.deregister('onAcceptFriendRequire', this, 'onAcceptFriendRequire');
        Player.deregister('onRefuseFriendRequire', this, 'onRefuseFriendRequire');
        Player.deregister('checkRedPoint', this, 'checkRedPoint');
        Player.deregister('onFriendChangeNickname', this, 'onFriendChangeNickname');
    },

    onShow:function(event){
        var client = Player.client;
        this.onFriendList(client.friendList);

        this.checkRedPoint();
    },

    onHide:function(){

    },

    /////////////////////////////////////////////////////////////////////////////////////
    // 事件
    /////////////////////////////////////////////////////////////////////////////////////
    onFriendList:function(friendList){
        var client = Player.client;
        this.friend_content.removeAllChildren();
        for(var i = 0; i < friendList.length; i++){
            var friend = friendList[i];
            var btn = cc.instantiate(this.friend_btn_prefab);
            btn.parent = this.friend_content;
            var b_l = btn.getChildByName('Label').getComponent('cc.Label');
            b_l.string = '昵称：'+friend.nickname+' 状态：'+(friend.online?'在线':'离线');
            var handler = new cc.Component.EventHandler;
            handler.target = this.node;
            handler.component = 'FriendUI';
            handler.handler = 'onFriendBtnCallback';
            handler.customEventData = friend;
            btn.getComponent('cc.Button').clickEvents.push(handler);
            btn.target_name = friend.username;
            var head_img = btn.getChildByName('head_img').getComponent('cc.Sprite');
            var head_index = g_utility.getHeadIndexByName(friend.portrait);
            if(head_index != null){
                g_utility.setSpriteFrame(HeadTemplate[head_index].path, head_img);
            }
        }
    },

    onAddFriendBeRequired:function(friend){
        var name = friend.nickname;
        var item = cc.instantiate(this.add_friend_item);
        item.target_name = friend.username;
        item.parent = this.add_friend_content;
        var label = item.getChildByName('info_label').getComponent('cc.Label');
        label.string = "昵称："+name;
        var yes_btn = item.getChildByName('yes_btn').getComponent('cc.Button');
        var yes_handler = new cc.Component.EventHandler();
        yes_handler.target = this.node;
        yes_handler.component = 'FriendUI';
        yes_handler.handler = 'onYesBtnCallback';
        yes_handler.customEventData = friend;
        yes_btn.clickEvents.push(yes_handler);
        var no_btn = item.getChildByName('no_btn').getComponent('cc.Button');
        var no_handler = new cc.Component.EventHandler();
        no_handler.target = this.node;
        no_handler.component = 'FriendUI';
        no_handler.handler = 'onNoBtnCallback';
        no_handler.customEventData = friend;
        no_btn.clickEvents.push(no_handler);
        var head_img = item.getChildByName('head_img').getComponent('cc.Sprite');
        var head_index = g_utility.getHeadIndexByName(friend.portrait);
        if(head_index != null){
            g_utility.setSpriteFrame(HeadTemplate[head_index].path, head_img);
        }
    },

    onAcceptFriend:function(result, friend){
        var enm = GameData.AcceptFriendResultEnm;
        switch(result){
            case enm.OK_ACCEPTFRIENDRESULT:
                cc.find('Canvas').getComponent('SceneUI').scene_global.showUI('message_ui', '接受好友成功');
                var btn = cc.instantiate(this.friend_btn_prefab);
                btn.parent = this.friend_content;
                var b_l = btn.getChildByName('Label').getComponent('cc.Label');
                b_l.string = '昵称：'+friend.nickname+' 状态：'+(friend.online?'在线':'离线');
                var handler = new cc.Component.EventHandler;
                handler.target = this.node;
                handler.component = 'FriendUI';
                handler.handler = 'onFriendBtnCallback';
                handler.customEventData = friend;
                btn.getComponent('cc.Button').clickEvents.push(handler);
                btn.target_name = friend.username;
                var head_img = btn.getChildByName('head_img').getComponent('cc.Sprite');
                var head_index = g_utility.getHeadIndexByName(friend.portrait);
                if(head_index != null){
                    g_utility.setSpriteFrame(HeadTemplate[head_index].path, head_img);
                }
                break;
            case enm.FRIEND_NUMBER_ALREADY_20:
                cc.find('Canvas').getComponent('SceneUI').scene_global.showUI('message_ui', '对方好友已满20');
                break;
            case enm.NOBODY_REQUIRED:
                cc.find('Canvas').getComponent('SceneUI').scene_global.showUI('message_ui', '没有这个好友');
                break;
        }
    },

    onDeleteFriend:function(username){
        if(this.currentFriend && this.currentFriend.username == username){
            this.onCloseBtnCallback();
        }
        var btn = this.findFriendBtn(username);
        if(!btn){
            return;
        }
        btn.removeFromParent();
    },

    onFriendOnline:function(username, nickname, portrait){
        var head_index = g_utility.getHeadIndexByName(portrait);
        if(this.currentFriend && this.currentFriend.username == username){
            this.name_label.string = "昵称："+nickname;
            this.state_label.string = "状态：在线";
            if(head_index != null){
                g_utility.setSpriteFrame(HeadTemplate[head_index].path, this.head_img);
            }
        }
        var btn = this.findFriendBtn(username);
        if(btn){
            var b_l = btn.getChildByName('Label').getComponent('cc.Label');
            b_l.string = '昵称：'+nickname+' 状态：在线';
            var head_img = btn.getChildByName('head_img').getComponent('cc.Sprite');
            if(head_index != null){
                g_utility.setSpriteFrame(HeadTemplate[head_index].path, head_img);
            }
        }
    },

    onFriendOffline:function(username, nickname){
        if(this.currentFriend && this.currentFriend.username == username){
            this.name_label.string = "昵称："+nickname;
            this.state_label.string = "状态：离线";
        }
        var btn = this.findFriendBtn(username);
        if(btn){
            var b_l = btn.getChildByName('Label').getComponent('cc.Label');
            b_l.string = '昵称：'+nickname+' 状态：离线';
        }
    },

    onFriendChat:function(info){
        var client = Player.client;
        var f_nickname = info[0];
        var t_nickname = info[1];
        var username = info[2];
        var msg = info[3];
        if(this.currentFriend && this.currentFriend.username == username){
            var label = cc.instantiate(this.chat_label_prefab);
            label.parent = this.chat_content;
            label.getComponent('cc.Label').string = f_nickname+':'+msg;
            client.setMsgRead(info);
            this.content_scroll.scrollToBottom();
        }
    },

    onRequireList:function(requireList){
        this.add_friend_content.removeAllChildren();
        for(var i = 0; i < requireList.length; i++){
            var friend = requireList[i];
            this.onAddFriendBeRequired(friend);
            // var name = friend.nickname;
            // var item = cc.instantiate(this.add_friend_item);
            // item.target_name = friend.username;
            // item.parent = this.add_friend_content;
            // var label = item.getChildByName('info_label').getComponent('cc.Label');
            // label.string = "昵称："+name;
            // var yes_btn = item.getChildByName('yes_btn').getComponent('cc.Button');
            // var yes_handler = new cc.Component.EventHandler();
            // yes_handler.target = this.node;
            // yes_handler.component = 'FriendUI';
            // yes_handler.handler = 'onYesBtnCallback';
            // yes_handler.customEventData = friend;
            // yes_btn.clickEvents.push(yes_handler);
            // var no_btn = item.getChildByName('no_btn').getComponent('cc.Button');
            // var no_handler = new cc.Component.EventHandler();
            // no_handler.target = this.node;
            // no_handler.component = 'FriendUI';
            // no_handler.handler = 'onNoBtnCallback';
            // no_handler.customEventData = friend;
            // no_btn.clickEvents.push(no_handler);
            // var head_img = item.getChildByName('head_img').getComponent('cc.Sprite');
            // var head_index = g_utility.getHeadIndexByName(friend.portrait);
            // if(head_index != null){
            //     g_utility.setSpriteFrame(HeadTemplate[head_index].path, head_img);
            // }
        }
    },

    onAcceptFriendRequire:function(name){
        var item = this.findAddFriendItem(name);
        if(!item){
            return;
        }
        item.removeFromParent();
    },

    onRefuseFriendRequire:function(name){
        var item = this.findAddFriendItem(name);
        if(!item){
            return;
        }
        item.removeFromParent();
    },

    onFriendChangeNickname:function(username, nickname){
        if(this.currentFriend && this.currentFriend.username == username){
            this.name_label.string = "昵称："+nickname;
        }
        var btn = this.findFriendBtn(username);
        if(btn){
            var b_l = btn.getChildByName('Label').getComponent('cc.Label');
            b_l.string = '昵称：'+nickname+' 状态：在线';
        }
    },
    
    /////////////////////////////////////////////////////////////////////////////////////
    // UI事件
    /////////////////////////////////////////////////////////////////////////////////////
    onEmailBtnCallback:function(){
        if(this.mail_input.string.length <= 0){
            return;
        }
        if(g_global.canSendEmail()){
            var target = new Array();
            target.push(this.currentFriend.username);
            Player.client.SendEmailReq(Player.client.username, target, 0, this.mail_input.string, false);
            cc.find('Canvas').getComponent('SceneUI').scene_global.showUI('message_ui', '邮件已发送');
            this.mail_input.string = "";
        }
        else{
            cc.find('Canvas').getComponent('SceneUI').scene_global.showUI('message_ui', '发送邮件频率过高（需要间隔'+ (g_const.EMAIL_INTERVAL/g_const.SECOND) +'s）');
        }
    },

    onChatBtnCallback:function(){
        if(this.chat_input.string.length <= 0){
            return;
        }
        if(g_global.canSendChat()){
            Player.client.FriendChatReq(this.currentFriend.username, this.chat_input.string);
            this.chat_input.string = "";
        }
        else{
            cc.find('Canvas').getComponent('SceneUI').scene_global.showUI('message_ui', '发送消息频率过高（需要间隔'+ (g_const.CHAT_INTERVAL/g_const.SECOND) +'s）');
        }
    },

    onCloseBtnCallback:function(){
        this.currentFriend = null;
        this.text_bg.active = false;
        this.text_bg2.active = false;
    },

    onAddBtnCallback:function(){
        var client = Player.client;
        var name_length = this.add_input.string.length;
        if(name_length <= 0 || name_length >= 12){
            cc.find('Canvas').getComponent('SceneUI').scene_global.showUI('message_ui', '名字长度为1~11个字符');
            return;
        }
        var name = this.add_input.string;
        if(client.nickname == name || client.username == name){
            cc.find('Canvas').getComponent('SceneUI').scene_global.showUI('message_ui', '不能加自己好友');
            return;
        }
        client.AddFriendRequireReq(name);
        cc.find('Canvas').getComponent('SceneUI').scene_global.showUI('message_ui', '好友申请发送成功');
        this.add_input.string = "";
    },

    onDeleteBtnCallback:function(event, data){
        Player.client.DeleteFriendReq(data);
    },

    onFriendBtnCallback:function(event, data){
        this.currentFriend = data;
        this.text_bg.active = true;
        this.chat_content.removeAllChildren();
        var infoList = Player.client.getFriendChatList(data.username);
        for(var i = 0; i < infoList.length; i++){
            this.onFriendChat(infoList[i]);
        }
        this.scheduleOnce(this.contentScrollMove, 0);
        this.name_label.string = "昵称："+data.nickname;
        this.state_label.string = "状态："+(data.online?"在线":"离线");
        this.chat_input.string = "";
        this.mail_input.string = "";
        var head_index = g_utility.getHeadIndexByName(data.portrait);
        if(head_index != null){
            g_utility.setSpriteFrame(HeadTemplate[head_index].path, this.head_img);
        }
        this.delete_btn.clickEvents[0].customEventData = data.username;
    },

    contentScrollMove:function(){
        this.content_scroll.scrollToBottom();
    },

    onCheckBtnCallback:function(){
        this.text_bg2.active = true;
        this.onRequireList(Player.client.requireList);
    },

    onYesBtnCallback:function(event, data){
        Player.client.AcceptFriendReq(data.username);
    },

    onNoBtnCallback:function(event, data){
        Player.client.RefuseFriendReq(data.username);
    },

    /////////////////////////////////////////////////////////////////////////////////////
    // 辅助函数
    /////////////////////////////////////////////////////////////////////////////////////
    findFriendBtn:function(username){
        var children = this.friend_content.children;
        var count = this.friend_content.childrenCount;
        for(var i = 0; i < count; i++){
            var child = children[i];
            if(child.target_name == username){
                return child;
            }
        }
        return null;
    },

    findAddFriendItem:function(name){
        var children = this.add_friend_content.children;
        var count = this.add_friend_content.childrenCount;
        for(var i = 0; i < count; i++){
            var child = children[i];
            if(child.target_name == name){
                return child;
            }
        }
        return null;
    },

    checkRedPoint:function(){
        var client = Player.client;
        var children = this.friend_content.children;
        var count = this.friend_content.childrenCount;
        for(var i = 0; i < count; i++){
            var child = children[i];
            if(client.hasNotReadMsg(child.target_name)){
                g_utility.addRedPoint(child, this.red_point, 180, 40);
            }
            else{
                g_utility.removeRedPoint(child);
            }
        }
    },
});
