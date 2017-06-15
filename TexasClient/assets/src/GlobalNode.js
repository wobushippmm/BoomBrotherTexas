var GlobalNode = cc.Class({
    extends: cc.Component,

    properties: {
        gold_prefab:cc.Prefab,
        gold2_prefab:cc.Prefab,
        pile_prefab:cc.Prefab,
    },

    statics:{
        gold_pool:new cc.NodePool('Gold'),
        gold2_pool:new cc.NodePool('Gold2'),
        pile_pool:new cc.NodePool('Pile'),
    },

    onLoad: function () {
        for(var i = 0; i < 20; i++){
            GlobalNode.gold_pool.put(cc.instantiate(this.gold_prefab));
        }
        for(var i = 0; i < 50; i++){
            GlobalNode.gold2_pool.put(cc.instantiate(this.gold2_prefab));
        }
        for(var i = 0; i < 10; i++){
            GlobalNode.pile_pool.put(cc.instantiate(this.pile_prefab));
        }
    },

    onDestroy:function(){
        GlobalNode.gold_pool.clear();
        GlobalNode.gold2_pool.clear();
        GlobalNode.pile_pool.clear()
    }
});
