var LayerUI = require('LayerUI');

cc.Class({
    extends: LayerUI,

    properties: {
        
    },

    onLoad: function () {
        this._super();
        this.easyHide = true;
    },

    onShow:function(event){
        
    },

    onGoodsBtnCallback:function(event, data){
        var type = parseInt(data);
        
    }
});
