var SOSEvent = function(){
    this._events = {};
    this.register = function(evtName, classinst, strCallback){
        var callbackfn = eval("classinst." + strCallback);
        if(callbackfn == undefined){
            console.error('no ' + 'classinst.' + strCallback);
            return;
        }
        var evtlst = this._events[evtName];
        if(evtlst == undefined){
            evtlst = [];
            this._events[evtName] = evtlst;
        }
        var info = {'classinst':classinst, 'callbackfn':callbackfn};
        evtlst.push(info);
    };
    this.deregister = function(evtName, classinst, strCallback){
        var callbackfn = eval("classinst." + strCallback);
        if(callbackfn == undefined){
            console.error('no ' + 'classinst.' + strCallback);
            return;
        }
        var evtlst = this._events[evtName];
        if(evtlst == undefined){
            return;
        }
        for(var i = evtlst.length-1; i >= 0; i--){
            var info = evtlst[i];
            if(info.classinst == classinst && info.callbackfn == callbackfn){
                evtlst.splice(i, 1);
                return;
            }
        }
    };
    this.fire = function(){
        if(arguments.length < 1){
            console.error('not found eventName');
            return;
        }
        var evtName = arguments[0];
        var evtlst = this._events[evtName];
        if(evtlst == undefined){
            return;
        }
        var args = [];
        for(var i = 1; i < arguments.length; i++){
            args.push(arguments[i]);
        }
        for(var i = 0; i < evtlst.length; i++){
            var info = evtlst[i];
            info.callbackfn.apply(info.classinst, args);
        }
    }
};

module.exports = SOSEvent;