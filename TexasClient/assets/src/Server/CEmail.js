var CChat = require('CChat');
var GameData = require('GameData_pb');

var CEmail = CChat.extend({
    ctor:function(){
        this._super();
        this.emailList = null;
    },

    // 推送
    EmailListRep:function(obj){
        var emailList = obj.emaillistList;
        this.emailList = emailList;
        for(var i = 0; i < emailList.length; i++){
            emailList[i].index = i;
        }
        this.event.fire('onEmailList', emailList);
        this.event.fire('checkRedPoint');
    },

    SetEmailReadRep:function(obj){
        var index = obj.index;
        var gold = obj.gold;
        this.emailList[index].read = true;
        this.event.fire('checkRedPoint');
    },

    // 请求
    SendEmailReq:function(from, target, gold, msg, read){
        var email = new Array();
        email[0] = from;
        email[1] = target;
        email[2] = gold;
        email[3] = msg;
        email[4] = read;
        this.clientCall('SendEmailReq', email);
    },

    GetEmailListReq:function(){
        this.clientCall('GetEmailListReq');
    },

    SetEmailReadReq:function(index){
        cc.error('set email read req', index);
        this.clientCall('SetEmailReadReq', index);
    },

    hasNotReadEmail:function(email_index){
        if(this.emailList == null){
            return false;
        }
        if(email_index == null){
            for(var i = 0; i < this.emailList.length; i++){
                var email = this.emailList[i];
                if(!email.read){
                    return true;
                }
            }
            return false;
        }
        else{
            if(this.emailList[email_index] && !this.emailList[email_index].read){
                return true;
            }
            else{
                return false;
            }
        }
    },
});

module.exports = CEmail;