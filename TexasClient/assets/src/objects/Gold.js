var GlobalNode = require('GlobalNode');

cc.Class({
    extends: cc.Component,

    properties: {
        chips:[cc.SpriteFrame],
    },

    onLoad: function () {

    },

    onDestroy:function(){

    },

    unuse:function(){

    },

    reuse:function(){
        this.node.x = 0;
        this.node.y = 0;
    },

    setGoldType:function(type){
        if(this.chips[type]){
            this.node.getComponent('cc.Sprite').spriteFrame = this.chips[type];
        }
    },

    addGoldAnimation:function(from, to, t1, t2){
        this.node.position = from;
        this.node.getComponent('cc.Sprite').enabled = false;
        this.node.active = true;
        this.node.runAction(
        cc.sequence(
            cc.delayTime(t1),
            cc.callFunc(function(target){
                target.getComponent('cc.Sprite').enabled = true;
            }),
            cc.moveTo(t2, to)
            )
        );
    },

    useGoldAnimation:function(to, t1, t2){
        this.node.active = true;
        this.node.runAction(
        cc.sequence(
            cc.delayTime(t1),
            cc.moveTo(t2, to),
            cc.callFunc(function(target){
                GlobalNode.gold_pool.put(target);
            })
            )
        );
    },
});
