var g_const = require('g_const');

var g_global = {};

g_global.lastChatTime = 0;
g_global.lastEmailTime = 0;
g_global.canSendChat = function(){
    var now = new Date();
    if(now - g_global.lastChatTime >= g_const.CHAT_INTERVAL){
        g_global.lastChatTime = now;
        return true;
    }
    else{
        return false;
    }
};
g_global.canSendEmail = function(){
    var now = new Date();
    if(now - g_global.lastEmailTime >= g_const.EMAIL_INTERVAL){
        g_global.lastEmailTime = now;
        return true;
    }
    else{
        return false;
    }
};

module.exports = g_global;