var g_const = require('g_const');
var g_utility = require('g_utility');
var Result = require('PickResult');
var GlobalNode = require('GlobalNode');
var TexasGameData = require('TexasGameData_pb');
var HeadTemplate = require('HeadTemplate');

cc.Class({
    extends: cc.Component,

    properties: {
        portrait_img:cc.Sprite,

        total_money_label:cc.Label,
        name_label:cc.Label,
        msg_label:cc.Label,
        type_label:cc.Label,
        chat_label:cc.Label,
        win_label:cc.Label,

        count_progress:cc.ProgressBar,

        button:cc.Node,
        win_img:cc.Node,
        chat_node:cc.Node,
        pile_node:cc.Node,
        card_nodes:[cc.Node],

        type_color:[cc.Color],

        card_prefab:cc.Prefab,
        pile_prefab:cc.Prefab,
    },

    onLoad: function () {
        this.total_gold = 0;
        this.isCounting = false;
        this.pile = null;
        this.msg = "";
        this.msgIndex = 0;
        this.msgCount = 0;
        this.card_list = [];
        for(var i = 0; i < this.card_nodes.length; i++){
            var card = cc.instantiate(this.card_prefab);
            card.parent = this.card_nodes[i];
            this.card_list.push(card.getComponent('Card'));
        }
    },

    onDestroy:function(){
        if(this.pile){
            GlobalNode.pile_pool.put(this.pile.node);
            this.pile = null;
        }
    },

    setInfo:function(info){
        this.resetInfo();
        var enm = TexasGameData.ActionEnm;

        this.total_money_label.string = info.gold;
        this.name_label.string = info.nickname;
        // this.msg_label.string = info.username;
        if(info.isout){
            this.msg_label.string = "淘汰";
        }
        else{
            this.msg_label.string = g_const.ACTION2NAME[info.action];
        }

        this.showBet(info.bet, true);
        for(var i = 0; i < info.cardsList.length; i++){
            this.setCardByNum(i, info.cardsList[i]);
            this.showCard(i);
        }
        var head_index = g_utility.getHeadIndexByName(info.portrait);
        if(head_index != null){
            g_utility.setSpriteFrame(HeadTemplate[head_index].path, this.portrait_img);
        }
    },

    resetInfo:function(){
        this.total_money_label.string = 0;
        this.name_label.string = "";
        this.msg_label.string = "";
        this.type_label.string = "";

        this.win_label.node.active = false;
        this.win_img.active = false;
        
        this.hideBet();
        this.setSelectImgActive(false);
        this.setCardLight(true);
        this.hideCard();
        this.hideCount();
    },

    setFlipByType:function(type){
        switch(type){
            case 0:
                this.node.scaleX = 1;
                this.portrait_img.node.scaleX = 1;
                this.total_money_label.node.scaleX = 1;
                this.name_label.node.scaleX = 1;
                this.msg_label.node.scaleX = 1;
                for(var i = 0; i < this.card_nodes.length; i++){
                    this.card_nodes[i].scaleX = 0.8;
                    this.card_nodes[i].zIndex = i+1;
                }
                this.count_progress.node.scaleX = 1;
                this.type_label.node.scaleX = 1;
                this.type_label.node.anchorX = 0;
                this.pile_node.scaleX = 1;
                this.pile_node.zIndex = this.card_nodes.length;
                this.chat_label.node.scaleX = 1;
                this.win_label.node.scaleX = 1;
                break;
            case 1:
                this.node.scaleX = -1;
                this.portrait_img.node.scaleX = -1;
                this.total_money_label.node.scaleX = -1;
                this.name_label.node.scaleX = -1;
                this.msg_label.node.scaleX = -1;
                for(var i = 0; i < this.card_nodes.length; i++){
                    this.card_nodes[i].scaleX = -0.8;
                    this.card_nodes[i].zIndex = this.card_nodes.length-i;
                }
                this.count_progress.node.scaleX = -1;
                this.type_label.node.scaleX = -1;
                this.type_label.node.anchorX = 1;
                this.pile_node.scaleX = -1;
                this.pile_node.zIndex = this.card_nodes.length;
                this.chat_label.node.scaleX = -1;
                this.win_label.node.scaleX = -1;
                break;
        }
    },

    showCard:function(index = null){
        if(index == null){
            for(var i = 0; i < this.card_nodes.length; i++){
                this.card_nodes[i].active = true;
            }
        }
        else{
            this.card_nodes[index].active = true;
        }
    },

    hideCard:function(index = null){
        if(index == null){
            for(var i = 0; i < this.card_nodes.length; i++){
                this.card_nodes[i].active = false;
            }
        }
        else{
            this.card_nodes[index].active = false;
        }
    },

    setCardBgType:function(type, index){
        if(index == null){
            for(var i = 0; i < this.card_list.length; i++){
                this.card_list[i].setBgType(type);
            }
        }
        else{
            this.card_list[index].setBgType(type);
        }
    },

    setCard:function(index, suit, point){
        this.card_list[index].setSuitAndPoint(suit, point);
    },

    setCardByNum:function(index, num){
        if(num < 0){
            return;
        }
        var v = Result.getSuitAndPoint(num);
        this.setCard(index, v.suit, v.point);
    },

    playCardAnimation:function(index, from, delay, type){
        var node = this.card_list[index].node;
        node.stopAllActions();
        node.position = from;
        node.active = true;
        node.runAction(
            cc.sequence(
                cc.delayTime(delay), 
                cc.spawn(cc.rotateBy(0.5, 360), cc.moveTo(0.5, cc.p(0, 0))),
                cc.callFunc(function(target, type){
                    this.setBgType(type);
                }, this.card_list[index], type)
            )
        );
    },

    playCardAnimation2:function(index, to){
        var node = this.card_list[index].node;
        this.card_list[index].setBgType(0);
        node.stopAllActions();
        node.position = cc.p(0, 0);
        node.active = true;
        node.runAction(
            cc.sequence(
                cc.spawn(
                    cc.rotateBy(0.5, 360),
                    cc.moveTo(0.5, to)
                ),
                cc.callFunc(function(target){
                    target.active = false;
                })
            )
        );
    },

    setPortrait:function(protrait){
        this.portrait_img.spriteFrame = protrait;
    },

    setTotalMoney:function(money){
        this.total_gold = money;
        this.total_money_label.string = money;
    },

    setName:function(name){
        this.name_label.string = name;
    },

    setMsg:function(msg){
        this.msg_label.string = msg;
    },

    showCount:function(time_left){
        if(this.isCounting == false){
            this.count_progress.node.active = true;
            if(time_left == null){
                this.count = 0;
                this.count_progress.progress = 0;
            }
            else{
                this.count = g_const.TIME_COUNT_LIMIT-time_left/g_const.SECOND;
                this.count_progress.progress = this.count / g_const.TIME_COUNT_LIMIT;
            }
            this.schedule(this.count_update, 0);
            this.isCounting = true;
        }
    },

    hideCount:function(){
        if(this.isCounting == true){
            this.count_progress.node.active = false;
            this.unschedule(this.count_update);
            this.isCounting = false;
        }
    },

    count_update:function(dt){
        this.count += dt;
        if(this.count >= g_const.TIME_COUNT_LIMIT){
            this.hideCount();
        }
        else{
            this.count_progress.progress = this.count / g_const.TIME_COUNT_LIMIT;
        }
    },

    setCardType:function(type){
        if(type == -1 || type == undefined){
            this.type_label.string = "";
        }
        else{
            this.type_label.string = Result.type2Name[type];
            this.type_label.node.color = this.type_color[type];
        }
    },

    showBet:function(num, need){
        if(this.pile){
            this.hideBet();
        }
        var pile;
        if(GlobalNode.pile_pool.size() > 0){
            pile = GlobalNode.pile_pool.get();
        }
        else{
            pile = cc.instantiate(this.pile_prefab);
        }
        pile.parent = this.pile_node;
        this.pile = pile.getComponent('Pile');
        var p = this.pile.node.convertToNodeSpaceAR(this.node.convertToWorldSpaceAR(cc.p(0, 0)));
        if(need){
            this.pile.showGold(num);
        }
        else{
            this.pile.showGoldAnimation(num, p)
        }
    },

    addBet:function(num, need){
        if(this.pile){
            var p = this.pile.node.convertToNodeSpaceAR(this.node.convertToWorldSpaceAR(cc.p(0, 0)));
            if(need){
                this.pile.addGold(num);
            }
            else{
                this.pile.addGoldAnimation(num, p);
            }
        }
        else{
            this.showBet(num);
        }
    },

    hideBet:function(){
        if(this.pile){
            GlobalNode.pile_pool.put(this.pile.node);
            this.pile = null;
        }
    },

    getGoldList:function(panel){
        if(this.pile){
            return this.pile.getGoldList(panel);
        }
        else{
            return [];
        }
    },

    setButtonActive:function(active){
        this.button.active = active;
    },

    chat:function(msg){
        if(msg == ""){
            return;
        }
        if(this.msg != ""){
            this.unschedule(this.showChat);
        }
        var len = msg.length;
        this.msg = msg;
        this.msgIndex = 0;
        this.msgCount = 0;
        var n = len-3;
        if(n <= 0){
            n = 1;
        }
        this.chat_node.active = true;
        this.chat_label.string = "";
        this.showChat();
        this.schedule(this.showChat, 1, n*5-1);
    },

    showChat:function(){
        if(this.msgCount == 5){
            this.msg = "";
            this.chat_node.active = false;
        }
        var len = this.msg.length;
        var msg = this.msg.slice(this.msgIndex, this.msgIndex+4);
        this.chat_label.string = msg;
        this.msgIndex++;
        if(this.msgIndex+4 > len){
            this.msgIndex = 0;
            this.msgCount++;
        }
    },

    setSelectImgActive:function(active, index){
        if(index == null){
            for(var i = 0; i < this.card_list.length; i++){
                this.card_list[i].setSelectImgActive(active);
            }
        }
        else{
            this.card_list[index].setSelectImgActive(active);
        }
    },

    setWinImgActive:function(active){
        this.win_img.active = active;
    },

    showWinLabel:function(num){
        this.setTotalMoney(this.total_gold+num);
        this.setWinImgActive(true);
        this.win_label.node.active = true;
        this.win_label.node.y = 0;
        this.win_label.string = "+"+num;
        this.win_label.node.runAction(cc.sequence(cc.moveBy(1, 0, 150),
            cc.callFunc(function(target){
                target.active = false;
                this.setWinImgActive(false);
            }, this)));
    },

    setCardLight:function(light, index){
        if(index == null){
            for(var i = 0; i < this.card_list.length; i++){
                this.card_list[i].setCardLight(light);
            }
        }
        else{
            this.card_list[index].setCardLight(light);
        }
    }
});
