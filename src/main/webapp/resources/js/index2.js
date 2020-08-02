//判断
function yes_no(contant,name,id){
    $('body').append('<div class="yes_no" style=" width: 100%; height: 100%; position: absolute; top: 0; z-index:10; background-color:#000; opacity:0.3;"></div><div class="yes_no" style="z-index: 11; left: 50%; top: 50%; transform: translate(-50%,-50%); background-color: white; position: absolute; width: 260px; height: 150px;"><p style="height: 40px; background-color: #F8F8F8; line-height: 40px; font-size: 16px;"><span style="margin-left: 10px;">信息</span> <span style="right: 10px; position: absolute; cursor: pointer;"><a style="color: #000; text-decoration: none;" onclick="$(\'.yes_no\').remove()">X</a></span></p><p style="height: 110px;"> <span style="font-size: 16px; top: 15px; left: 15px; position: relative;">'+contant+'</span><span style="bottom: 10px; right: 15px; position: absolute;"><button type="button" class="btn btn-info btn-sm" style="margin-right: 10px;" onclick="'+name+'(\''+id+'\');$(\'.yes_no\').remove()">确认</button><button type="button" class="btn btn-sm" onclick="$(\'.yes_no\').remove();">取消</button></span></p></div>');
};
function del_num(){ 

}
//操作完成后
function popup_over(icon,color,contant){
    $('body').append('<div class="popup_over" style="text-align: center; line-height: 80px; left: 50%; top: 50%; transform: translate(-50%,-50%); background-color: white; position: absolute; width: 180px; height: 80px; border: 1px solid #D5D6D5; z-index: 1000;"><i class="iconfont '+icon+'" style="font-size: 30px;color: '+color+';"></i><span style="font-size: 16px; position: relative; bottom: 5px;"> '+contant+'</span></div>');
    setTimeout(function(){
        $('.popup_over').remove();
        // clear('.popup_over');
    },1500)
};
//添加
function popup_mod(id){
    $('body').append('<div class="popup_mod" style=" width: 100%; height: 100%; position: absolute; top: 0; z-index:10; background-color:#000; opacity:0.3;"></div><div class="popup_mod" style="z-index: 11; left: 50%; top: 50%; transform: translate(-50%,-50%); background-color: white; position: absolute; width: 600px; height: 400px;"><p style="height: 40px; background-color: #F8F8F8; line-height: 40px; font-size: 16px;"><span style="margin-left: 10px;">菜单编辑</span> <span style="right: 10px; position: absolute; cursor: pointer;"><a style="color: #000; text-decoration: none;" onclick="$(\'.popup_mod\').remove()">X</a></span></p><div class="row"><form class="form-horizontal"> <div class="form-group"><label class="col-md-2 control-label">名字</label><div class="col-md-6"><input type="text" class="form-control" id="food-name2" placeholder="菜品名称."></div></div><div class="form-group"><label class="col-md-2 control-label">价格</label><div class="col-md-6"><input type="text" class="form-control" id="food-price2" placeholder="菜品价格."></div></div><div class="form-group"><label class="col-md-2 control-label">描述</label><div class="col-md-6"><input type="text" class="form-control" id="food-desc" placeholder="菜品描述."></div></div><div class="form-group"><label class="col-md-2 control-label">类型</label><div class="col-md-6"><input type="text" class="form-control" id="food-type2" placeholder="菜品类型(蔬菜；水果；热菜；凉菜；西餐)."></div></div></form></div><span style="bottom: 10px; right: 15px; position: absolute;"><button type="button" class="btn btn-info btn-sm" id="submit-menu" style="margin-right: 10px;" onclick="paging_mod(this,\''+id+'\');$(\'.popup_mod\').remove()">提交</button><button type="button" class="btn btn-sm" onClick="$(\'.popup_mod\').remove();">取消</button></span></div>');
}
//修改用户信息弹窗
function popup_user(id){
    $('body').append(`<div class="popup_mod" style=" width: 100%; height: 100%; position: absolute; top: 0; z-index:10; background-color:#000; opacity:0.3;"></div>
    <div class="popup_mod" style="z-index: 11; left: 50%; top: 50%; transform: translate(-50%,-50%); background-color: white; position: absolute; width: 500px; height: 450px;">
        <p style="height: 40px; background-color: #F8F8F8; line-height: 40px; font-size: 16px; margin-bottom: 20px;">
            <span style="margin-left: 10px;">编辑用户</span>
            <span style="right: 10px; position: absolute; cursor: pointer;">
                <a style="color: #000; text-decoration: none;" onclick="$(\'.popup_mod\').remove()">X</a></span></p>
                <div class="">
                    
                        <div class="item">
                            <label class="">用户名</label>
                            <span class="">
                                <input type="text" class="" id="popup_user" name="user" placeholder="输入用户名"></span></div>
                                <div class="item">
                                    <label class="">账号状态</label>
                                    <span class="">
                                    <select name="status" class="item" id="popup_status" οnchange="checkinfo_location();" >
                                    <option value="FINE" >正常</option>
                                    <option value="UNVERIFIED" >未验证</option>
                                    <option value="BANED" >封禁</option>
                                    </select>
                                    </div>
                                        <div class="item">
                                            <label class="">手机号</label>
                                            <span class=""><input type="text" id="popup_phone" name="user" placeholder="请输入手机号"></span></div>
                                            <div class="item">
                                                <label class="">积分</label>
                                                <span class="">
                                                    <input type="text" id="popup_experience" name="user" placeholder="请输入">
                                                    </span>
                                                    <span class="">
                                                        <button onclick="user_exp('${id}')">修改积分</button>
                                                        </span>
                                                </div>
                                                <span style="bottom: 10px; right: 15px; position: absolute;">
                                                    <button type="button" class="btn" id="submit-user" style="margin-right: 10px;" onclick="user_mod('${id}');">提交</button>
                                                    <button type="button" class="btn" onClick="$(\'.popup_mod\').remove();">取消</button></span></div>`)
}
//修改小说  信息弹窗
function popup_book(id){
    $('body').append(`<div class="popup_mod" style=" width: 100%; height: 100%; position: absolute; top: 0; z-index:10; background-color:#000; opacity:0.3;"></div>
    <div class="popup_mod" style="z-index: 11; left: 50%; top: 50%; transform: translate(-50%,-50%); background-color: white; position: absolute; width: 500px; height: 450px;">
        <p style="height: 40px; background-color: #F8F8F8; line-height: 40px; font-size: 16px; margin-bottom: 20px;">
            <span style="margin-left: 10px;">编辑用户</span>
            <span style="right: 10px; position: absolute; cursor: pointer;">
                <a style="color: #000; text-decoration: none;" onclick="$(\'.popup_mod\').remove()">X</a></span></p>
                <div class="">
                    
                        <div class="item">
                            <label class="">小说名</label>
                            <span class="">
                                <input type="text" class="" id="popup_title" name="user" placeholder="请输入"></span></div>
                                <div class="item">
                                    <label class="">版权</label>
                                    <span class="">
                                    <select name="status" class="item" id="popup_copyright" οnchange="checkinfo_location();" >
                                    <option value="FINE" >正常</option>
                                    <option value="UNVERIFIED" >未验证</option>
                                    <option value="BANED" >封禁</option>
                                    </select>
                                    </div>
                                        <div class="item">
                                            <label class="">出版社</label>
                                            <span class=""><input type="text" id="popup_publisher" name="user" placeholder="请输入"></span></div>
                                            <div class="item">
                                                </div>
                                                <span style="bottom: 10px; right: 15px; position: absolute;">
                                                    <button type="button" class="btn" id="submit-user" style="margin-right: 10px;" onclick="book_mod('${id}');">提交</button>
                                                    <button type="button" class="btn" onClick="$(\'.popup_mod\').remove();">取消</button></span></div>`)
}
//批量添加用户弹窗
function popup_user_add(){
        $('body').append(`<div class="popup_mod" style=" width: 100%; height: 100%; position: absolute; top: 0; z-index:10; background-color:#000; opacity:0.3;"></div>
        <div class="popup_mod" style="z-index: 11; left: 50%; top: 50%; transform: translate(-50%,-50%); background-color: white; position: absolute; width: 500px; height: 450px;">
            <p style="height: 40px; background-color: #F8F8F8; line-height: 40px; font-size: 16px; margin-bottom: 20px;">
                <span style="margin-left: 10px;">添加用户</span>
                <span style="right: 10px; position: absolute; cursor: pointer;">
                    <a style="color: #000; text-decoration: none;" onclick="$(\'.popup_mod\').remove()">X</a></span></p>
                    <div class="">
                            <div class="item">
                                <label class="">用户名</label>
                                <span class="">
                                    <input type="text" class="" id="popup_user_add" name="user" placeholder="输入用户名"></span></div>
                                    <div class="item">
                                        <label>邮箱</label>
                                        <span class="">
                                            <input id="popup_email_add" name="email" placeholder="输入邮箱"></span></div>
                                            <div class="item">
                                                <label class="">密码</label>
                                                <span class=""><input type="text" class="" id="popup_pas_add" name="pas" placeholder="输入用户名"></span></div>
                                                <div class="item">
                                                    <label class="">手机号</label>
                                                    <span class="">
                                                        <input type="text" class="" id="popup_phone_add" name="phone" placeholder="请输入">
                                                        </span>
                                                        
                                                    </div></div>
                                                    <span style="bottom: 10px; right: 15px; position: absolute;">
                                                        <button style="margin-right: 10px;"onclick="user_add();">提交</button>
                                                        <button type="button" class="btn" onClick="$(\'.popup_mod\').remove();">取消</button></span></div>`)
}
//重新获取用户数据
function refresh(){
    $.ajax({
        url:/*[[${#request.getContextPath()} + '/api/admin/accounts/pages']]*/ "http://localhost:8080/future-novel/api/admin/accounts/pages" ,
        type:'GET',
        data:{
            per_page: Number(onepage)
        },

        success: function(data){
            const allPage=data.pages;
            console.log(data)
            $.ajax({
                url:/*[[${#request.getContextPath()} + '/api/admin/accounts/get']]*/ "http://localhost:8080/future-novel/api/admin/accounts/get" ,
                type:'GET',
                data:{
                    per_page: Number(onepage),
                    page: Number(curPage)
                },
                success: function(data){
                    $('#a').empty();
                    $.each(data,function(i){
                                $('#a').append(`<tr>
                                <th style="width: 4%;"><input type="checkbox" class="check" name="check"  value="${data[i].uid}" style="zoom: 1.5;"></th>
                                <th style="width: 13%;">${data[i].userName}</th>
                                <th style="width: 16%;">${data[i].phone}</th>
                                <th style="width: 4%;">${data[i].experience}</th>
                                <th style="width: 13%;">${data[i].permission}</th>
                                <th style="width: 15%;">${data[i].registerDate}</th>
                                <th style="width: 13%;">${data[i].status}</th>
                                <th style="width: 24%;"><span class="modify-user" onclick="popup_user('${data[i].uid}')"><i class="iconfont icon-xiugai"> 编辑</i></span></th>
                            </tr>`)
                            })
                    $('#control_page').empty()
                    $('#control_page').prepend('第'+curPage+'页/共'+allPage+'页')
                    console.log(data)
                },
                error: function() {
                    //const data = JSON.parse(jqXHR.responseText);
                    popup_over('icon-sad','#d81e06',"请求失败");
                }
            })
        },
        error: function(jqXHR) {
            let data = JSON.parse(jqXHR.responseText);
            popup_over('icon-sad','#d81e06',data.errorMessage);
            console.log(data.errorMessage);
        }
    })

}
//重新获取小说数据
function refresh_book(){
    $.ajax({
        url:/*[[${#request.getContextPath()} + '/api/admin/novel/all']]*/ "http://localhost:8080/future-novel/api/admin/novel/all" ,
        type:'GET',
        data:{
            per_page: Number(onepage),
            page: Number(curPage)
        },
        success: function(data){
            $('#a').empty();
            $.each(data,function(i){
                        $('#a').append(`<tr>
                        <th style="width: 13%;">${data[i].title}</th>
                        <th style="width: 16%;">${data[i].authors}</th>
                        <th style="width: 4%;">${data[i].hot}</th>
                        <th style="width: 13%;">${data[i].series}</th>
                        <th style="width: 15%;">${data[i].pubdate}</th>
                        <th style="width: 13%;">${data[i].copyright}</th>
                        <th style="width: 24%;"><span class="modify-user" onclick="popup_book('${data[i].uniqueId}')"><i class="iconfont icon-xiugai"> 编辑</i></span> <span class="modify-user" style="background-color: #FF5722;" onclick="yes_no('确认删除吗？','book_del','${data[i].uniqueId}')"><i class="iconfont icon-shanchu"> 删除</i></span></th>
                    </tr>`)
                    })

            $('#control_page').prepend('第'+curPage+'页/共'+allPage+'页')
            console.log(data)
        },
        error: function() {
            //const data = JSON.parse(jqXHR.responseText);
            popup_over('icon-sad','#d81e06',"请求失败");
        }
    })
}
//批量添加用户ajax
function user_add(){
    let Arr=[],
        userName=$('#popup_user_add').val(),
        userEmail=$('input[name="email"]').val(),
        password=$('input[name="pas"]').val(),
        phone=$('input[name="phone"]').val();
        let data1={"userName": userName,"password": password,"email": userEmail,"phone": phone
        };
        Arr.push(data1);
        if(/^1[3,5,7,8]\d{9}$/.test(phone) && /^([\.a-zA-Z0-9_-])+@([a-zA-Z0-9_-])+(\.[a-zA-Z0-9_-])+/.test(userEmail)){
            $('.popup_mod').remove();
            $.ajax({
                url:/*[[${#request.getContextPath()} + '/api/admin/account/add']]*/'http://localhost:8080/future-novel/api/admin/account/add',
                type: 'post',
                datatype:'json',
                data: JSON.stringify(Arr),
                contentType: 'application/json; charset=utf-8',
                success: function(data){
                    popup_over('icon-happy-l','#1afa29','添加成功');
                    console.log(data);
                    refresh()
                },
                error: function(jqXHR){
                    popup_over('icon-sad','#d81e06','添加失败');
                    console.log(jqXHR.responseJSON.errorMessage);
                }
            })
        }else if(userName=='' || password==''){
            popup_over('icon-sad','#d81e06','关键项不能为空');
            if(userName==''){
                $('#popup_user_add').css('border','1px solid #FF5722');
            $('#popup_user_add').blur(function(){
                $('#popup_user_add').css('border','1px solid #BBBBBB');
            })
            $('#popup_user_add').focus(function(){
                $('#popup_user_add').css('border','1px solid #FF5722');
            })
            }else{
                $('input[name="pas"]').css('border','1px solid #FF5722');
            $('input[name="pas"]').blur(function(){
                $('input[name="pas"]').css('border','1px solid #BBBBBB');
            })
            $('input[name="pas"]').focus(function(){
                $('input[name="pas"]').css('border','1px solid #FF5722');
            })
            }
        }else{
            if(/^1[3,5,7,8]\d{9}$/.test(phone)){
                popup_over('icon-sad','#d81e06','请输入正确格式');
                $('input[name="email"]').css('border','1px solid #FF5722');
            $('input[name="email"]').blur(function(){
                $('input[name="email"]').css('border','1px solid #BBBBBB');
            })
            $('input[name="email"]').focus(function(){
                $('input[name="email"]').css('border','1px solid #FF5722');
            })
            }else{
                popup_over('icon-sad','#d81e06','请输入正确格式');
                $('input[name="phone"]').css('border','1px solid #FF5722');
            $('input[name="phone"]').blur(function(){
                $('input[name="phone"]').css('border','1px solid #BBBBBB');
            })
            $('input[name="phone"]').focus(function(){
                $('input[name="phone"]').css('border','1px solid #FF5722');
            })
            }
        }
}
//批量删除用户ajax
function user_del(){
        let Arr_del=[];
        $("input[name='check']:checked").each(function(){
            Arr_del.push($(this).val())
        });
        $.ajax({
            url:/*[[${#request.getContextPath()} + '/api/admin/account/delete']]*/'http://localhost:8080/future-novel/api/admin/account/delete',
            type: 'DELETE',
            datatype:'json',
            data: JSON.stringify(Arr_del),
            contentType: 'application/json; charset=utf-8',
            success: function(data){
                popup_over('icon-happy-l','#1afa29','删除成功');
                refresh()
            },
            error: function(jqXHR){
                
                popup_over('icon-sad','#d81e06','删除失败');
            }
        })
}
//删除书籍
function book_del(id){
    $.ajax({
        url:'http://localhost:8080/future-novel/api/novel/'+id+'',
        type: 'DELETE',
        contentType: 'application/json; charset=utf-8',
        success: function(data){    
            popup_over('icon-happy-l','#1afa29','删除成功');
            refresh_book();
        },
        error: function(jqXHR){
            
            popup_over('icon-sad','#d81e06','删除失败');
        }
    })
}
//修改积分ajax
function user_exp(id){
    let exp=$('#popup_experience').val();
    $.ajax({
        url:/*[[${#request.getContextPath()} + '/api/admin/account/experience/edit']]*/'http://localhost:8080/future-novel/api/admin/account/experience/edit',
        type: 'POST',
        datatype: 'json',
        contentType: 'application/json; charset=utf-8',
        data: JSON.stringify({
            accountId: id,
            experience: Number(exp),
        }),
        success: function(data){
            popup_over('icon-happy-l','#1afa29','修改成功');
            console.log('修改积分成功')
        },
        error: function(jqXHR){
            
            popup_over('icon-sad','#d81e06','修改失败');
        }
    })
}
//修改用户接口函数
function user_mod(id){
    let userName=$('#popup_user').val(),
        userStatus=$('#popup_status').find('option:selected').val(),
        //head=$('popup_head').val(),
        phone=$('#popup_phone').val();
        if(/^1[3,5,7,8]\d{9}$/.test(phone)){
            $('.popup_mod').remove();
            $.ajax({
                url:/*[[${#request.getContextPath()} + '/api/account/edit']]*/'http://localhost:8080/future-novel/api/account/edit',
                type: 'POST',
                datatype: 'json',
                contentType: 'application/json; charset=utf-8',
                data: JSON.stringify({
                    uuid: id,
                    userName: userName,
                    status: userStatus,
                    phone: phone
                }),
                success: function(data){
                    popup_over('icon-happy-l','#1afa29','修改成功');
                    refresh()
                },
                error: function(jqXHR){
                    
                    popup_over('icon-sad','#d81e06','修改失败');
            
                }
            })
        }else if(userName.length<4){
            popup_over('icon-sad','#d81e06','用户名太短');
            $('#popup_user').css('border','1px solid #FF5722');
            $('#popup_user').blur(function(){
                $('#popup_user').css('border','1px solid #BBBBBB');
            })
            $('#popup_user').focus(function(){
                $('#popup_user').css('border','1px solid #FF5722');
            })
        }else{
            popup_over('icon-sad','#d81e06','请输入正确的电话');
            $('#popup_phone').css('border','1px solid #FF5722');
            $('#popup_phone').blur(function(){
                $('#popup_phone').css('border','1px solid #BBBBBB');
            })
            $('#popup_phone').focus(function(){
                $('#popup_phone').css('border','1px solid #FF5722');
            })
        }
}

//修改小说接口函数
function book_mod(id){
    let title=$('#popup_title').val(),
    copyright=$('#popup_copyright').find('option:selected').val(),
    publisher=$('#popup_publisher').val();
            $('.popup_mod').remove();
            $.ajax({
                url:/*[[${#request.getContextPath()} + '/api/account/edit']]*/'http://localhost:8080/future-novel/api/novel/'+id+'/edit',
                type: 'POST',
                datatype: 'json',
                contentType: 'application/json; charset=utf-8',
                data: JSON.stringify({
                    title: title,
                    copyright: copyright,
                    publisher: publisher
                }),
                success: function(data){
                    popup_over('icon-happy-l','#1afa29','修改成功');
                    refresh()
                },
                error: function(jqXHR){
                    
                    popup_over('icon-sad','#d81e06','修改失败');
            
                }
            })
}
//激活账号窗口
function popup_activate(name){
    $('body').append('<div class="yes_no" style=" width: 100%; height: 100%; position: absolute; top: 0; z-index:10; background-color:#000; opacity:0.3;"></div><div class="yes_no" style="z-index: 11; left: 50%; top: 50%; transform: translate(-50%,-50%); background-color: white; position: absolute; width: 260px; height: 170px;"><p style="height: 40px; margin: 0; background-color: #F8F8F8; line-height: 40px; font-size: 16px;"><span style="margin-left: 10px;">激活账号</span> <span style="right: 10px; position: absolute; cursor: pointer;"><a style="color: #000; text-decoration: none;" onclick="$(\'.yes_no\').remove()">X</a></span></p><p style="height: 110px; margin: 0;"> <span style="font-size: 16px; top: 15px; left: 15px; position: relative;"><input type="text" name="activate" placeholder="验证码已发送，请输入" id="activate" style="width: 150px; height: 35px; font-size:16px; border: 1px solid grey;"></span><span style="bottom: 10px; right: 15px; position: absolute;"><button type="button" class="btn btn-info btn-sm" style="margin-right: 10px;" onclick="'+name+'();$(\'.yes_no\').remove()">确认</button><button type="button" class="btn btn-sm" onclick="$(\'.yes_no\').remove();">取消</button></span></p></div>');
}

function activate_yes(){
    let us=$('#us').val(),
        ps=$('#ps').val(),
        activate=$('#activate').val();
    $.ajax({
        url:/*[[${#request.getContextPath()} + '/api/login']]*/ "http://localhost:8080/future-novel/api/login" ,
        type:'post',
        datatype:"json",
        data:JSON.stringify({
            userName: us,
            password: ps,
            activateCode: activate
        }),
        contentType:'application/json; charset=utf-8',
        success: function(data){
            $(location).attr('href',data.redirectTo);
            console.log('登录成功');
        },
        error: function(jqXHR) {
            let mes = JSON.parse(jqXHR.responseText);
            popup_over('icon-sad','#d81e06',"验证失败");
            $("#login").attr('disabled', true).html("30秒后可重试");
            const interval = setInterval(function() {
                const htmlobj = $("#login");
                let remaintime = parseInt(htmlobj.html().substr(0, 2));
                remaintime--;
                if (remaintime < 10) remaintime = "0" + remaintime;
                htmlobj.html(remaintime + "秒后可重试");
            }, 1000);
            setTimeout(function() {
                $("#login").attr('disabled', false).html('登录');
                clearInterval(interval);
            }, 30000);
        }
    })
}
//删除checkbox管理
$('#all-check').on('click',function(){
if($('#all-check').prop("checked")){
    $('.check').prop('checked',true)
}else{
    $('.check').prop('checked',false)
}
})
$('.check').on('click',function(){
    var allCheck=$('.check').length;
    
    if($('.check').checked){
        $('#all-check').prop('checked',true)
    }else{
        $('#all-check').prop('checked',false)
    }
})
// function logout_user(){
//     $.ajax({
//         url:'http://localhost:8080/future-novel/api/logout',
//         type:'get',
//         datatype:"json",
//         data:JSON.stringify({
            
//         }),
//         contentType:'application/json; charset=utf-8',
//         success: function(data){
//            alert('已退出')
//         }
//     })
// }