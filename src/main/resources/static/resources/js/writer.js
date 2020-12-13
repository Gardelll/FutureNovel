//这里只有部分的动画
window.onload = function () {
    $("#modify-novel").click(function () {
        $(".modify-Novel").css("top",0);
    })
    $("#file").on("change",function () {
        var formData = new FormData ;
        formData.append("file",$("#file")[0].files[0]);
        $.ajax({
            url: contextPath + "/api/img/upload",
            type: "put",
            dataType: "JSON",
            data:formData,
            processData: false,
            contentType: false,
            success:function (data) {
                $("#sure").click(function () {
                    $("#addIMG").attr("src",data.url);
                    var imgurl = data.url;
                });
            },error(){
                console.log("上传的图片有误")
            }
        })

    });


}
