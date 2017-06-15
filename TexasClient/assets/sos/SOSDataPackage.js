var Protocol = require('protocol_pb')
var GameData = require('GameData_pb');
var TexasGameData = require('TexasGameData_pb');

var SOSDataPackage = function(){
    this.encode = function(func_name, args){
        var rpc = new Protocol.RpcPo();
        rpc.setRpc(func_name);
        if(args){
            rpc.setAnypo(args.serializeBinary());
        }
        return rpc.serializeBinary();
    };
    this.decode = function(data){
        var rpc = Protocol.RpcPo.deserializeBinary(data);
        var func_name = rpc.getRpc();
        var args = rpc.getAnypo();
        var rpc_class = Protocol[func_name] || GameData[func_name] || TexasGameData[func_name];
        if(args || args==""){
            args = rpc_class.deserializeBinary(args);
        }
        return [func_name, args];
    };
};

module.exports = SOSDataPackage;