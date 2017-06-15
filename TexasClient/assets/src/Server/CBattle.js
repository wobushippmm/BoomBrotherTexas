var SOSClient = require('SOSClient');
var GameData = require('GameData_pb');
var TexasGameData = require('TexasGameData_pb');
var Result = require('PickResult');

var CBattle = SOSClient.extend({
    /////////////////////////////////////////////////////////////////////////////////////
    // 数据
    /////////////////////////////////////////////////////////////////////////////////////
    ctor:function(){
        this._super();
        this.tableMode = null;
        this.tableId = null;
        this.seats = null;
        this.pubCards = null;
        this.butten = null;
        this.blinds = null;
        this.currentSeat = null;
        this.ownerIndex = null;
        this.currentBet = null;
        this.lastBet = null;
        this.pile = null;
        this.gameTurn = null;
        this.gameTime = null;
        this.isRejoin = null;
    },

    /////////////////////////////////////////////////////////////////////////////////////
    // Server->Client
    /////////////////////////////////////////////////////////////////////////////////////
    TableInfoRep:function(obj){
        var id = obj.id;
        var seats = obj.seatsList;
        var pubCards = obj.pubcardsList?obj.pubcardsList:[];
        var currentBet = obj.currbet?obj.currbet:0;
        var mode = obj.mode;
        var turn = obj.turn;
        var time = obj.time;
        var isRejoin = obj.isrejoin;
        this.tableMode = mode;
        this.tableId = id;
        this.seats = seats;
        this.pubCards = pubCards;
        this.currentBet = currentBet;
        this.gameTurn = turn;
        this.gameTime = time;
        this.isRejoin = isRejoin;
        this.lastBet = 0;
        this.pile = [];
        for(var i = 0; i < seats.length; i++){
            if(seats[i].username == this.username){
                this.ownerIndex = i;
            }
            if(!seats[i].cardsList){
                seats[i].cardsList = [];
            }
        }
        this.event.fire('goBattle');
    },

    StartRoundRep:function(obj){
        var pubCards = obj.pubcardsList;
        var seatCards = obj.seatcardsList;
        var butten = obj.butten;
        var blinds = obj.blinds;
        this.pubCards = pubCards;
        this.butten = butten;
        this.blinds = blinds;
        this.currentBet = 0;
        this.lastBet = 0;
        this.pile = [];
        for(var i = 0; i < this.seats.length; i++){
            if(i == this.ownerIndex){
                this.seats[i].cardsList = seatCards;
            }
            this.seats[i].bet = 0;
            this.seats[i].action = 0;
            if(this.seats[i].gold == 0){
                this.seats[i].isout = true;
            }
        }
        this.event.fire('onRoundInfo', this.seats, butten, blinds, this.ownerIndex);
    },

    CallActionRep:function(obj){
        var seat = obj.seat;
        this.currentSeat = seat;
        this.event.fire('onCallActon', seat, this.ownerIndex);
    },

    SendCardRep:function(obj){
        var enm = TexasGameData.ActionEnm;
        var card = obj.card;
        this.pubCards.push(card);
        this.lastBet = this.currentBet;
        var names = [];
        for(var i = 0; i < this.seats.length; i++){
            names.push(this.seats[i].username);
        }
        this.event.fire('onSendCard', card, this.pubCards.length-1, this.getPileList(), names);
        if(this.pubCards.length >= 3){
            if(this.seats[this.ownerIndex].action == enm.FOLD || this.seats[this.ownerIndex].isout){
                this.event.fire('onSeatCardType', this.ownerIndex);
            }
            else{
                this.event.fire('onSeatCardType', this.ownerIndex, this.getSeatCardType(this.ownerIndex), this.seats[this.ownerIndex].cardsList, this.pubCards);
            }
        }
    },

    ActionRep:function(obj){
        var result = obj.result;
        var seat = obj.seat;
        var action = obj.action;
        var gold = obj.gold;
        var bet = obj.bet;
        var enm = TexasGameData.ActionResultEnm;
        var enm2 = TexasGameData.ActionEnm;
        var old = this.seats[seat].bet;
        var change = false;
        switch(result){
            case enm.OK_ACTIONRESULT:
                switch(action){
                    case enm2.WAIT:
                        break;
                    case enm2.CHECK:
                        break;
                    case enm2.CALL:
                        this.seats[seat].gold = gold;
                        this.seats[seat].bet = bet;
                        break;
                    case enm2.RAISE:
                        this.seats[seat].gold = gold;
                        this.seats[seat].bet = bet;
                        if(this.currentBet < bet){
                            this.currentBet = bet;
                            change = true;
                        }
                        break;
                    case enm2.ALLIN:
                        this.seats[seat].gold = gold;
                        this.seats[seat].bet = bet;
                        if(this.currentBet < bet){
                            this.currentBet = bet;
                            change = true;
                        }
                        break;
                    case enm2.FOLD:
                        break;
                }
                this.seats[seat].action = action;
                this.event.fire('onAction', 
                    seat, 
                    this.seats[seat], 
                    // bet-old, 
                    bet-this.lastBet,
                    // old==this.lastBet, 
                    old<bet,
                    change, 
                    this.currentBet-this.seats[this.ownerIndex].bet);
                break;
            case enm.GOLD_NOT_ENOUGH:
                if(seat == this.ownerIndex){
                    this.event.fire('onGoldNotEnough');
                }
                break;
            case enm.ACTION_INVALIDITY:
                if(seat == this.ownerIndex){
                    this.event.fire('onInvalidAction');
                }
                break;
        }
    },

    RoundResultRep:function(obj){
        var enm = TexasGameData.ActionEnm;
        var winners = obj.winnersList;
        var seats = obj.seatsList;
        for(var i = 0; i < winners.length; i++){
            this.seats[winners[i].seat].gold += winners[i].gold;
        }
        var types = [];
        var cards = [];
        var states = [];
        for(var i = 0; i < this.seats.length; i++){
            this.seats[i].cardsList = seats[i].cardsList;
            types.push(this.getSeatCardType(i));
            cards.push(seats[i].cardsList);
            states.push(!this.seats[i].isout && this.seats[i].action!=enm.FOLD);
        }
        winners.sort(function(a, b){
            if(a.bet > b.bet){
                return 1;
            }
            else if(a.bet < b.bet){
                return -1;
            }
            return 0;
        });
        var result = [];
        var cur = [winners[0]];
        for(var i = 1; i < winners.length; i++){
            if(winners[i].bet == cur[0].bet){
                cur.push(winners[i]);
            }
            else{
                result.push(cur);
                cur = [winners[i]];
            }
        }
        result.push(cur);
        cc.warn(this.seats, winners, result, this.getPileList());
        this.event.fire('onRoundResult', result, states, cards, types, this.getPileList(), this.pubCards);
    },
    
    SeatOfflineRep:function(obj){
        var seat = obj.seat;
    },

    SeatOnlineRep:function(obj){
        var seat = obj.seat;
    },

    SitDownSeatInfoRep:function(obj){
        var seatIndex = obj.seatindex;
        var seatInfo = obj.seatinfo;
        this.seats[seatIndex] = seatInfo;
        this.event.fire('onSitDownSeatInfo', seatIndex, seatInfo);
    },
    
    LeaveTableRep:function(obj){
        var seat = obj.seat;
        var winners = [];
        for(var i = 0; i < this.seats.length; i++){
            if(this.seats[i].gold > 0){
                winners.push(this.seats[i]);
            }
        }
        if(winners.length == 1){
            this.event.fire('onLeaveTable', seat, this.ownerIndex, winners[0]);
        }
        else{
            this.event.fire('onLeaveTable', seat, this.ownerIndex);
        }
    },

    BattleChatRep:function(from, channel, msg){
        if(!this.seats){
            return;
        }
        for(var i = 0; i < this.seats.length; i++){
            var seat = this.seats[i];
            if(seat.username == from){
                this.event.fire('onBattleChat', i, seat.nickname, msg);
                return;
            }
        }
    },
    
    /////////////////////////////////////////////////////////////////////////////////////
    // Client->Server
    /////////////////////////////////////////////////////////////////////////////////////
    ActionReq:function(action, bet){
        this.clientCall('ActionReq', action, bet);
    },

    LeaveTableReq:function(){
        this.clientCall('LeaveTableReq');
    },

    BattleChatReq:function(msg){
        var targets = new Array();
        for(var i = 0; i < this.seats.length; i++){
            var seat = this.seats[i];
            if(seat.username != ""){
                targets.push(seat.username);
            }
        }
        this.ChatReq(targets, GameData.ChatChannelTypeEnm.PERSONAL_CHANNEL, msg);
    },
    
    /////////////////////////////////////////////////////////////////////////////////////
    // 辅助方法
    /////////////////////////////////////////////////////////////////////////////////////
    // 计算牌型
    getSeatCardType:function(index){
        var card_list = this.seats[index].cardsList.concat(this.pubCards);
        if(card_list.length < 5){
            return [];
        }
        else{
            return Result.pick(card_list);
        }
    },

    // 得到钱堆
    getPileList:function(){
        var enm = TexasGameData.ActionEnm;
        var gold_list = [];
        var bet_list = [];
        for(var i = 0; i < this.seats.length; i++){
            if(this.seats[i].action == enm.ALLIN){
                bet_list.push(this.seats[i].bet);
            }
            gold_list.push(this.seats[i].bet);
        }
        bet_list.sort(function(a, b){
            return a-b;
        });
        var current = this.currentBet;
        var temp_list = [current];
        for(var i = bet_list.length-1; i >= 0; i--){
            if(bet_list[i] != current){
                current = bet_list[i];
                temp_list.push(current);
            }
        }
        var result = [];
        temp_list.sort(function(a, b){
            return a-b;
        });
        for(var i = temp_list.length-2; i >= 0; i--){
            temp_list[i+1] -= temp_list[i];
        }
        for(var i = 0; i < temp_list.length; i++){
            var sum = 0;
            for(var j = 0; j < gold_list.length; j++){
                if(gold_list[j] >= temp_list[i]){
                    gold_list[j] -= temp_list[i];
                    sum += temp_list[i];
                }
                else{
                    sum += gold_list[j];
                    gold_list[j] = 0;
                }
            }
            result.push(sum);
        }
        return result;
    },

    /////////////////////////////////////////////////////////////////////////////////////
    // 判断是否能操作
    canAction:function(){
        if(this.ownerIndex == null || 
            this.seats == null || 
            this.seats[this.ownerIndex].gold <= 0 || 
            this.seats[this.ownerIndex].action == 5 || 
            this.seats[this.ownerIndex].action == 6){
            return false;
        }
        else{
            return true;
        }
    },

    // 是否可以看牌
    canCheck:function(){
        if(!this.canAction()){
            return false;
        }
        if(this.seats[this.ownerIndex].bet == this.currentBet){
            return true;
        }
        else{
            return false;
        }
    },

    // 是否可以弃牌
    canFold:function(){
        return this.canAction();
    },

    // 是否可以跟注
    canCall:function(){
        if(!this.canAction()){
            return false;
        }
        if(this.seats[this.ownerIndex].bet < this.currentBet && this.seats[this.ownerIndex].gold+this.seats[this.ownerIndex].bet > this.currentBet){
            return true;
        }
        else{
            return false;
        }
    },

    // 是否可以加注
    canRaise:function(){
        if(!this.canAction()){
            return false;
        }
        if(this.seats[this.ownerIndex].gold+this.seats[this.ownerIndex].bet > this.currentBet){
            return true;
        }
        else{
            return false;
        }
    },

    // 是否可以不高于当前赌注全投
    canAllin1:function(){
        if(!this.canAction()){
            return false;
        }
        if(this.seats[this.ownerIndex].bet < this.currentBet && this.seats[this.ownerIndex].gold+this.seats[this.ownerIndex].bet <= this.currentBet){
            return true;
        }
        else{
            return false;
        }
    },

    // 是否可以高于当前赌注全投
    canAllin2:function(){
        return this.canRaise();
    },
});

module.exports = CBattle;