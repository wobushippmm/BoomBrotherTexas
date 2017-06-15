var LayerUI = require('LayerUI');

cc.Class({
    extends: LayerUI,

    properties: {
        lock_label:cc.Label,
        lock_img:cc.Node,
    },

    onLoad: function () {
        this._super();
    },

    onDestroy:function(){
        
    },

    onShow:function(){
        // this.lock_label.string = "等待";
        // this.node.runAction(cc.sequence(cc.callFunc(this.lockLabelCallFunc, this), cc.delayTime(0.5)).repeatForever());
        this.lock_img.active = true;
        this.lock_img.runAction(cc.rotateBy(1, 90).repeatForever());
    },

    lockLabelCallFunc:function(){
        var l = this.lock_label.string.length;
        if(l >= "等待。。。".length){
            this.lock_label.string = "等待";
        }
        else{
            this.lock_label.string += "。";
        }
    },

    onHide:function(){
        // this.node.stopAllActions();
        this.lock_img.stopAllActions();
    },
});
