var LayerUI = require('LayerUI');
var HeadTemplate = require('HeadTemplate');
var g_utility = require('g_utility');

cc.Class({
    extends: LayerUI,

    properties: {
        info_label:cc.Label,
        head_img:cc.Sprite,
    },

    onLoad: function () {
        this._super();
        this.easyHide = true;
    },

    onDestory:function(){
        
    },

    onShow:function(event){
        var info = event.detail;
        this.info_label.string = "胜者："+info.nickname;
        var head_index = g_utility.getHeadIndexByName(info.portrait);
        if(head_index != null){
            g_utility.setSpriteFrame(HeadTemplate[head_index].path, this.head_img);
        }
        this.scheduleOnce(this.quitBattleScene, 5);
    },

    clickAndHide:function(){
        this._super();
        cc.director.loadScene('channel');
    },

    quitBattleScene:function(){
        cc.director.loadScene('channel');
    }
});
