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


    // var first = document.getElementById("first");
    // var classify_list = document.getElementById("classify-list");

    // first.onclick = function() {
    //     classify_list.style.display = classify_list.style.display == "block" ? "none" : "block";
    // }

};