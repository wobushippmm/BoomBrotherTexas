var CBattle = require('CBattle');
var TexasGameData = require('TexasGameData_pb');

var CChannel = CBattle.extend({
    ctor:function(){
        this._super();
        this.tableList = null;
        this.isMatching = false;
    },

    GetTableListReq:function(num){
        this.clientCall('GetTableListReq', num);
    },

    GetTableListRep:function(obj){
        var tableList = obj.tablelistList;
        this.tableList = tableList;
        this.event.fire('setTableList', tableList);
    },

    MatchTableReq:function(type){
        this.clientCall('MatchTableReq', type);
    },

    MatchTableRep:function(obj){
        var result = obj.result;
        var enm = TexasGameData.MatchTableResultEnm;
        if(result == enm.OK_MATCHTABLERESULT){
            this.isMatching = true;
            this.event.fire('matchTable');
        }
        else{
            this.event.fire('matchTableError', result);
        }
    },

    CancelMatchTableReq:function(){
        this.clientCall('CancelMatchTableReq');
    },

    CancelMatchTableRep:function(){
        this.isMatching = false;
        this.event.fire('cancelMatchTable');
    },

    JoinTableReq:function(id){
        cc.error('join table req', id);
        this.clientCall('JoinTableReq', id);
    },

    JoinTableRep:function(obj){
        cc.error('join table rep', obj);
        var result = obj.result;
        this.event.fire('joinTableResult', result);
    },

    ExitBattleReq:function(){
        this.clientCall('ExitBattleReq');
    },

    ExitBattleRep:function(){
        this.event.fire('onExitBattle');
    }
});

module.exports = CChannel;