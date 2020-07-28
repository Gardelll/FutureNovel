window.onload = function() {

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