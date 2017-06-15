var LayerUI = require('LayerUI');
var HeadTemplate = require('HeadTemplate');
var g_utility = require('g_utility');
var Player = require('Player');

cc.Class({
    extends: LayerUI,

    properties: {
        head_content:cc.Node,
        head_item_prefab:cc.Prefab,
    },

    onLoad: function () {
        this._super();
        this.easyHide = true;
        this.select_head_id = null;
    },

    onDestroy:function(){

    },

    onShow:function(event){
        
    },

    onHide:function(){

    },

    start:function(){
        for(var i = 1; i <= Object.keys(HeadTemplate).length; i++){
            var info = HeadTemplate[i];
            var head_item = cc.instantiate(this.head_item_prefab);
            head_item.parent = this.head_content;
            head_item.head_id = info.id;
            var select_img = head_item.getChildByName('select_img');
            select_img.active = false;
            var head_img = head_item.getChildByName('head_img');
            g_utility.setSpriteFrame(info.path, head_img.getComponent('cc.Sprite'));
            head_item.on('touchend', this.onHeadItemTouchEnd, this)
        }
    },

    onHeadItemTouchEnd:function(event){
        event.stopPropagation()
        var target = event.target;
        this.select_head_id = target.head_id;
        for(var i in this.head_content.children){
            var child = this.head_content.children[i];
            child.getChildByName('select_img').active = false;
        }
        target.getChildByName('select_img').active = true;
    },

    onOkBtnCallback:function(){
        if(this.select_head_id != null){
            Player.client.ChangePortraitReq(HeadTemplate[this.select_head_id].name);
        }
    }
});
