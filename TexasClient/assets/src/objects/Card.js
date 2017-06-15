var Result = require('PickResult');

cc.Class({
    extends: cc.Component,

    properties: {
        bg0_img:cc.Node,
        bg1_img:cc.Node,
        type_big_img:cc.Sprite,
        type_small_img:cc.Sprite,
        point_label:cc.Label,
        type_big_frames:[cc.SpriteFrame],
        type_small_frames:[cc.SpriteFrame],
        type_role_frames:[cc.SpriteFrame],
        type_colors:[cc.Color],
        select_img:cc.Node,
    },

    onLoad: function () {

    },

    setBgType:function(type){
        switch(type){
            case 0:
                this.bg0_img.active = true;
                this.bg1_img.active = false;
                break;
            case 1:
                this.bg0_img.active = false;
                this.bg1_img.active = true;
                break;
        }
    },

    setSuitAndPoint:function(suit, point){
        var plist = ['A','2','3','4','5','6','7','8','9','10','J','Q','K','A'];
        this.point_label.string = plist[point];
        this.point_label.node.color = this.type_colors[suit];
        this.type_small_img.spriteFrame = this.type_small_frames[suit];
        if(point >= 10 && point <= 12){
            this.type_big_img.spriteFrame = this.type_role_frames[point-10];
        }
        else{
            this.type_big_img.spriteFrame = this.type_big_frames[suit];
        }
    },

    setSuitAndPointByNum:function(num){
        var v = Result.getSuitAndPoint(num);
        this.setSuitAndPoint(v.suit, v.point);
    },

    setSelectImgActive:function(active){
        this.select_img.active = active;
    },

    setCardLight:function(isLight){
        this.bg1_img.color = isLight?cc.color(255, 255, 255):cc.color(180, 180, 180);
    },
});
