var LayerUI = require('LayerUI');

cc.Class({
    extends: LayerUI,

    properties: {
        
    },

    onLoad: function () {
        this._super();
        this.easyHide = true;
    },

    onDestory:function(){
        
    },

    start:function(){
        
    },

    onShow:function(event){
        
    },

    onHide:function(){
        
    },

    clickAndHide:function(){
        this._super();
        cc.director.loadScene('login');
    },

});