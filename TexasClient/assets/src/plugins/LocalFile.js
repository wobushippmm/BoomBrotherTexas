var LocalFile = {};

LocalFile.target = null;

LocalFile.openLocal = function(){
    var fileInput = document.getElementById('fileInput');
    if(fileInput == null){
        fileInput = document.createElement('input');
        fileInput.id = 'fileInput';
        fileInput.type = 'file';
        fileInput.accept = "image/*";
        fileInput.style.height = '0px';
        fileInput.style.display = 'block';
        fileInput.style.overflow = 'hidden';
        document.body.insertBefore(fileInput, document.body.firstChild);
        fileInput.addEventListener('change', LocalFile.selectFile, false);
    }
    setTimeout(function(){
        fileInput.click();
    }, 300);
};

LocalFile.selectFile = function(evt){
    var file = evt.target.files[0];
    if(file == null){
        return;
    }
    var url = LocalFile.createObjectURL(file);
    LocalFile.loadLocal(url);
};

LocalFile.createObjectURL = function(file){
    if(window.URL){
        return window.URL.createObjectURL(file);
    }
    else{
        return window.webkitURL.createObjectURL(file);
    }
};

LocalFile.loadLocal = function(url){
    var myImg = document.getElementById('myImg');
    if(myImg){
        document.body.removeChild(myImg);
        myImg = null;
    }
    myImg = document.createElement('img');
    document.body.insertBefore(myImg, document.body.firstChild);
    myImg.id = 'myImg';
    myImg.src = url;
    myImg.style.position = 'absolute';
    myImg.style.display = 'none';
    myImg.style.visibility = 'hidden';
    myImg.onload = function(){
        if(LocalFile.target){
            var texture = LocalFile.target.spriteFrame.getTexture();
            texture.initWithElement(this);
            texture.handleLoadedTexture();
        }
    };
};

module.exports = LocalFile;