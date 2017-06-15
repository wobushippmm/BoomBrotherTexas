var LayerUI = require('LayerUI');
var GlobalNode = require('GlobalNode');

cc.Class({
    extends: LayerUI,

    properties: {
        num_label:cc.Label,
        gold2_prefab:cc.Prefab,
    },

    onLoad: function () {
        this._super();
        this.gold_list = [];
        this.num_label.node.zIndex = 1;
    },

    onDestory:function(){
        
    },

    onShow:function(data){
        var gold = data.detail;
        this.num_label.string = '获得了'+gold+'金币';
        for(var i = 0; i < 50; i++){
            var node;
            if(GlobalNode.gold2_pool.size() > 0){
                node = GlobalNode.gold2_pool.get();
            }
            else{
                node = cc.instantiate(this.gold2_prefab);
            }
            node.parent = this.node;
        }
        this.scheduleOnce(this.hideSelf, 3);
    },

    onHide:function(){
        for(var i = 0; i < this.gold_list.length; i++){
            GlobalNode.gold2_pool.put(this.gold_list[i]);
        }
        this.gold_list = null;
    }
});
