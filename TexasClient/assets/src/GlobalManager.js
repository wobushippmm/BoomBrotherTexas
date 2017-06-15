var SceneManager = require('SceneManager');

cc.Class({
    extends: SceneManager,

    properties: {
        disconnect_ui:cc.Prefab,
        gold_ui:cc.Prefab,
        lock_ui:cc.Prefab,
        message_ui: cc.Prefab,
    },

    onLoad: function () {
        this._super();
    }
});
