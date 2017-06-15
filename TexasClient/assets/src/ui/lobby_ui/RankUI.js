var LayerUI = require('LayerUI');
var GameData = require('GameData_pb');
var Player = require('Player');
var HeadTemplate = require('HeadTemplate');
var g_utility = require('g_utility');

cc.Class({
    extends: LayerUI,

    properties: {
        rank_btn:cc.Prefab,
        title_label:cc.Label,
        rank_content:cc.Node,
        left_btn:cc.Node,
        right_btn:cc.Node,

        num1_label:cc.Label,
        num2_label:cc.Label,
    },

    onLoad: function () {
        this._super();
        this.easyHide = true;
        this.rankType = 0;
        this.startIndex = 0;
        this.num1_label.string = 1;
        this.num2_label.string = 10
        Player.register('setRankList', this, 'setRankList');
    },

    onDestroy:function(){
        Player.deregister('setRankList', this, 'setRankList');
    },

    onShow:function(event){
        this.rankType = event.detail;
        if(this.rankType == 1){
            this.title_label.string = "财富榜";
        }
        else if(this.rankType == 2){
            this.title_label.string = "胜率榜";
        }
        Player.client.GetRankListReq(this.rankType, this.startIndex);
    },

    onHide:function(){

    },

    setRankList:function(type, startIndex, rankItems){
        var enm = GameData.RankTypeEnm;
        this.rank_content.removeAllChildren();
        for(var i = 0; i < rankItems.length; i++){
            var item = rankItems[i];
            var btn = cc.instantiate(this.rank_btn);
            var b_l = btn.getChildByName('Label').getComponent('cc.Label');
            if(type == enm.GOLD_RANK){
                b_l.string = "用户名："+item.username+'\n排名：'+(startIndex+i+1)+"\n金币："+item.gold;
            }
            else if(type == enm.WIN_RANK){
                if(item.gamecount == 0){
                    b_l.string = "用户名："+item.username+'\n排名：'+(startIndex+i+1)+"\n胜率：无";
                }
                else{
                    b_l.string = "用户名："+item.username+'\n排名：'+(startIndex+i+1)+"\n胜率："+(item.wincount/item.gamecount);
                }
            }
            btn.parent = this.rank_content;
            var head_img = btn.getChildByName('head_img').getComponent('cc.Sprite');
            var head_index = g_utility.getHeadIndexByName(item.portrait);
            if(head_index != null){
                g_utility.setSpriteFrame(HeadTemplate[head_index].path, head_img);
            }
        }
    },

    onBtnCallback:function(event, data){
        var off = parseInt(data);
        this.startIndex += off;
        if(this.startIndex < 0){
            this.startIndex = 0;
        }
        if(this.startIndex > 90){
            this.startIndex = 90;
        }
        this.num1_label.string = this.startIndex+1;
        this.num2_label.string = this.startIndex+10;
        Player.client.GetRankListReq(this.rankType, this.startIndex);
    },
});
