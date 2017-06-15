var Server = require('SOSServer');
var Client = require('CRank')

var Player = cc.Class({
    extends: cc.Component,

    properties: {
        ip:"",
        port:0,  
    },

    statics:{
        client:new Client(),
        register:function(){
            this.client.event.register.apply(this.client.event, arguments);
        },
        deregister:function(){
            this.client.event.deregister.apply(this.client.event, arguments);
        }
    },

    onLoad: function () {
        this.username = null;
        this.id = null;
    },

    onDestroy:function(){
        
    },

    start:function(){
        Server.create(this.ip, this.port, Player.client);
    }
});
