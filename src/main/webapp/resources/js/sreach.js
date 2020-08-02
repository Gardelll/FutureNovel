$(function () {
    function GetTags(){
        $.ajax({
            url:"http://localhost:8080/future-novel/api/novel/tags/all",
            type:"get",
            data:"",
            success:function (data) {
                console.log(data);
                for (var i=0;i<data.length;i++){
                    $("#data").append("<option value='" + "" + data[i] + "" + "'>")
                }
            },error:function (data) {
                console.log(data.errorMessage)
            }
        });
    }
    GetTags();
    function GetSeries () {
        $.ajax({
            url:"http://localhost:8080/future-novel/api/novel/series/all",
            type:"get",
            data:"",
            success:function (data) {
                console.log(data);
                for (var i=0;i<data.length;i++){
                    $("#data").append("<option value='" + "" + data[i] + "" + "'>")
                }
            },error:function (data) {
                console.log(data.errorMessage)
            }
        });
    }
    GetSeries ();
    function displaywindowsSize() {
        var w = document.documentElement.clientWidth;
        var h = document.documentElement.clientHeight;
        if(w < 1200) {
            $("#first").css("width",990+"px");
            $(".box-center").css("width",990+"px");
            $("#second").css("width",990+"px");
            $("#main-content-wrap").css("width",990+"px");
        }
        if(w > 1200) {
            $("#first").css("width",1200+"px");
            $(".box-center").css("width",1200+"px");
            $("#second").css("width",1200+"px");
            $("#main-content-wrap").css("width",1200+"px");
        }
    }

    $("#fixed-input").keydown(function () {

    })
    window.addEventListener("resize", displaywindowsSize);
    displaywindowsSize();

    // function tpformsubmit(index) {
    //     str='http://localhost:8080/future-novel/search?page='+index+'&per_page=2'
    //     $("#form").attr("action",str);
    // }
    // $(".rank-page").click(function () {
    //     var page = $(this).text();
    //     console.log(page)
    //     tpformsubmit(page);
    // })

})