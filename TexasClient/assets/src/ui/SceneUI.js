var SceneManager = require('SceneManager');
var BaseUI = require('BaseUI');

cc.Class({
    extends: BaseUI,

    properties: {
        scene_manager:SceneManager,
        scene_global:SceneManager,
    },

    onLoad: function () {
        this._super();
    },

});