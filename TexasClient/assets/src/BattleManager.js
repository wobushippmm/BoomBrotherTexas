var SceneManager = require('SceneManager');

cc.Class({
    extends: SceneManager,

    properties: {
        result_ui:cc.Prefab,
    },

    onLoad: function () {
        this._super();
    },

});
