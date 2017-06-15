var SceneUI = require('SceneUI');
var TexasGameData = require('TexasGameData_pb');
var Result = require('PickResult');
var Player = require('Player');
var GlobalNode = require('GlobalNode');
var g_const = require('g_const');
var g_global = require('g_global');

cc.Class({
    extends: SceneUI,

    properties: {
        role_nodes:[cc.Node],
        card_nodes:[cc.Node],
        butten_nodes:[cc.Node],
        pile_nodes:[cc.Node],
        check_btn:cc.Node,
        check_label:cc.Label,
        check_toggle:cc.Toggle,
        call_btn:cc.Node,
        call_label:cc.Label,
        call_toggle:cc.Toggle,
        raise_btn:cc.Node,
        raise_label:cc.Label,
        raise_toggle:cc.Toggle,
        allin_btn:cc.Node,
        allin_label:cc.Label,
        allin_toggle:cc.Toggle,
        fold_btn:cc.Node,
        fold_label:cc.Label,
        fold_toggle:cc.Toggle,
        confirm_btn:cc.Node,
        cancel_btn:cc.Node,
        raise_num_label:cc.Label,
        raise_slider:cc.Slider,
        btn_panel:cc.Node,
        bet_panel:cc.Node,
        back_btn:cc.Node,
        gold_panel:cc.Node,
        id_label:cc.Label,
        blinds_label:cc.Label,
        dealer:cc.Node,
        role_prefab:cc.Prefab,
        card_prefab:cc.Prefab,
        gold_prefab:cc.Prefab,
        pile_prefab:cc.Prefab,
        audio_m:[cc.AudioClip],
        audio_f:[cc.AudioClip],
        chat_btn:cc.Node,
        chat_panel:cc.Node,
        chat_scroll:cc.ScrollView,
        chat_input:cc.EditBox,
        chat_content:cc.Node,
        chat_label_prefab:cc.Prefab,
    },

    onLoad:function(){
        // 初始化
        this._super();
        this.roles = [];    // role节点对应的脚本
        this.cards = [];    // card节点对应的脚本
        this.seats = [];    // 座位
        // 需要经常初始化的
        this.stake = 0;             // 下注
        this.stake_progress = 0;    // 下注进度条
        this.pile_list = [];        // 钱堆列表
        this.winners = null;        // 结果
        this.cardTypes = null;      // 结果展示用的牌型
        this.perCards = null;       // 结果展示用的私有牌
        this.pubCards = null;       // 结果展示用的公有牌
        this.onTurn = false;        // 是否轮到自己
        // 注册
        Player.register('onTableInfo', this, 'onTableInfo');
        Player.register('onRoundInfo', this, 'onRoundInfo');
        Player.register('onCallActon', this, 'onCallActon');
        Player.register('onAction', this, 'onAction');
        Player.register('onSendCard', this, 'onSendCard');
        Player.register('onRoundResult', this, 'onRoundResult');
        Player.register('onSeatCardType', this, 'onSeatCardType');
        Player.register('onLeaveTable', this, 'onLeaveTable');
        Player.register('onSeatOffline', this, 'onSeatOffline');
        Player.register('onSeatOnline', this, 'onSeatOnline');
        Player.register('onGoldNotEnough', this, 'onGoldNotEnough');
        Player.register('onInvalidAction', this, 'onInvalidAction');
        Player.register('onBattleChat', this, 'onBattleChat');
    },

    onDestroy:function(){
        Player.deregister('onTableInfo', this, 'onTableInfo');
        Player.deregister('onRoundInfo', this, 'onRoundInfo');
        Player.deregister('onCallActon', this, 'onCallActon');
        Player.deregister('onAction', this, 'onAction');
        Player.deregister('onSendCard', this, 'onSendCard');
        Player.deregister('onRoundResult', this, 'onRoundResult');
        Player.deregister('onSeatCardType', this, 'onSeatCardType');
        Player.deregister('onLeaveTable', this, 'onLeaveTable');
        Player.deregister('onSeatOffline', this, 'onSeatOffline');
        Player.deregister('onSeatOnline', this, 'onSeatOnline');
        Player.deregister('onGoldNotEnough', this, 'onGoldNotEnough');
        Player.deregister('onInvalidAction', this, 'onInvalidAction');
        Player.deregister('onBattleChat', this, 'onBattleChat');
    },

    start:function(){
        var client = Player.client;
        var enm = TexasGameData.TableModeEnm;
        switch(client.tableMode){
            case enm.MODE_NORMAL_5:
            case enm.MODE_RICH_5:
            case enm.MODE_SUPER_RICH_5:
                var temp = [0, 1, 3, 4, 6];
                var role_temp = [];
                var butten_temp = [];
                var pile_temp = [];
                for(var i = 0; i < temp.length; i++){
                    role_temp.push(this.role_nodes[temp[i]]);
                    butten_temp.push(this.butten_nodes[temp[i]]);
                    pile_temp.push(this.pile_nodes[temp[i]]);
                }
                this.role_nodes = role_temp;
                this.butten_nodes = butten_temp;
                this.pile_nodes = pile_temp;
                break;
            case enm.MODE_NORMAL_7:
            case enm.MODE_RICH_7:
            case enm.MODE_SUPER_RICH_7:
                break;
        }
        // 生成预制件
        for(var i = 0; i < this.role_nodes.length; i++){
            var role = cc.instantiate(this.role_prefab);
            role.parent = this.role_nodes[i];
            role.active = false;
            if(i > this.role_nodes.length/2){
                role.getComponent('Role').setFlipByType(1);
            }
            var b_pos = role.convertToNodeSpaceAR(this.butten_nodes[i].convertToWorldSpaceAR(cc.p(0, 0)));
            var p_pos = role.convertToNodeSpaceAR(this.pile_nodes[i].convertToWorldSpaceAR(cc.p(0, 0)));
            role.getChildByName('button').position = b_pos;
            role.getChildByName('pile_node').position = p_pos;
            this.roles.push(role.getComponent('Role'));
        }
        for(var i = 0; i < this.card_nodes.length; i++){
            var card = cc.instantiate(this.card_prefab);
            card.parent = this.card_nodes[i];
            card.active = false;
            this.cards.push(card.getComponent('Card'));
        }
        // 初始化牌桌
        if(client.seats){
            this.onTableInfo(client.tableId, client.ownerIndex, client.seats, client.pubCards, client.gameTurn, client.gameTime, client.isRejoin);
        }
    },

    resetBattle:function(){
        this.stake = 0;             // 下注
        this.stake_progress = 0;    // 下注进度条
        this.pile_list = [];        // 钱堆列表
        this.winners = null;        // 结果
        this.cardTypes = null;      // 结果展示用的牌型
        this.perCards = null;       // 结果展示用的私有牌
        this.pubCards = null;       // 结果展示用的公有牌
        this.onTurn = false;        // 是否轮到自己
    },

    /////////////////////////////////////////////////////////////////////////////////////
    // 渲染事件
    /////////////////////////////////////////////////////////////////////////////////////
    onTableInfo:function(tableId, index, infos, pubCards, gameTurn, gameTime, isRejoin){
        this.id_label.string = "房间编号："+tableId;
        for(var i = this.roles.length-index; i < this.roles.length; i++){
            this.seats.push(this.roles[i]);
        }
        for(var i = 0; i < this.roles.length-index; i++){
            this.seats.push(this.roles[i]);
        }
        this.resetTableInfo(infos);
        this.showPubCard(pubCards);
        cc.error('on table info', infos);
        for(var i = 0; i < this.seats.length; i++){
            var info = infos[i];
            if(index == i){
                this.seats[i].setCardBgType(1);
            }
            else{
                this.seats[i].setCardBgType(0);
            }
            if(gameTurn == i && gameTime > 0){
                this.seats[i].showCount(gameTime);
            }
        }
    },

    onRoundInfo:function(infos, butten, blinds, index){
        this.blinds_label.string = "盲注："+blinds+"/"+(blinds*2);
        this.resetBattle();
        this.resetTableInfo(infos);
        for(var i = 0; i < this.seats.length; i++){
            this.seats[i].setCardBgType(0);
            if(butten == i){
                this.seats[i].setButtonActive(true);
            }
            else{
                this.seats[i].setButtonActive(false);
            }
        }
        var count = 0;
        for(var i = 0; i < 2; i++){
            for(var j = 0; j < this.seats.length; j++){
                if(infos[j].username != '' && !infos[j].isout){
                    var type = j==index?1:0;
                    var p = this.seats[j].card_nodes[i].convertToNodeSpaceAR(this.dealer.convertToWorldSpaceAR(cc.p(0, 0)));
                    this.seats[j].playCardAnimation(i, p, count*0.1, type);
                    count ++;
                }
            }
        }
    },

    onCallActon:function(index, ownerIndex){
        this.onTurn = index == ownerIndex;
        if(index == ownerIndex){
            this.showBtnPanel();
        }
        else{
            this.showBtnPanel(true, true);
        }
        this.seats[index].showCount();
        this.seats[index].setMsg("思考中");
    },

    onAction:function(index, info, bet, needShow, change, delta){
        this.seats[index].hideCount();
        this.seats[index].setTotalMoney(info.gold);
        this.seats[index].setMsg(g_const.ACTION2NAME[info.action]);
        // if(needShow){
        //     this.seats[index].showBet(bet);
        // }
        // else{
        //     this.seats[index].addBet(bet);
        // }
        var enm = TexasGameData.ActionEnm;
        switch(info.action){
            case enm.CHECK:
                cc.audioEngine.play(this.audio_f[0]);
                break;
            case enm.CALL:
                cc.audioEngine.play(this.audio_f[1]);
                break;
            case enm.RAISE:
                cc.audioEngine.play(this.audio_f[2]);
                break;
            case enm.ALLIN:
                cc.audioEngine.play(this.audio_f[3]);
                break;
            case enm.FOLD:
                cc.audioEngine.play(this.audio_f[4]);
                break;
        }
        if(info.action == enm.FOLD){
            for(var i = 0; i < 2; i++){
                var p = this.seats[index].card_nodes[i].convertToNodeSpaceAR(this.node.convertToWorldSpaceAR(cc.p(0, 0)));
                this.seats[index].playCardAnimation2(i, p)
            }
        }
        if(needShow){
            this.seats[index].showBet(bet);
        }
        if(change){
            this.call_toggle.isChecked = false;
        }
        this.call_label.string = "跟注"+delta;
        // this.hideBtnPanel();
        this.hideBetPanel();
    },

    onSendCard:function(card, index, piles, names){
        var p = this.cards[index].node.convertToNodeSpaceAR(this.dealer.parent.convertToWorldSpaceAR(this.dealer.position));
        this.cards[index].setSuitAndPointByNum(card);
        this.cards[index].setBgType(0);
        // this.cards[index].node.x = p.x;
        this.cards[index].node.y = g_const.LONG_DISCTANCE;
        this.cards[index].node.active = true;
        this.cards[index].node.runAction(cc.sequence(
            cc.delayTime(0.5),
            cc.callFunc(function(target){
                target.x = p.x;
                target.y = p.y;
            }),
            cc.moveTo(0.5, 0, 0),
            cc.callFunc(function(target){
                target.getComponent('Card').setBgType(1);
            })));
        this.setPile(piles);
        // for(var i = 0; i < names.length; i++){
        //     this.roles[i].setMsg(names[i]);
        // }
    },

    onSeatCardType:function(index, type, cards, pubCards, isout){
        if(arguments.length == 1){
            this.seats[index].setCardType(-1);
            this.seats[index].setSelectImgActive(false);
            this.seats[index].setCardLight(true);
            for(var i = 0; i < this.cards.length; i++){
                this.cards[i].setSelectImgActive(false);
                this.cards[i].setCardLight(true);
            }
        }
        else{
            this.seats[index].setCardType(type[0]);
            var temp = type.slice(1);
            for(var i = 0; i < cards.length; i++){
                if(temp.indexOf(cards[i]) == -1){
                    this.seats[index].setSelectImgActive(false, i);
                    this.seats[index].setCardLight(false, i);
                }
                else{
                    this.seats[index].setSelectImgActive(true, i);
                    this.seats[index].setCardLight(true, i);
                }
            }
            for(var i = 0; i < pubCards.length; i++){
                if(temp.indexOf(pubCards[i]) == -1){
                    this.cards[i].setSelectImgActive(false);
                    this.cards[i].setCardLight(false);
                }
                else{
                    this.cards[i].setSelectImgActive(true);
                    this.cards[i].setCardLight(true);
                }
            }
        }
    },

    onRoundResult:function(winners, states, cards, types, piles, pubCards){
        this.hideBtnPanel();
        this.hideBetPanel();
        for(var i = 0; i < cards.length; i++){
            if(states[i]){
                this.seats[i].setCardBgType(1);
                this.seats[i].setCardType(types[i][0]);
                this.seats[i].setSelectImgActive(false);
                this.seats[i].setCardLight(true);
                for(var j = 0; j < cards[i].length; j++){
                    this.seats[i].setCardByNum(j, cards[i][j]);
                }
            }
        }
        this.setPile(piles);
        for(var i = 0; i < pubCards.length; i++){
            this.cards[i].setSelectImgActive(false);
            this.cards[i].setCardLight(true);
        }
        this.winners = winners;
        this.perCards = cards;
        this.cardTypes = types;
        this.pubCards = pubCards;
        this.rewardPile();
        this.schedule(this.rewardPile, 1, piles.length-2);
    },

    onLeaveTable:function(index, ownerIndex, winner){
        this.seats[index].node.active = false;
        if(index == ownerIndex){
            if(winner){
                this.scene_manager.showUI('result_ui', winner)
            }
            else{
                cc.director.loadScene('channel');
            }
        }
    },

    onSitDownSeatInfo:function(index, info){
        this.seats[index].setInfo(info);
    },

    onGoldNotEnough:function(){
        this.scene_global.showUI('message_ui', '钱不够');
    },

    onInvalidAction:function(){
        this.scene_global.showUI('message_ui', '非法操作');
    },

    onSeatOffline:function(seat, isOffline){

    },

    onSeatOnline:function(seat){

    },

    onBattleChat:function(seat, nickname, msg){
        var chat_label = cc.instantiate(this.chat_label_prefab);
        chat_label.parent = this.chat_content;
        chat_label.getComponent('cc.Label').string = nickname+':'+msg;
        this.scheduleOnce(this.scrollMoveToBotton, 0);
        this.seats[seat].chat(msg);
    },

    /////////////////////////////////////////////////////////////////////////////////////
    // 辅助渲染
    /////////////////////////////////////////////////////////////////////////////////////
    resetTableInfo:function(infos){
        for(var i = 0; i < this.cards.length; i++){
            this.cards[i].setCardLight(true);
            this.cards[i].setSelectImgActive(false);
            this.cards[i].node.active = false;
            this.cards[i].node.x = 0;
            this.cards[i].node.y = 0;
        }
        for(var i = 0; i < this.seats.length; i++){
            if(infos[i].username == ""){
                this.seats[i].node.active = false;
                continue;
            }
            this.seats[i].node.active = true;
            this.seats[i].setInfo(infos[i]);
            if(infos[i].isout){
                this.seats[i].hideCard();
                this.seats[i].color = cc.color(180, 180, 180);
            }
            else{
                if(this.seats.length == 0){
                    this.seats[i].hideCard();
                }
                else{
                    this.seats[i].showCard();
                }
                this.seats[i].color = cc.color(255, 255, 255);
            }
        }
        this.resetBtnToggle();
        this.hideBtnPanel();
        this.hideBetPanel();
    },

    showPubCard:function(pubCards){
        for(var i = 0; i < pubCards.length; i++){
            this.cards[i].node.active = true;
            this.cards[i].setBgType(1);
            this.cards[i].setSuitAndPointByNum(pubCards[i]);
        }
    },

    setPile:function(piles){
        for(var i = 0; i < this.seats.length; i++){
            var pos = this.gold_panel.convertToNodeSpaceAR(this.dealer.convertToWorldSpaceAR(cc.p(0, 0)));
            var gold_list = this.seats[i].getGoldList(this.gold_panel);
            for(var j = 0; j < gold_list.length; j++){
                gold_list[j].getComponent('Gold').useGoldAnimation(pos, j*0.1, 0.5);
            }
            this.seats[i].hideBet();
        }
        for(var i = 0; i < this.pile_list.length; i++){
            GlobalNode.pile_pool.put(this.pile_list[i]);
        }
        this.pile_list = [];
        for(var i = 0; i < piles.length; i++){
            var pile;
            if(GlobalNode.pile_pool.size() > 0){
                pile = GlobalNode.pile_pool.get();
            }
            else{
                pile = cc.instantiate(this.pile_prefab);
            }
            pile.parent = this.gold_panel;
            pile.x = this.pile_list.length*80;
            this.pile_list.push(pile);
            pile.getComponent('Pile').showGold(piles[i]);
        }
    },

    rewardPile:function(){
        var index = this.winners.length-this.pile_list.length;
        var winners = this.winners[index];
        var pile = this.pile_list.shift();
        GlobalNode.pile_pool.put(pile);
        for(var i = 0; i < this.cards.length; i++){
            this.cards[i].setSelectImgActive(false);
            this.cards[i].setCardLight(false);
        }
        for(var i = 0; i < this.seats.length; i++){
            this.seats[i].setSelectImgActive(false);
            this.seats[i].setCardLight(false);
        }
        for(var i = 0; i < winners.length; i++){
            var seat = winners[i].seat;
            var gold = winners[i].gold;
            if(GlobalNode.pile_pool.size() > 0){
                pile = GlobalNode.pile_pool.get();
            }
            else{
                pile = cc.instantiate(this.pile_prefab);
            }
            pile.parent = this.gold_panel;
            pile.getComponent('Pile').showGold(gold);
            var pos = this.gold_panel.convertToNodeSpaceAR(this.seats[seat].node.parent.convertToWorldSpaceAR(cc.p(0, 0)));
            var gold_list = pile.getComponent('Pile').getGoldList(this.gold_panel);
            for(var j = 0; j < gold_list.length; j++){
                gold_list[j].getComponent('Gold').useGoldAnimation(pos, j*0.1, 0.5);
            }
            GlobalNode.pile_pool.put(pile);
            this.seats[seat].showWinLabel(gold);
            var cards = this.perCards[seat];
            var type = this.cardTypes[seat];
            var temp = type.slice(1);
            for(var j = 0; j < cards.length; j++){
                if(temp.indexOf(cards[j]) == -1){
                    this.seats[seat].setSelectImgActive(false, j);
                    this.seats[seat].setCardLight(false, j);
                }
                else{
                    this.seats[seat].setSelectImgActive(true, j);
                    this.seats[seat].setCardLight(true, j);
                }
            }
            for(var j = 0; j < this.pubCards.length; j++){
                if(temp.indexOf(this.pubCards[j]) != -1){
                    this.cards[j].setSelectImgActive(true);
                    this.cards[j].setCardLight(true);
                }
            }
        }
        for(var i = 0; i < this.pile_list.length; i++){
            this.pile_list[i].active = true;
            this.pile_list[i].runAction(cc.moveTo(0.5, i*20, 0));
        }
    },

    resetBtnToggle:function(){
        this.check_toggle.isChecked = false;
        this.call_toggle.isChecked = false;
        this.raise_toggle.isChecked = false;
        this.allin_toggle.isChecked = false;
        this.fold_toggle.isChecked = false;
    },

    showBtnPanel:function(need = true, isPre = false){
        var client = Player.client;
        this.btn_panel.active = true;
        if(need && !isPre){
            this.check_btn.active   = client.canCheck();
            this.call_btn.active    = client.canCall();
            this.raise_btn.active   = client.canRaise();
            this.allin_btn.active   = client.canAllin1();
            this.fold_btn.active    = client.canFold();
            this.check_label.node.x = 0;
            this.call_label.node.x = 0;
            this.raise_label.node.x = 0;
            this.allin_label.node.x = 0;
            this.fold_label.node.x = 0;
            this.check_toggle.node.active = false;
            this.call_toggle.node.active = false;
            this.raise_toggle.node.active = false;
            this.allin_toggle.node.active = false;
            this.fold_toggle.node.active = false;
            this.raise_label.string = "加注";
            //this.call_label.string = "跟注";
            if(this.check_btn.active && this.check_toggle.isChecked){
                this.checkBtnCallback();
            }
            else if(this.call_btn.active && this.call_toggle.isChecked){
                this.callBtnCallback();
            }
            else if(this.raise_btn.active && this.raise_toggle.isChecked){
                if(this.check_btn.active){
                    this.checkBtnCallback();
                }
                else if(this.call_btn.active){
                    this.callBtnCallback();
                }
                else if(this.allin_btn.active){
                    this.allinBtnCallback();
                }
            }
            else if(this.allin_btn.active && this.allin_toggle.isChecked){
                this.allinBtnCallback();
            }
            else if(this.fold_btn.active && this.fold_toggle.isChecked){
                this.foldBtnCallback();
            }
            this.resetBtnToggle();
        }
        else if(need && isPre){
            this.check_btn.active   = client.canCheck(isPre);
            this.call_btn.active    = client.canCall(isPre);
            this.raise_btn.active   = client.canRaise(isPre);
            this.allin_btn.active   = client.canAllin1(isPre);
            this.fold_btn.active    = client.canFold(isPre);
            this.check_label.node.x = 15;
            this.call_label.node.x = 15;
            this.raise_label.node.x = 15;
            this.allin_label.node.x = 15;
            this.fold_label.node.x = 15;
            this.check_toggle.node.active = true;
            this.call_toggle.node.active = true;
            this.raise_toggle.node.active = true;
            this.allin_toggle.node.active = true;
            this.fold_toggle.node.active = true;
            this.raise_label.string = "跟任何注";
        }
    },

    hideBtnPanel:function(){
        this.btn_panel.active = false;
    },

    showBetPanel:function(){
        this.stake = 0;
        this.stake_progress = 0;
        this.bet_panel.active = true;
        this.raise_slider.progress = 0;
        this.raise_num_label.string = 0;
    },

    hideBetPanel:function(){
        this.bet_panel.active = false;
    },

    scrollMoveToBotton:function(){
        if(this.chat_content.height > this.chat_content.parent.height){
            this.chat_scroll.scrollToBottom();
        }
    },

    /////////////////////////////////////////////////////////////////////////////////////
    // UI事件
    /////////////////////////////////////////////////////////////////////////////////////
    checkBtnCallback:function(){
        if(this.onTurn){
            Player.client.ActionReq(TexasGameData.ActionEnm.CHECK);
            this.hideBtnPanel();
        }
        else{
            this.check_toggle.isChecked = !this.check_toggle.isChecked;
            if(this.check_toggle.isChecked){
                this.resetBtnToggle();
                this.check_toggle.isChecked = true;
            }
        }
    },

    callBtnCallback:function(){
        if(this.onTurn){
            Player.client.ActionReq(TexasGameData.ActionEnm.CALL);
            this.hideBtnPanel();
        }
        else{
            this.call_toggle.isChecked = !this.call_toggle.isChecked;
            if(this.call_toggle.isChecked){
                this.resetBtnToggle();
                this.call_toggle.isChecked = true;
            }
        }
    },

    raiseBtnCallback:function(){
        if(this.onTurn){
            this.hideBtnPanel();
            this.showBetPanel();
        }
        else{
            this.raise_toggle.isChecked = !this.raise_toggle.isChecked;
            if(this.raise_toggle.isChecked){
                this.resetBtnToggle();
                this.raise_toggle.isChecked = true;
            }
        }
    },

    allinBtnCallback:function(){
        if(this.onTurn){
            Player.client.ActionReq(TexasGameData.ActionEnm.ALLIN);
            this.hideBtnPanel();
        }
        else{
            this.allin_toggle.isChecked = !this.allin_toggle.isChecked;
            if(this.allin_toggle.isChecked){
                this.resetBtnToggle();
                this.allin_toggle.isChecked = true;
            }
        }
    },

    foldBtnCallback:function(){
        if(this.onTurn){
            Player.client.ActionReq(TexasGameData.ActionEnm.FOLD);
            this.hideBtnPanel();
        }
        else{
            this.fold_toggle.isChecked = !this.fold_toggle.isChecked;
            if(this.fold_toggle.isChecked){
                this.resetBtnToggle();
                this.fold_toggle.isChecked = true;
            }
        }
    },

    confirmBtnCallback:function(){
        var client = Player.client;
        if(this.stake_progress == 0){
            this.showBtnPanel(false);
        }
        else if(this.stake_progress == 1){
            client.ActionReq(TexasGameData.ActionEnm.ALLIN);
        }
        else{
            client.ActionReq(TexasGameData.ActionEnm.RAISE, this.stake + client.currentBet - client.seats[client.ownerIndex].bet);
        }
        this.hideBetPanel();
    },

    cancelBtnCallback:function(){
        this.hideBetPanel();
        this.showBtnPanel(false);
    },

    raiseSliderCallback:function(slider, event){
        var client = Player.client;
        var seat = client.seats[client.ownerIndex];
        var bet = client.currentBet;
        this.stake = parseInt((seat.gold+seat.bet-bet)*slider.progress);
        if(slider.progress > 0 && slider.progress < 1){
            if(this.stake == 0){
                this.stake = 1;
            }
            else if(this.stake == seat.gold+seat.bet-bet){
                this.stake = seat.gold+seat.bet-bet-1;
            }
        }
        this.stake_progress = slider.progress;
        this.raise_num_label.string = this.stake;
    },

    onChatBtnCallback:function(){
        this.chat_panel.active = true;
        this.chat_btn.active = false;
    },

    onChatCloseBtnCallback:function(){
        this.chat_panel.active = false;
        this.chat_btn.active = true;
    },

    onSendBtnCallback:function(){
        if(this.chat_input.string.length <= 0){
            return;
        }
        if(g_global.canSendChat()){
            Player.client.BattleChatReq(this.chat_input.string);
            this.chat_input.string = "";
        }
        else{
            this.scene_global.showUI('message_ui', '发送消息频率过高（需要间隔'+ (g_const.CHAT_INTERVAL/g_const.SECOND) +'s）');
        }
    },

    backBtnCallback:function(){
        Player.client.LeaveTableReq();
    },
});
