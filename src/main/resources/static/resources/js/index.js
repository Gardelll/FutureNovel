var Zoom=document.getElementById('pngfix'),
    Catalog=document.getElementById('catalog'),
    Admin=document.getElementById('admin'),
    Dropdown=document.getElementById('dropdown'),
    Skin_blue=document.getElementById('skin_blue'),
    Skin_black=document.getElementById('skin_black'),
    Dropdown2=document.getElementById('dropdown2'),
    skin_fl=document.getElementsByClassName('skin')[0],
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
    Contant.style.width='95%'
    }else if(Catalog.style.display=='none'){
        Catalog.style.display='block';
        Zoom.parentNode.style.left=x;
        Zoom.children[0].children[0].className='iconfont icon-cebianlanshousuo';
        Contant.style.left=x;
        Contant.style.width='81%';
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
