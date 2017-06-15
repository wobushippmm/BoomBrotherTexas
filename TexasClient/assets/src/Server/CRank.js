var CLogin = require('CLogin');
var GameData = require('GameData_pb');

var CRank = CLogin.extend({
    ctor:function(){
        this._super();
        this.rankInfo = {};
    },

    GetRankListReq:function(type, startIndex){
        if(this.rankInfo[type] && this.rankInfo[type][startIndex]){
            this.event.fire('setRankList', type, startIndex, this.rankInfo[type][startIndex]);
            return;
        }
        this.clientCall('GetRankListReq', type, startIndex);
    },

    GetRankListRep:function(obj){
        var type = obj.type;
        var startIndex = obj.startindex;
        var rankItems = obj.rankitemsList;
        if(this.rankInfo[type] == null){
            this.rankInfo[type] = {};
        }
        if(this.rankInfo[type][startIndex] == null){
            this.rankInfo[type][startIndex] = rankItems;
        }
        this.event.fire('setRankList', type, startIndex, rankItems);
    }
});

module.exports = CRank;