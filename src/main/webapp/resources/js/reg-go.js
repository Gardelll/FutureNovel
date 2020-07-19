$(document).ready(function() {

    $("#get-code").click(function() {
        const email = $("#txtemailnumber").val();
        if (!email.match(/^(\w){3,}(\.\w+)*@(\w){2,}((\.\w+)+)$/)) {
            alert("请填写正确的邮箱地址");
            return;
        }
        $("#get-code").attr('disabled', true).html("30秒后可重试");
        $.ajax({
            url: /*[[${#request.getContextPath()} + '/api/sendCaptcha']]*/ "/",
            type: 'POST',
            dataType: "JSON",
            contentType: "application/json;charset=UTF-8",
            data: JSON.stringify({
                email: email
            }),
            success: function() {
                $("#get-code").attr('disabled', true).html("30秒后可重试");
                const interval = setInterval(function() {
                    const htmlobj = $("#get-code");
                    let remaintime = parseInt(htmlobj.html().substr(0, 2));
                    remaintime--;
                    if (remaintime < 10) remaintime = "0" + remaintime;
                    htmlobj.html(remaintime + "秒后可重试");
                }, 1000);
                setTimeout(function() {
                    $("#get-code").attr('disabled', false).html('重新发送');
                    clearInterval(interval);
                }, 30000);
            },
            error: function(jqXHR, textStatus, errorThrown) {
                const data = JSON.parse(jqXHR.responseText);
                alert("发送失败，请稍候再试:" + data.errorMessage);
                $("#get-code").attr('disabled', false).html("发送");
            }
        });
    });
    $("#txtusername").blur(function() {
        checkName($(this).val(), 'username');
    });
    $("#txtemailnumber").blur(function() {
        checkName($(this).val(), 'email');
    });

    function checkName(name, type) {
        $.ajax({
            url: /*[[${#request.getContextPath()} + '/api/checkUsername']]*/ "http://localhost:8080/future-novel/api/sendCaptcha",
            data: {
                name: name,
                type: type
            },
            type: "GET",
            success: function(data, status, jqXHR) {},
            error: function(jqXHR, textStatus, errorThrown) {
                alert("1");
                const data = JSON.parse(jqXHR.responseText);
                console.error(data.errorMessage);
                alert(data.errorMessage);
            }
        });
    }
    $("#reg-form").submit(function(event) {
        if ($("#txtusername").val().length < 3) {
            alert("用户名过短");
            event.preventDefault();
            event.stopPropagation();
        }
        if (!$("#txtemailnumber").val().match(/^(\w){3,}(\.\w+)*@(\w){2,}((\.\w+)+)$/)) {
            alert("邮箱格式不正确");
            event.preventDefault();
            event.stopPropagation();
        }
        if ($("#txtphonepwd").val() !== $("#txtphonepwd2").val()) {
            alert("密码输入不一致");
            event.preventDefault();
            event.stopPropagation();
        }
        if ($("#txtphonenumber").val().length < 11) {
            alert("手机号码不正确");
            event.preventDefault();
            event.stopPropagation();
        }
        if ($("#textemailcode").val().length < 4) {
            alert("验证码不正确");
            event.preventDefault();
            event.stopPropagation();
        }
    });
});