var LayerUI = require('LayerUI');
var GameData = require('GameData_pb');
var Player = require('Player');
var g_utility = require('g_utility');

cc.Class({
    extends: LayerUI,

    properties: {
        content:cc.Node,
        text_bg:cc.Node,
        email_txt:cc.Label,
        email_btn_prefab:cc.Prefab,

        red_point:cc.SpriteFrame,
    },

    onLoad: function () {
        this._super();
        this.easyHide = true;
        this.reading = false;
        Player.register('onEmailList', this, 'onEmailList');
        Player.register('checkRedPoint', this, 'checkRedPoint');
    },

    onDestroy:function(){
        Player.deregister('onEmailList', this, 'onEmailList');
        Player.deregister('checkRedPoint', this, 'checkRedPoint');
    },

    onShow:function(event){
        Player.client.GetEmailListReq();
    },

    onHide:function(){

    },

    onEmailList:function(emailList){
        this.content.removeAllChildren();
        for(var i = 0; i < emailList.length; i++){
            var email = emailList[i];
            var btn = cc.instantiate(this.email_btn_prefab);
            btn.email_index = i;
            btn.parent = this.content;
            var b_b = btn.getComponent('cc.Button');
            var handler = new cc.Component.EventHandler();
            handler.target = this.node;
            handler.component = 'EmailUI';
            handler.handler = 'onEmailBtnCallback';
            handler.customEventData = [i, email.msg];
            b_b.clickEvents.push(handler);
            var b_l = btn.getChildByName('Label').getComponent('cc.Label');
            b_l.string = '来自：'+email.from;
        }
    },

    onEmailBtnCallback:function(event, data){
        if(this.reading){
            return;
        }
        else{
            this.reading = true;
            this.text_bg.active = true;
            this.email_txt.string = data[1];
            Player.client.SetEmailReadReq(data[0]);
        }
    },

    onCloseBtnCallback:function(){
        this.reading = false;
        this.text_bg.active = false;
    },

    onLeftBtnCallback:function(){

    },

    onRightBtnCallback:function(){

    },

    checkRedPoint:function(){
        var client = Player.client;
        var children = this.content.children;
        var count = this.content.childrenCount;
        for(var i = 0; i < count; i++){
            var child = children[i];
            if(client.hasNotReadEmail(child.email_index)){
                g_utility.addRedPoint(child, this.red_point, 180, 10);
            }
            else{
                g_utility.removeRedPoint(child);
            }
        }
    },
});
