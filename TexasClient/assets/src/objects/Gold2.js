cc.Class({
    extends: cc.Component,

    properties: {
        chips:[cc.SpriteFrame],
    },

    onLoad: function () {
        this.node.x = cc.randomMinus1To1()*this.node.parent.width/2;
        this.y = cc.random0To1()*100+this.node.parent.height/2;
        this.node.y = this.y;
        this.speed = 0;
        this.acceleration = -200 - 100*cc.random0To1();
        var r = parseInt(cc.random0To1()*this.chips.length);
        if(r == this.chips.length){
            r = this.chips.length-1;
        }
        this.node.getComponent('cc.Sprite').spriteFrame = this.chips[r];
    },

    onDestroy:function(){

    },

    unuse:function(){
        
    },

    reuse:function(){
        this.y = cc.random0To1()*100+this.node.parent.height/2;
        this.speed = 0;
    },

    update:function(dt){
        this.speed += this.acceleration*dt;
        this.y += this.speed*dt;
        this.node.y = this.y;
    }
});
