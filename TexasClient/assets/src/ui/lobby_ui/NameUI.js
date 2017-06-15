var LayerUI = require('LayerUI');
var Player = require('Player');

cc.Class({
    extends: LayerUI,

    properties: {
        name_input:cc.EditBox,
    },

    onLoad: function () {
        this._super();
        this.easyHide = true;
    },

    onShow:function(event){
        
    },

    onOKBtnCallback:function(){
        var name_length = this.name_input.string.length;
        if(name_length <= 0 || name_length >= 12){
            cc.find('Canvas').getComponent('SceneUI').scene_global.showUI('message_ui', '名字长度为1~11个字符');
            return;
        }
        Player.client.ChangeNicknameReq(this.name_input.string);
        this.hideSelf();
    },
});
