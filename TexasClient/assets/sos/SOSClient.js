var SOSClass = require('SOSClass');
var SOSServer = require('SOSServer');
var SOSEvent = require('SOSEvent');

var SOSClient = SOSClass.extend({
    event : new SOSEvent(),

    ctor:function(){
        this._super();
        this.username = null;
        this.password = null;
    },

    clientCall:function(){
        SOSServer.callFunction.apply(SOSServer, arguments);
    },

    login:function(username, password){
        this.username = username;
        this.password = password;
        SOSServer.app.connectLoginServer();
    },

    connectGatewayServer:function(gatewayHost, gatewayPort){
        SOSServer.app.gatewayIp = gatewayHost;
        SOSServer.app.gatewayPort = gatewayPort;
        SOSServer.app.connectGatwayServer();
    },

    logout:function(){
        this.username = "";
        this.password = "";
        SOSServer.app.disconnect();
    },
});

module.exports = SOSClient;