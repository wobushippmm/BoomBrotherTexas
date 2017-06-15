if(!Uint8Array.slice){
    Uint8Array.prototype.slice = function(begin, length){
        if(length == null){
            length = this.byteLength-begin;
        }
        var temp = new Uint8Array(length);
        for(var i = 0; i < length; i++){
            temp[i] = this[begin+i];
        }
        return temp;
    }
}