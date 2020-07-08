window.onload = function() {
    //				var direction = document.getElementById("direction");
    //				var first = document.getElementById("first");
    //				var timer =null;
    //				clearInterval(timer);
    //				    timer = setTimeout(function() {
    //				        first.onmouseover = function(){
    //							direction.style.transform = "rotateX(180deg)"
    //						};
    //						first.onmouseout = function(){
    //							direction.style.transform = "rotateX(0deg)"
    //						};
    //				    },1000)

    function displaywindowsSize() {
        var w = document.documentElement.clientWidth;
        var h = document.documentElement.clientHeight;
        var topBoxCenter = document.getElementById("top-box-center");
        if (w < 1200) {
            topBoxCenter.style.width = 990 + "px";
        }
        if (w > 1200) {
            topBoxCenter.style.width = 1200 + "px";
        }
    }
    window.addEventListener("resize", displaywindowsSize);
    displaywindowsSize();
};window.onload = function() {
    //				var direction = document.getElementById("direction");
    //				var first = document.getElementById("first");
    //				var timer =null;
    //				clearInterval(timer);
    //				    timer = setTimeout(function() {
    //				        first.onmouseover = function(){
    //							direction.style.transform = "rotateX(180deg)"
    //						};
    //						first.onmouseout = function(){
    //							direction.style.transform = "rotateX(0deg)"
    //						};
    //				    },1000)

    function displaywindowsSize() {
        var w = document.documentElement.clientWidth;
        var h = document.documentElement.clientHeight;
        var topBoxCenter = document.getElementById("top-box-center");
        if (w < 1200) {
            topBoxCenter.style.width = 990 + "px";
        }
        if (w > 1200) {
            topBoxCenter.style.width = 1200 + "px";
        }
    }
    window.addEventListener("resize", displaywindowsSize);
    displaywindowsSize();
};