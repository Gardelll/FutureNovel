//这里只有部分的动画
window.onload = function () {
    $("#modify-novel").click(function () {
        $(".modify-Novel").css("top",0);
    })
    var tags = [];
    var actor=["haha","heihei"];
    $("#book-tags").keydown(function (event) {
        if(event.keyCode==32){
            if($("#book-tags").val().length>=4){
                alert("标签不得超过三个字")
                $("#book-tags").val("");
            }else{
                var str= $("#book-tags").val();
                var test= " <div class=\"tags-s\">\n" +
                    "<a href=\"###\" class=\"tags-text\">"+str+"</a>\n" +
                    "<i class=\"fa fa-close close2\" ></i>\n" +
                    "</div>";
                $(".tags").append(test);
                $("#book-tags").val("");
            }
        }
    });
    $(".tags").on('click','.close2',function(){
        $(this).parent().remove();
    });
    function GetTags(){
        $.ajax({
            url:"http://localhost:8080/future-novel/api/novel/tags/all",
            type:"get",
            data:"",
            success:function (data) {
                for (var i=0;i<data.length;i++){
                    $("#tag").append(" <option value='" + "" + data[i] + "" + "'>")
                }
            },error:function (data) {
                console.log(data)
            }
        });
    }
    GetTags();
    $("#file").on("change",function () {
        var formData = new FormData ;
        formData.append("file",$("#file")[0].files[0]);
        $.ajax({
            url: "http://localhost:8080/future-novel/api/img/upload",
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
