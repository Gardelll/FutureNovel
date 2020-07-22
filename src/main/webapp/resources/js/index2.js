function yes_no(contant,name,id){
    $('body').append('<div class="yes_no" style=" width: 100%; height: 100%; position: absolute; top: 0; z-index:10; background-color:#000; opacity:0.3;"></div><div class="yes_no" style="z-index: 11; left: 50%; top: 50%; transform: translate(-50%,-50%); background-color: white; position: absolute; width: 260px; height: 150px;"><p style="height: 40px; background-color: #F8F8F8; line-height: 40px; font-size: 16px;"><span style="margin-left: 10px;">信息</span> <span style="right: 10px; position: absolute; cursor: pointer;"><a style="color: #000; text-decoration: none;" onclick="$(\'.yes_no\').remove()">X</a></span></p><p style="height: 110px;"> <span style="font-size: 16px; top: 15px; left: 15px; position: relative;">'+contant+'</span><span style="bottom: 10px; right: 15px; position: absolute;"><button type="button" class="btn btn-info btn-sm" style="margin-right: 10px;" onclick="'+name+'();$(\'.yes_no\').remove()">确认</button><button type="button" class="btn btn-sm" onclick="$(\'.yes_no\').remove();">取消</button></span></p></div>');
};
function del_num(){

}
function popup_over(icon,color,contant){
    $('body').append('<div class="popup_over" style="text-align: center; line-height: 80px; left: 50%; top: 50%; transform: translate(-50%,-50%); background-color: white; position: absolute; width: 180px; height: 80px; border: 1px solid #D5D6D5; z-index: 1000;"><i class="iconfont '+icon+'" style="font-size: 30px;color: '+color+';"></i><span style="font-size: 16px; position: relative; bottom: 5px;"> '+contant+'</span></div>');
    setTimeout(function(){
        $('.popup_over').remove();
        // clear('.popup_over');
    },1500)
};
function logout_user(){
    $.ajax({
        url:'http://localhost:8080/future-novel/api/logout',
        type:'get',
        datatype:"json",
        data:JSON.stringify({
            
        }),
        contentType:'application/json; charset=utf-8',
        success: function(data){
           alert('已退出')
        }
    })
}