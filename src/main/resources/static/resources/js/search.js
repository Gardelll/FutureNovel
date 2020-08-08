$(function() {
    function displaywindowsSize() {
        var w = document.documentElement.clientWidth;
        var h = document.documentElement.clientHeight;
        if (w < 1200) {
            $("#first").css("width", 990 + "px");
            $(".box-center").css("width", 990 + "px");
            $("#second").css("width", 990 + "px");
            $("#main-content-wrap").css("width", 990 + "px");
        }
        if (w > 1200) {
            $("#first").css("width", 1200 + "px");
            $(".box-center").css("width", 1200 + "px");
            $("#second").css("width", 1200 + "px");
            $("#main-content-wrap").css("width", 1200 + "px");
        }
    }
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
$(".time-tip").click(function() {
    $(".search-tool-tip").show();
});
$(".time-sure").click(function() {
    $(".search-tool-tip").hide();
});

function doSearch(e) {
    if (e != null && e.keyCOde != 13) {
        return false;
    }
}
$("#form").submit(function(event) {
    var goSearch = $("#goSearch").val();
    if (goSearch == "") {
        alert("请输入正确格式！")
        return false;
    } else {
        return true;
    }
});