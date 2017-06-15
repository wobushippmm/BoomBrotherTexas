var GlobalNode = require('GlobalNode');

cc.Class({
    extends: cc.Component,

    properties: {
        gold_prefab:cc.Prefab,
        num_label:cc.Label,
    },

    onLoad: function () {
        this.num = 0;
        this.num_label.enabled = false;
        this.gold_node_list = [];
    },

    unuse:function(){
        if(this.gold_node_list){
            for(var i = 0; i < this.gold_node_list.length; i++){
                GlobalNode.gold_pool.put(this.gold_node_list[i]);
            }
            this.gold_node_list = [];
        }
    },

    reuse:function(){
        this.node.position = cc.p(0, 0);
        this.num = 0;
        this.num_label.enabled = false;
    },

    _getGoldList:function(num){
        // num += 9;
        // num = parseInt(num/10)*10;
        // var str = ""+num;
        // var len = str.length;
        // var t_n = parseInt(str.slice(0, 2));
        // var d1 = parseInt(t_n/10);
        // var d2 = t_n % 10;
        // var gold_list = [];
        // for(var i = 0; i < d1; i++){
        //     gold_list.push(len-1);
        // }
        // for(var i = 0; i < d2; i++){
        //     gold_list.push(len-2);
        // }
        num += 9;
        num = parseInt(num/10)*10;
        var str = ""+num;
        var len = str.length;
        var d = parseInt(str.slice(0, 1));
        d = parseInt((d+1)/2);
        var gold_list = [];
        for(var i = 0; i < d; i++){
            gold_list.push(len-1);
        }
        return gold_list;
    },

    _addGoldNode:function(gold_list){
        for(var i = 0; i < gold_list.length; i++){
            var gold_node;
            if(GlobalNode.gold_pool.size() > 0){
                gold_node = GlobalNode.gold_pool.get();
            }
            else{
                gold_node = cc.instantiate(this.gold_prefab);
            }
            gold_node.parent = this.node;
            gold_node.y = 5*this.gold_node_list.length;
            gold_node.getComponent('Gold').setGoldType(gold_list[i]);
            this.gold_node_list.push(gold_node);
        }
    },

    showGold:function(num){
        num = parseInt(num);
        if(num <= 0){
            return;
        }
        this.num = num;
        var gold_list = this._getGoldList(num);
        this._addGoldNode(gold_list);
        this.num_label.enabled = true;
        this.num_label.string = this.num;
    },
    
    addGold:function(num){
        num = parseInt(num);
        if(num <= 0){
            return;
        }
        this.num += num;
        var gold_list = this._getGoldList(num);
        this._addGoldNode(gold_list);
        this.num_label.string = this.num;
    },

    _addGoldNodeAnimation:function(gold_list, from){
        for(var i = 0; i < gold_list.length; i++){
            var gold_node;
            if(GlobalNode.gold_pool.size() > 0){
                gold_node = GlobalNode.gold_pool.get();
            }
            else{
                gold_node = cc.instantiate(this.gold_prefab);
            }
            gold_node.parent = this.node;
            gold_node.getComponent('Gold').addGoldAnimation(from, cc.p(0, 5*this.gold_node_list.length), i*0.1, 0.5);
            gold_node.getComponent('Gold').setGoldType(gold_list[i]);
            this.gold_node_list.push(gold_node);
        }
    },

    showGoldAnimation:function(num, from){
        num = parseInt(num);
        if(num <= 0){
            return;
        }
        this.num = num;
        var gold_list = this._getGoldList(num);
        this._addGoldNodeAnimation(gold_list, from);
        this.num_label.node.active = true;
        this.num_label.node.runAction(cc.sequence(cc.delayTime(0.6), cc.callFunc(function(target){
            target.getComponent('cc.Label').enabled = true;
        })));
        this.num_label.string = this.num;
    },

    addGoldAnimation:function(num, from){
        num = parseInt(num);
        if(num <= 0){
            return;
        }
        this.num += num;
        var gold_list = this._getGoldList(num);
        this._addGoldNodeAnimation(gold_list, from);
        this.num_label.string = this.num;
    },

    getGoldList:function(panel){
        for(var i = 0; i < this.gold_node_list.length; i++){
            var gold = this.gold_node_list[i];
            var p = panel.convertToNodeSpaceAR(gold.convertToWorldSpaceAR(cc.p(0, 0)));
            gold.parent = panel;
            gold.position = p;
        }
        var result = this.gold_node_list;
        this.gold_node_list = [];
        return result;
    }
});
