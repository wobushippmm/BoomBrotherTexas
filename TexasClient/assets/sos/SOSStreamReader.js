var SOSStreamReader = function(head_size, max_size){
    this.head_size = head_size;
    this.max_size = max_size;
    this.buffers = [];
    this.step = 0;
    this.curBuffer = new Uint8Array(0);
    this.body_size = 0;
    this.read = function(data){
        var buf = new Uint8Array(data, 0, data.byteLength);
        var tempBuf = new Uint8Array(this.curBuffer.byteLength + data.byteLength);
        for(var i = 0; i < this.curBuffer.byteLength; i++){
            tempBuf[i] = this.curBuffer[i];
        }
        for(var i = 0; i < data.byteLength; i++){
            tempBuf[this.curBuffer.byteLength+i] = buf[i];
        }
        this.curBuffer = tempBuf;
        if(this.step == 0){
            this.readHead();
        }
        else{
            this.readBody();
        }
    };
    this.readHead = function(){
        if(this.curBuffer.byteLength >= this.head_size){
            this.body_size = 0;
            for(var i = 0; i < this.head_size; i++){
                this.body_size += this.curBuffer[this.head_size - i - 1] << (8*i);
            }
            this.curBuffer = this.curBuffer.slice(this.head_size);
            this.body_size -= this.head_size;
            this.step = 1;
            this.readBody();
        }
    };
    this.readBody = function(){
        if(this.curBuffer.byteLength >= this.body_size){
            this.buffers.push(this.curBuffer.slice(0, this.body_size));
            this.curBuffer = this.curBuffer.slice(this.body_size);
            this.step = 0;
            this.readHead();
        }
    };
}

module.exports = SOSStreamReader;