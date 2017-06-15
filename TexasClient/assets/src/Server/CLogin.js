var CLobby = require('CLobby');
var GameData = require('GameData_pb');
var g_utility = require('g_utility');

var CLogin = CLobby.extend({
    ctor:function(){
        this._super();
        this.cid = null;
        this.level = null;
        this.gold = null;
        this.winCount = null;
        this.gameCount = null;
        this.gatewayHost = null;
        this.gatewayPort = null;
        this.scene = null;
        this.nickname = null;
        this.portrait = null;
    },

    onLoginServerOpen:function(){
        this.clientCall('LoginReq', this.username, this.password);
    },

    onLoginServerError:function(err){
        this.logout();
        this.event.fire('onReconnect');
    },
    
    onLoginServerClose:function(){

    },

    onGatewayServerOpen:function(){
        this.clientCall('ConnectReq', this.username, this.cid);
    },

    onGatewayServerError:function(err){
        this.logout();
        this.event.fire('onReconnect');
    },

    onGatewayServerClose:function(){
        this.logout();
        this.event.fire('onReconnect');
    },

    DisconnectRep:function(obj){
        var cause = obj.cause;
        this.logout();
        this.event.fire('onReconnect', cause);
    },

    LoginRep:function(obj){
        var result = obj.result;
        var gatewayHost = obj.gatewayhost;
        var gatewayPort = obj.gatewayport;
        var cid = obj.cid;
        this.gatewayHost = gatewayHost;
        this.gatewayPort = gatewayPort;
        this.cid = cid;
        var enm = GameData.LoginResultEnm;
        if(result == enm.OK_LOGINRESULT || result == enm.CREATE_ACCOUNT){
            this.connectGatewayServer(gatewayHost, gatewayPort);
        }
        else{
            this.event.fire('loginFail', result);
        }
    },

    EnterSceneRep:function(obj){
        var username = obj.username;
        var scene = obj.scene;
        var userDat = obj.userdat;
        this.username = username;
        this.scene = scene;
        this.username = userDat.username;
        this.cid = userDat.cid;
        this.level = userDat.level;
        this.gold = userDat.gold;
        this.winCount = userDat.wincount;
        this.gameCount = userDat.gamecount;
        this.nickname = userDat.nickname;
        this.portrait = userDat.portrait;
        this.event.fire('loginSuccess');
    },

    SetGoldRep:function(obj){
       var gold = obj.gold;
       var cause = obj.cause;
       var old = this.gold;
       this.gold = gold;
       this.event.fire('onSetGold', obj.gold, cause, gold-old);
    },

    ChangeNicknameReq:function(nickname){
        this.clientCall('ChangeNicknameReq', nickname);
    },

    ChangeNicknameRep:function(obj){
        var result = obj.result;
        var nickname = obj.nickname;
        var enm = GameData.ChangeNicknameResultEnm;
        if(result == enm.OK_CHANGENICKNAMERESULT){
            this.nickname = nickname;
        }
        this.event.fire('onChangeNickname', result, nickname);
    },

    ChangePortraitReq:function(portrait){
        var index = g_utility.getHeadIndexByName(portrait);
        if(index != null){
            this.clientCall('ChangePortraitReq', portrait);
        }
    },

    ChangePortraitRep:function(obj){
        var portrait = obj.portrait;
        var index  = g_utility.getHeadIndexByName(portrait);
        if(index != null){
            this.event.fire('onChangePortrait', portrait);
        }
    },
});

module.exports = CLogin;