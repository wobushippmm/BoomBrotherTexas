var SceneUI = require('SceneUI');
var Player = require('Player');
var GameData = require('GameData_pb');

cc.Class({
    extends: SceneUI,

    properties: {
        
    },

    onLoad: function () {
        this._super();
        this.scene_manager = cc.find('Canvas').getComponent('SceneManager');
        this.scene_global = this.node.getComponent('SceneManager');
        Player.register('onReconnect', this, 'onReconnect');
        Player.register('onSetGold', this, 'onSetGold');
    },

    onDestroy:function(){
        Player.deregister('onReconnect', this, 'onReconnect');
        Player.deregister('onSetGold', this, 'onSetGold');
    },

    onReconnect:function(cause){
        this.scene_global.showUI('disconnect_ui');
    },

    onSetGold:function(gold, cause, delta){
        var enm = GameData.SetGoldCauseEnm;
        switch(cause){
            case enm.FROM_EMAIL:
                this.scene_global.showUI('gold_ui', delta);
                break;
            case enm.AFTER_BATTLE:
                break;
            case enm.BY_GM:
                break;
        }
    },
});
