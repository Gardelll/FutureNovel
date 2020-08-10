window.onload = function down() {
    var parent = document.getElementById("side-parent");
    var children = document.getElementById("side-group");

    parent.onclick = function() {
        children.style.display = children.style.display == "block" ? "none" : "block";
    }

}