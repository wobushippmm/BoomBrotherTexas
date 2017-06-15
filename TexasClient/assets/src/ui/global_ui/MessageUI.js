var LayerUI = require('LayerUI');

cc.Class({
    extends: LayerUI,

    properties: {
        message_label:cc.Label,
        black_bg:cc.Node,
    },

    onLoad: function () {
        this._super();
        this.easyHide = true;
    },

    onDestory:function(){

    },

    start:function(){
        this.scheduleOnce(function(){
            if(cc.isValid(this.node)){
                this.hideSelf();
            }
        }.bind(this), 5);
    },

    onShow:function(event){
        this.message_label.string = event.detail;
        var width = this.message_label.node.width;
        var height = this.message_label.node.height;
        this.black_bg.width = width+20;
        this.black_bg.height = height+20;
    },

    onHide:function(){
        
    },
});
