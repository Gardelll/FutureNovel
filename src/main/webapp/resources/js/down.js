window.onload = function down() {
    var parent = document.getElementById("side-parent");
    var children = document.getElementById("side-group");
    var parent2 =document.getElementById("side-parent2");
    var children2 = document.getElementById("side-group2");

    parent.onclick = function() {
        children.style.display = children.style.display == "block" ? "none" : "block";
    }
    parent2.onclick = function() {
        children2.style.display = children2.style.display == "block" ? "none" : "block";
    }
}