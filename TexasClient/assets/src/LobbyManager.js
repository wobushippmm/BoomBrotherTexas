var SceneManager = require('SceneManager');

cc.Class({
    extends: SceneManager,

    properties: {
        daily_award_ui:cc.Prefab,
        rank_ui:cc.Prefab,
        email_ui:cc.Prefab,
        friend_ui:cc.Prefab,
        shop_ui:cc.Prefab,
        name_ui:cc.Prefab,
        portrait_ui:cc.Prefab
    },

    onLoad: function () {
        this._super();
    }
});

