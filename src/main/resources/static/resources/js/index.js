var Zoom=document.getElementById('pngfix'),
    Catalog=document.getElementById('catalog'),
    Admin=document.getElementById('admin'),
    Dropdown=document.getElementById('dropdown'),
    Contant=document.getElementById('contant');
    Catalog.style.display='block';
    //侧边栏消失浮现
Zoom.onclick=function(){
    let x=getStyle(Catalog,'width');
    if(Catalog.style.display=='block'){
    Catalog.style.display='none';
    Zoom.parentNode.style.left='0';
    Zoom.children[0].children[0].className='iconfont icon-cebianlandanchu';
    Contant.style.left='0'
    }else if(Catalog.style.display=='none'){
        Catalog.style.display='block';
        Zoom.parentNode.style.left=x;
        Zoom.children[0].children[0].className='iconfont icon-cebianlanshousuo';
        Contant.style.left=x;
    }else{
        
    }
}
function getStyle(element, attr) {
    if(element.currentStyle) {
            return element.currentStyle[attr];
    } else {
            return getComputedStyle(element, false)[attr];
    }
}

Admin.onmouseover=function(){
    Dropdown.style.display='block';
}
Admin.onmouseout=function(){
    Dropdown.style.display='none';
}