var LayerUI = require('LayerUI');

cc.Class({
    extends: LayerUI,

    properties: {
        gold_label:cc.Label,
    },

    onLoad: function () {
        this._super();
        this.easyHide = true;
    },

    onShow:function(event){
        var gold = event.detail;
        this.gold_label.string = gold;
    }
});
