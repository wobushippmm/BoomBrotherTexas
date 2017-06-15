cc.Class({
    extends: cc.Component,

    properties: {
        
    },

    onLoad: function () {
        this.eventDict = {};
        this.initEvent();
    },

    initEvent:function(){

    },

    registerEvent:function(name, except_list){
        var info = {};
        this.eventDict[name] = info;
        info.enable = true;
        info.exceptList = except_list?except_list:[];
    },

    lockEvent:function(name){
        if(this.eventDict[name]){
            for(var n in this.eventDict){
                if(this.eventDict[name].exceptList.indexOf(n) == -1){
                    this.eventDict[n].enable = false;
                }
            }
        }
        else{
            for(var n in this.eventDict){
                this.eventDict[n].enable = false;
            }
        }
    },

    unlockEvent:function(){
        for(var name in this.eventDict){
            this.eventDict[name].enable = true;
        }
    },

    isEventEnable:function(name, default_value = true){
        if(this.eventDict[name]){
            return this.eventDict[name].enable;
        }
        else{
            return default_value;
        }
    },
});
