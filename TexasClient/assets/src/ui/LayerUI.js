var g_const = require('g_const');
var BaseUI = require('BaseUI');

cc.Class({
    extends: BaseUI,

    properties: {
        z_index:g_const.LAYER_ZINDEX,
    },

    onLoad: function () {
        this._super();
        this.easyHide = false;
        this.node.zIndex = this.z_index;
        this.node.on("onShow", this.onShow, this);
        this.node.on("onHide", this.onHide, this);
        this.node.on('touchend', this.clickAndHide, this);
    },

    clickAndHide:function(){
        if(this.easyHide){
            this.hideSelf();
        }
    },

    onShow:function(){

    },

    onHide:function(){

    },

    hideSelf:function(){
        cc.find('Canvas').getComponent('SceneManager').hideUIByName(this.name);
        cc.find('SceneGlobal').getComponent('SceneManager').hideUIByName(this.name);
    }
});
