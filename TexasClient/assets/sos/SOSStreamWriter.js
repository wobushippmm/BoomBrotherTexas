var SOSStreamWriter = function(head_size, max_size){
    this.head_size = head_size;
    this.max_size = max_size;
    this.streams = [];
    this.curSteram = null;
    this.write = function(data){
        var size = data.byteLength + this.head_size;
        if(size <= max_size){
            this.curSteram = new ArrayBuffer(size);
        }
        else{
            this.curSteram = new ArrayBuffer(this.max_size);
        }
        this.writeHead(size);
        this.writeBody(data);
    };
    this.writeHead = function(size){
        var buf = new Uint8Array(this.curSteram, 0, this.head_size);
        for(var i = 0; i < this.head_size; i++){
            buf[this.head_size-1-i] = (size >> (i * 8)) & 0xff;
        }
    };
    this.writeBody = function(data){
        var s = data.byteLength;
        var p = this.head_size;
        var t = 0;
        do{
            var n = this.max_size - p;
            if(n >= s-t){
                var buf = new Uint8Array(this.curSteram, p, s-t);
                for(var i = 0; i < s-t; i++){
                    buf[i] = data[t+i];
                }
                t = s;
                this.streams.push(this.curSteram);
                this.curSteram = null;
            }
            else{
                var buf = new Uint8Array(this.curSteram, p, this.max_size - p);
                for(var i = 0; i < this.max_size-p; i++){
                    buf[i] = data[t+i];
                }
                t += this.max_size-p;
                this.streams.push(this.curSteram);
                if(s-t <= this.max_size){
                    this.curSteram = new ArrayBuffer(s-t);
                }
                else{
                    this.curSteram = new ArrayBuffer(this.max_size);
                }
                p = 0;
            }
        }while(t < s)
    };
};

module.exports = SOSStreamWriter;