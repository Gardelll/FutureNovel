$('#login').on('click',function(){
    var us=$('#us').val(),
        //ul='index.html',
        ps=$('#ps').val();
        if(us=='' || ps==''){
            alert('输入框内不能为空')
        }else if(us.length>=18 ||ps.length>=18){
            alert('输入内容过多')
        }else {
            $.ajax({
                url:'http://localhost:8080/future-novel/api/login',
                type:'post',
                datatype:"json",
                data:JSON.stringify({
                    userName: us,
                    password: ps,
                    //redirectTo: ul
                }),
                contentType:'application/json; charset=utf-8',
                success: function(data){
                    $(location).attr('href','index.html');
                }
            })
        }

});
$('#logon').on('click',function(){
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
})