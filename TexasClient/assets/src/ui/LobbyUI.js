var SceneUI = require('SceneUI');
var GameData = require('GameData_pb');
var Player = require('Player');
var g_utility = require('g_utility');
var HeadTemplate = require('HeadTemplate');

cc.Class({
    extends: SceneUI,

    properties: {
        channel_content:cc.Node,
        channel_btn_prefab:cc.Prefab,
        head_img:cc.Sprite,
        name_label:cc.Label,
        gold_label:cc.Label,

        friend_btn:cc.Node,
        email_btn:cc.Node,
        red_point:cc.SpriteFrame,
    },

    onLoad: function () {
        this._super();
        Player.register('joinSuccess', this, 'joinSuccess');
        Player.register('joinFail', this, 'joinFail');
        Player.register('dailyLoginAward', this, 'dailyLoginAward');
        Player.register('onChangeNickname', this, 'onChangeNickname');
        Player.register('onChangePortrait', this, 'onChangePortrait');
        Player.register('checkRedPoint', this, 'checkRedPoint');
        Player.register('onSetGold', this, 'onSetGold');
        cc.director.preloadScene('channel');
    },

    onDestroy:function(){
        Player.deregister('joinSuccess', this, 'joinSuccess');
        Player.deregister('joinFail', this, 'joinFail');
        Player.deregister('dailyLoginAward', this, 'dailyLoginAward');
        Player.deregister('onChangeNickname', this, 'onChangeNickname');
        Player.deregister('onChangePortrait', this, 'onChangePortrait');
        Player.deregister('checkRedPoint', this, 'checkRedPoint');
        Player.deregister('onSetGold', this, 'onSetGold');
    },

    start:function(){
        var client = Player.client;
        this.name_label.string = client.nickname;
        this.gold_label.string = client.gold;
        var head_index = g_utility.getHeadIndexByName(client.portrait);
        if(head_index != null){
            g_utility.setSpriteFrame(HeadTemplate[head_index].path, this.head_img);
        }
        var btn = cc.instantiate(this.channel_btn_prefab);
        btn.parent = this.channel_content;
        var b_l = btn.getChildByName('Label').getComponent('cc.Label');
        b_l.string = "默认频道";
        var b_b = btn.getComponent('cc.Button');
        var handler = new cc.Component.EventHandler();
        handler.target = this.node;
        handler.component = 'LobbyUI';
        handler.handler = 'onJoinBtnCallback';
        handler.customEventData = "";
        b_b.clickEvents.push(handler);

        this.checkRedPoint();
    },

    onSetGold:function(gold, cause, delta){
        this.gold_label.string = gold;
    },

    onChangeNickname:function(result, nickname){
        var enm = GameData.ChangeNicknameResultEnm;
        switch(result){
            case enm.OK_CHANGENICKNAMERESULT:
                this.scene_global.showUI('message_ui', '修改昵称成功');
                this.name_label.string = nickname;
                break;
            case enm.NICKNAME_USED:
                this.scene_global.showUI('message_ui', '昵称已被注册');
                break;
            case enm.ERROR_FORMAT:
                this.scene_global.showUI('message_ui', '昵称格式错误');
                break;
        }
    },

    onChangePortrait:function(portrait){
        var head_index = g_utility.getHeadIndexByName(portrait);
        if(head_index != null){
            g_utility.setSpriteFrame(HeadTemplate[head_index].path, this.head_img);
            this.scene_global.showUI('message_ui', '修改头像成功');
        }
    },

    onJoinBtnCallback:function(){
        Player.client.JoinBattleReq();
    },

    joinSuccess:function(){
        cc.director.loadScene('channel');
    },

    joinFail:function(){
        this.scene_global.showUI('message_ui', '加入频道失败');
    },

    dailyLoginAward:function(gold){
        this.scene_manager.showUI('daily_award_ui', gold);
    },

    rankBtnCallback:function(event, data){
        this.scene_manager.showUI('rank_ui', parseInt(data));
    },

    shopBtnCallback:function(){
        this.scene_manager.showUI('shop_ui');
    },

    emailBtnCallback:function(){
        this.scene_manager.showUI('email_ui');
    },

    friendBtnCallback:function(){
        this.scene_manager.showUI('friend_ui');
    },

    goldBtnCallback:function(){
        this.scene_manager.showUI('shop_ui');
    },

    nameBtnCallback:function(){
        this.scene_manager.showUI('name_ui');
    },

    headBtnCallback:function(){
        this.scene_manager.showUI('portrait_ui');
    },

    checkRedPoint:function(){
        var client = Player.client;
        if(client.hasNotReadMsg()){
            g_utility.addRedPoint(this.friend_btn, this.red_point, 40, 30);
        }
        else{
            g_utility.removeRedPoint(this.friend_btn);
        }

        if(client.hasNotReadEmail()){
            g_utility.addRedPoint(this.email_btn, this.red_point, 40, 30);
        }
        else{
            g_utility.removeRedPoint(this.email_btn);
        }
    },
});
