window.onclick = function click_tip() {

    var txtphonepwd = document.getElementById("txtphonepwd");
    var password_tip = document.getElementsByClassName("password-tip")[0];


    txtphonepwd.onclick = function() {
        password_tip.style.display = "block";
    }

}