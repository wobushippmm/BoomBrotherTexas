var CChannel = require('CChannel');
var GameData = require('GameData_pb');

var CChat = CChannel.extend({
    ctor:function(){
        this._super();
    },

    // 推送
    ChatRep:function(obj){
        var from = obj.from;
        var channel = obj.channel;
        var msg = obj.msg;
        // this.event.fire('onChat', from, channel, msg);
        this.BattleChatRep(from, channel, msg);
        this.FriendChatRep(from, channel, msg);
    },

    // 请求
    ChatReq:function(target, channel, msg){
        this.clientCall('ChatReq', target, channel, msg);
    },
});

module.exports = CChat;