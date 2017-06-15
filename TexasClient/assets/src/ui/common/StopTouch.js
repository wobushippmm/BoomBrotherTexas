cc.Class({
    extends: cc.Component,

    properties: {
        
    },

    onLoad: function () {
        this.node.on('mousedown', this.stopTouch);
        this.node.on('mouseenter', this.stopTouch);
        this.node.on('mousemove', this.stopTouch);
        this.node.on('mouseleave', this.stopTouch);
        this.node.on('mouseup', this.stopTouch);
        this.node.on('mousewheel', this.stopTouch);
        this.node.on('touchstart', this.stopTouch);
        this.node.on('touchmove', this.stopTouch);
        this.node.on('touchend', this.stopTouch);
        this.node.on('touchcancel', this.stopTouch);
    },

    stopTouch:function(event){
        event.stopPropagation();
    }
});
