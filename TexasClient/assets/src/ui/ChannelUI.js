var SceneUI = require('SceneUI');
var GameData = require('GameData_pb');
var TexasGameData = require('TexasGameData_pb');
var Player = require('Player');
var TableModeTemplate = require('TableModeTemplate');

cc.Class({
    extends: SceneUI,

    properties: {
        match_btn_list:[cc.Node],
        match_panel:cc.Node,
        match_img:cc.Node,
        table_scroll:cc.Node,
        table_layout:cc.Node,
        table_btn:cc.Prefab,
        back_btn:cc.Node,
    },

    onLoad: function () {
        this._super();
        this.table_number = 0;
        Player.register('setTableList', this, 'setTableList');
        Player.register('matchTable', this, 'matchTable');
        Player.register('cancelMatchTable', this, 'cancelMatchTable');
        Player.register('joinTableResult', this, 'joinTableResult');
        Player.register('goBattle', this, 'goBattle');
        Player.register('onExitBattle', this, 'onExitBattle');
        Player.register('matchTableError', this, 'matchTableError');
        cc.director.preloadScene('battle');
    },

    onDestroy:function(){
        Player.deregister('setTableList', this, 'setTableList');
        Player.deregister('matchTable', this, 'matchTable');
        Player.deregister('cancelMatchTable', this, 'cancelMatchTable');
        Player.deregister('joinTableResult', this, 'joinTableResult');
        Player.deregister('goBattle', this, 'goBattle');
        Player.deregister('onExitBattle', this, 'onExitBattle');
        Player.deregister('matchTableError', this, 'matchTableError');
    },

    start:function(){
        Player.client.GetTableListReq(this.table_number);
    },

    setTableList:function(tableList){
        this.table_layout.removeAllChildren();
        if(tableList){
            for(var i = 0; i < tableList.length; i++){
                var id = tableList[i].id;
                var emptySeat = tableList[i].emptyseat;
                var mode = tableList[i].mode;
                var t_b = cc.instantiate(this.table_btn);
                t_b.parent = this.table_layout;
                var t_l = t_b.getChildByName('Label').getComponent('cc.Label');
                t_l.string = '编号：' + id + "，空余位置" + emptySeat +"，牌桌类型："+TableModeTemplate[mode].name;
                var handler = new cc.Component.EventHandler();
                handler.target = this.node;
                handler.component = "ChannelUI";
                handler.handler = "onTableBtnCallback";
                handler.customEventData = id;
                var t_b_b = t_b.getComponent('cc.Button');
                t_b_b.clickEvents.push(handler);
            }
        }
    },

    matchTable:function(){
        for(var i = 0; i < this.match_btn_list.length; i++){
            this.match_btn_list[i].active = false;
        }
        this.match_panel.active = true;
        this.match_img.runAction(cc.rotateBy(2, 180).repeatForever());
        this.isMatching = true;
    },

    cancelMatchTable:function(){
        for(var i = 0; i < this.match_btn_list.length; i++){
            this.match_btn_list[i].active = true;
        }
        this.match_img.stopAllActions();
        this.match_panel.active = false;
        this.isMatching = false;
    },

    goBattle:function(){
        cc.director.loadScene('battle');
    },

    onExitBattle:function(){
        cc.director.loadScene('lobby');
    },

    onMatchCallback:function(event, data){
        var type = parseInt(data);
        Player.client.MatchTableReq(type);
    },

    onCancelCallback:function(){
        Player.client.CancelMatchTableReq();
    },

    onTableScrollCallback:function(scrollview, eventType, data){
        
    },

    onLeftBtnCallback:function(){
        this.table_number -= 10;
        if(this.table_number < 0){
            this.table_number = 0;
        }
        Player.client.GetTableListReq(this.table_number);
    },

    onRightBtnCallback:function(){
        this.table_number += 10;
        Player.client.GetTableListReq(this.table_number);
    },
    
    onTableBtnCallback:function(event, data){
        if(this.isMatching){
            return;
        }
        var id = parseInt(data);
        Player.client.JoinTableReq(id);
    },

    joinTableResult:function(result){
        var enm = TexasGameData.JoinTableResultEnm;
        switch(result){
            case enm.OK_JOINTABLERESULT:
                this.scene_global.showUI('lock_ui');
                break;
            case enm.NO_EMPTY_SEAT:
                this.scene_global.showUI('message_ui', '没位置了');
                break;
            case enm.TABLE_DISTROYED:
                this.scene_global.showUI('message_ui', '牌局已散');
                break;
            case enm.GOLD_NOT_ENOUGH_JOINTABLERESULT:
                this.scene_global.showUI('message_ui', '金钱不足');
                break;
            default:
                break;
        }
    },

    matchTableError:function(result){
        var enm = TexasGameData.MatchTableResultEnm;
        switch(result){
            case enm.ALREADY_IN_QUEUE:
                this.scene_global.showUI('message_ui', '已在匹配队列中');
                break;
            case enm.GOLD_NOT_ENOUGH_MATCHTABLERESULT:
                this.scene_global.showUI('message_ui', '金钱不足');
                break;
        }
    },

    onBackBtnCallback:function(){
        Player.client.ExitBattleReq();
        this.scene_global.showUI('lock_ui');
    },
});
