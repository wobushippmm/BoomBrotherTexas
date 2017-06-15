var CFriend = require('CFriend');
var GameData = require('GameData_pb');

var CLobby = CFriend.extend({
    ctor:function(){
        this._super();
    },

    JoinBattleReq:function(){
        this.clientCall('JoinBattleReq');
    },

    JoinBattleRep:function(obj){
        var result = obj.result;
        var battleName = obj.battlename;
        var enm = GameData.JoinBattleResultEnm;
        if(result == enm.OK_JOINBATTLERESULT){
            this.battleName = battleName;
            this.event.fire('joinSuccess');
        }
        else{
            this.event.fire('joinFail');
        }
    },

    DailyLoginAwardRep:function(obj){
        var gold = obj.gold;
        this.event.fire('dailyLoginAward', gold);
    }
});

module.exports = CLobby;