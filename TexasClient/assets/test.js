var Card = require('Card');

cc.Class({
    extends: cc.Component,

    properties: {
        card:Card,
        node1:cc.Node,
        node2:cc.Node,
    },

    onLoad: function () {
        // var req = new XMLHttpRequest();
        // req.open('GET', 'http://121.42.161.244/poker/img/female_01.png', true);
        // req.onreadystatechange = function(e){
        //     if(this.readyState == 4 && this.status == 200){
        //         var response = this.response;
        //         cc.error(response);
        //     }
        // }
        // req.send();
    },

    start:function(){
        cc.error('start');
        this.test_func();
        this.schedule(this.test_func, 5, 3);
    },

    test_func:function(){
        cc.error('test func');
    },

    onBtnCallback1:function(){
        // this.card.setCardLight(true);
        // this.card.setBgType(0);
        // // this.card.node.runAction(cc.sequence(cc.moveBy(0.5, 100, 100),
        // //     cc.callFunc(function(target){
        // //         target.getComponent('Card').setBgType(1);
        // //         // cc.error(target.opacity);
        // //         // for(var ch in target.children){
        // //         //     cc.error(target.children[ch].opacity);
        // //         // }
        // //     })));
        // this.card.node.runAction(cc.sequence(cc.delayTime(0.5), 
        //     cc.callFunc(function(target){
        //         target.getComponent('Card').setBgType(1);
        //     })))
        this.node1.opacity = 255;
        this.node2.active = false;
        this.node1.runAction(cc.sequence(cc.delayTime(0.5),
            cc.callFunc(function(target){
                target.getChildByName('node2').active = true;
            })))
    },

    onBtnCallback2:function(){
        // // this.card.node.runAction(cc.moveBy(0.5, -100, -100));
        // this.card.setCardLight(false);
        this.node1.opacity = 180;
    },

    onBtnCallback3:function(){
        
    },
});
