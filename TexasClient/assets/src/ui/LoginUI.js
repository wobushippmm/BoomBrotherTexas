var SceneUI = require('SceneUI');
var Player = require('Player');
var GameData = require('GameData_pb');

cc.Class({
    extends: SceneUI,

    properties: {
        name_input:cc.EditBox,
        password_input:cc.EditBox,
        login_btn:cc.Button,
        player_prefab:cc.Prefab,
    },

    onLoad: function () {
        this._super();
        var p = cc.find('Player');
        if(!p){
            p = cc.instantiate(this.player_prefab);
            p.parent = cc.director.getScene();
        }
        Player.register('loginFail', this, 'loginFail');
        Player.register('loginSuccess', this, 'loginSuccess');
        cc.director.preloadScene('lobby');
    },

    onDestroy:function(){
        Player.deregister('loginFail', this, 'loginFail');
        Player.deregister('loginSuccess', this, 'loginSuccess');
    },

    loginFail:function(result){
        var enm = GameData.LoginResultEnm;
        switch(result){
            case enm.PASSWORD_ERROR:
                this.scene_global.hideUI('lock_ui');
                this.scene_global.showUI('message_ui', '密码错误');
                break;
            case enm.USERNAME_ERROR:
                this.scene_global.hideUI('lock_ui');
                this.scene_global.showUI('message_ui', '用户名错误');
                break;
            case enm.ACCOUNT_FREEZE:
                this.scene_global.hideUI('lock_ui');
                this.scene_global.showUI('message_ui', '账号冻结');
                break;
        }
    },

    loginSuccess:function(){
        cc.director.loadScene('lobby');
    },

    onLoginCallback:function(){
        var name_length = this.name_input.string.length;
        var password_length = this.password_input.string.length;
        if(name_length <= 0 || name_length >= 12){
            this.scene_global.showUI('message_ui', '名字长度为1~11个字符');
            return;
        }
        if(password_length <= 0 || password_length >= 12){
            this.scene_global.showUI('message_ui', '密码长度为1~11个字符');
            return;
        }
        Player.client.login(this.name_input.string, this.password_input.string);
        this.scene_global.showUI('lock_ui')
    }
});
