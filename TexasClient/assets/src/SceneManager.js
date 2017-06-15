cc.Class({
    extends: cc.Component,

    properties: {
        
    },

    onLoad: function () {
        this.canvas = cc.find('Canvas');
        this.ui_dict = {};
        this.name_dict = {};
    },

    showUI:function(ui_name, ui_args, isHide){
        if(this[ui_name]){
            if(this.ui_dict[ui_name]){
                if(isHide){
                    this.hideUI(ui_name);
                }
                else{
                    return;
                }
            }
            var ui = cc.instantiate(this[ui_name]);
            this.canvas.addChild(ui);
            ui.emit('onShow', ui_args);
            this.ui_dict[ui_name] = ui;
            this.name_dict[ui.getComponent('LayerUI').name] = ui_name;
        }
    },

    hideUI:function(ui_name){
        if(this[ui_name]){
            if(this.ui_dict[ui_name]){
                this.ui_dict[ui_name].emit('onHide');
                this.ui_dict[ui_name].destroy();
                delete this.ui_dict[ui_name];
            }
            else{
                return;
            }
        }
    },

    hideUIByName:function(name){
        var ui_name = this.name_dict[name];
        if(ui_name){
            this.hideUI(ui_name);
        }
    }
});
