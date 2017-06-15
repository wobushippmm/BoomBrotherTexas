var Protocol = require('protocol_pb');
var GameData = require('GameData_pb');
var TexasGameData = require('TexasGameData_pb');
var SOSConfig = require('SOSConfig');
var SOSConst = require('SOSConst');
var SOSDataPackage = require('SOSDataPackage');
var SOSStreamReader = require('SOSStreamReader');
var SOSStreamWriter = require('SOSStreamWriter');

var SOSServer = {};
SOSServer.app = null;

/*-------------------------------------------------------------------------------------------
SOSServer app
-------------------------------------------------------------------------------------------*/
SOSServer.SOSServerApp = function(ip, port, client){
    // Login Server的地址
    this.loginIp = ip;
    this.loginPort = port;

    // 客户端主类
    this.client = client;

    this.reset = function(){
        // Gateway Server的地址
        this.gatewayIp = "";
        this.gatewayPort = 0;

        // 当前服务器
        this.currentServer = "";

        // 网络相关
        this.socket = null;
        this.dataPackage = new SOSDataPackage();
        this.streamReader = new SOSStreamReader(SOSConfig.LENGTH_OF_NET_PACKAGE_HEADER, SOSConfig.MAX_SIZE_OF_SINGLE_PACKAGE);
        this.streamWriter = new SOSStreamWriter(SOSConfig.LENGTH_OF_NET_PACKAGE_HEADER, SOSConfig.MAX_SIZE_OF_SINGLE_PACKAGE);

        // 发送心跳时间
        var dateObject = new Date();
        this.lastTickTime = dateObject.getTime();
    };

    this.connect = function(addr){
        if(SOSServer.app.socket){
            return;
        }
        try{
            SOSServer.app.socket = new WebSocket(addr);
        }
        catch(e){
            console.error('WebSocket init error!');
            return;
        }
        if(SOSServer.app.currentServer == "LoginServer"){
            SOSServer.app.socket.binaryType = "arraybuffer";
            SOSServer.app.socket.onopen = SOSServer.app.onLoginServerOpen;
            SOSServer.app.socket.onerror = SOSServer.app.onLoginServerError;
            SOSServer.app.socket.onmessage = SOSServer.app.onLoginServerMessage;
            SOSServer.app.socket.onclose = SOSServer.app.onLoginServerClose;
        }
        else if(SOSServer.app.currentServer == "GatewayServer"){
            SOSServer.app.socket.binaryType = "arraybuffer";
            SOSServer.app.socket.onopen = SOSServer.app.onGatewayServerOpen;
            SOSServer.app.socket.onerror = SOSServer.app.onGatewayServerError;
            SOSServer.app.socket.onmessage = SOSServer.app.onGatewayServerMessage;
            SOSServer.app.socket.onclose = SOSServer.app.onGatewayServerClose;
        }
    };

    this.disconnect = function(){
        if(!SOSServer.app.socket){
            return;
        }
        try{
            SOSServer.app.socket.onclose = undefined;
            SOSServer.app.socket.close();
        }
        catch(e){
            console.error('WebSocket clear error!');
            return;
        }
        SOSServer.app.socket = null;
        SOSServer.app.currentServer = "";
    };

    this.onLoginServerOpen = function(){
        SOSServer.app.currentServer = "LoginServer";
        SOSServer.app.client.onLoginServerOpen();
    };

    this.onLoginServerError = function(err){
        console.error("onLoginServerError", err);
        SOSServer.app.client.onLoginServerError(err);
    };

    this.onLoginServerMessage = function(msg){
        SOSServer.app.receiveData(msg.data);
    };

    this.onLoginServerClose = function(){
        SOSServer.app.socket = null;
    };

    this.onGatewayServerOpen = function(){
        SOSServer.app.currentServer = "GatewayServer";
        SOSServer.app.client.onGatewayServerOpen();
    };

    this.onGatewayServerError = function(err){
        console.error("onGatewayServerError", err);
        SOSServer.app.client.onGatewayServerError(err);
    };

    this.onGatewayServerMessage = function(msg){
        SOSServer.app.receiveData(msg.data);
    };

    this.onGatewayServerClose = function(){
        console.error("onGatewayServerClose");
        SOSServer.app.currentServer = "";
        SOSServer.app.socket = null;
        SOSServer.app.client.onGatewayServerClose();
    };

    this.update = function(){
        if(SOSServer.app.socket == null || SOSServer.app.socket.readyState != WebSocket.OPEN){
            if(SOSServer.app.streamReader.buffers.length > 0){
                var buf = SOSServer.app.streamReader.buffers.shift();
                var rpc_info = SOSServer.app.dataPackage.decode(buf);
                SOSServer.fireFunction(rpc_info[0], rpc_info[1]);
            }
        }
        else{
            var dateObject = new Date();
            if(dateObject.getTime() - SOSServer.app.lastTickTime >= SOSConfig.DELAY_OF_HEART){
                SOSServer.callFunction('HeartReq');
                SOSServer.app.lastTickTime = dateObject.getTime();
            }
            if(SOSServer.app.streamWriter.streams.length > 0){
                var data = SOSServer.app.streamWriter.streams.shift();
                SOSServer.app.socket.send(data);
            }
            if(SOSServer.app.streamReader.buffers.length > 0){
                var buf = SOSServer.app.streamReader.buffers.shift();
                var rpc_info = SOSServer.app.dataPackage.decode(buf);
                SOSServer.fireFunction(rpc_info[0], rpc_info[1]);
            }
        }
    };

    this.connectLoginServer = function(){
        SOSServer.app.currentServer = "LoginServer";
        SOSServer.app.connect("ws://" + SOSServer.app.loginIp + ":" + SOSServer.app.loginPort);
    };

    this.connectGatwayServer = function(){
        SOSServer.app.currentServer = "GatewayServer";
        SOSServer.app.connect("ws://" + SOSServer.app.gatewayIp + ":" + SOSServer.app.gatewayPort);
    };

    this.receiveData = function(data){
        SOSServer.app.streamReader.read(data);
    };

    this.sendData = function(rpc){
        SOSServer.app.streamWriter.write(rpc);
    };
};

/*-------------------------------------------------------------------------------------------
public function
-------------------------------------------------------------------------------------------*/
SOSServer.create = function(ip, port, client){
    if(SOSServer.app != undefined){
        return;
    }
    SOSServer.app = new SOSServer.SOSServerApp(ip, port, client);
    SOSServer.app.reset();
    SOSServer.idInterval = setInterval(SOSServer.app.update, SOSConfig.UPDATE_RATE);
};

SOSServer.destroy = function(){
    if(SOSServer.app == undefined){
        return;
    }
    clearInterval(SOSServer.idInterval);
    SOSServer.idInterval = undefined;
    SOSServer.app.reset();
    SOSServer.app = undefined;
};

SOSServer.callFunction = function(func_name){
    if(SOSServer.app == undefined){
        return;
    }
    var rpc_class = Protocol[func_name] || GameData[func_name] || TexasGameData[func_name];
    if(rpc_class == undefined){
        return;
    }
    var arg_list = [];
    for(var i = 1; i < arguments.length; i++){
        arg_list.push(arguments[i]);
    }
    var args = new rpc_class(arg_list);
    var rpc = SOSServer.app.dataPackage.encode(func_name, args);
    SOSServer.app.sendData(rpc);
};

SOSServer.fireFunction = function(func_name, args){
    if(SOSServer.app == undefined){
        return;
    }
    var rpc_class = Protocol[func_name] || GameData[func_name] || TexasGameData[func_name];
    if(rpc_class == undefined){
        return;
    }
    cc.log(func_name, args.toObject());
    if(SOSServer.app.client[func_name]){
        SOSServer.app.client[func_name](args.toObject());
    }
    else{
        console.error('client no function name: ' + func_name);
    }
};

module.exports = SOSServer;