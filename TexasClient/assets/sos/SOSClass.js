var SOSClass = function(){};
SOSClass.prototype.ctor = function(){};
SOSClass.extend = function(prop){
    var _super = this.prototype;
    var initializing = true;
    var prototype = Object.create(_super);
    initializing = false;
    var fnText = /xyz/.test(function(){xyz;})?/\b_super\b/:/.*/;
    for(var name in prop){
        prototype[name] = typeof prop[name] == "function" &&
            typeof _super[name] == "function" && fnText.test(prop[name])?
            (function(name, fn){
                return function(){
                    var tmp = this._super;
                    this._super = _super[name];
                    var ret = fn.apply(this, arguments);
                    this._super = tmp;
                    return ret;
                };
            })(name, prop[name]):
            prop[name];
    }
    function Class(){
        if(!initializing){
            if(!this.ctor){
                if(this.__nativeObj){
                    console.info('no ctor');
                }
            }
            else{
                this.ctor.apply(this, arguments);
            }
        }
    }
    Class.prototype = prototype;
    Class.prototype.constructor = Class;
    Class.extend = this.extend;
    return Class;
}

module.exports = SOSClass;