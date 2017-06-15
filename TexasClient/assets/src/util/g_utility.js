var g_const = require('g_const');
var HeadTemplate = require('HeadTemplate');

var g_utility = {};

g_utility.setSpriteFrame = function(path, sp){
    cc.loader.loadRes(path, cc.SpriteFrame, function(err, spriteFrame){
        if(err){
            cc.error('no sprite frame: '+path);
        }
        else{
            sp.spriteFrame = spriteFrame;
        }
    });
};

g_utility.getHeadIndexByName = function(name){
    for(var i in HeadTemplate){
        var info = HeadTemplate[i];
        if(info.name == name){
            return i;
        }
    }
    return null;
};

g_utility.addRedPoint = function(parent, red_point, x, y){
    var red_sprite = parent.getChildByName('red_point_');
    if(red_sprite){
        return;
    }
    else{
        red_sprite = new cc.Node();
        red_sprite.name = 'red_point_';
        red_sprite.parent = parent;
        red_sprite.addComponent('cc.Sprite');
        var sprite = red_sprite.getComponent('cc.Sprite');
        sprite.spriteFrame = red_point;
        sprite.sizeMode = cc.Sprite.SizeMode.RAW;
        red_sprite.x = x;
        red_sprite.y = y;
    }
};

g_utility.removeRedPoint = function(parent){
    var red_sprite = parent.getChildByName('red_point_');
    if(red_sprite){
        red_sprite.destroy();
    }
};

g_utility.convertMSToTime = function(ms){
    var hour = parseInt(ms/g_const.HOUR);
    var minute = parseInt(ms/g_const.MINUTE);
    var second = parseInt(ms.g_const.SECOND);
    second -= minute*60;
    minute -= hour*60;
    hour = '0'+hour;
    minute = '0'+minute;
    second = '0'+second;
    hour = hour.slice(hour.length-2, hour.length);
    minute = minute.slice(minute.length-2, minute.length);
    second = second.slice(second.length-2, second.length);
    return hour+':'+minute+':'+second;
};

g_utility.convertNumberToString = function(num){
    var unit_str = ['', '万', '亿', '兆', '京'];
    var d = 1;
    for(var i = 0; i < unit_str.length; i++){
        var temp = num/d;
        if(temp < 10000){
            return (''+temp).slice(0, 4)+unit_str[i];
        }
        d *= 10000;
    }
};

module.exports = g_utility;