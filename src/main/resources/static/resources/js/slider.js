window.onload = function() {
    var slider = document.getElementById('slider_con1');
    var list1 = document.getElementById('list1');
    var buttons = document.getElementById('buttons').getElementsByTagName('span');
    var prev = document.getElementById('prev');
    var next = document.getElementById('next');
    var index = 1;
    var timer;

    function animate(offset) {
        var newLeft = parseInt(list1.style.left) + offset;
        list1.style.left = newLeft + 'px';
        //无限滚动判断
        if (newLeft > -748) {
            list1.style.left = -3740 + 'px';
        }
        if (newLeft < -3740) {
            list1.style.left = -748 + 'px';
        }
    }

    function play() {
        //重复执行的定时器
        timer = setInterval(function() {
            next.onclick();
        }, 3000)
    }

    function stop() {
        clearInterval(timer);
    }

    function buttonsShow() {
        //将之前的小圆点的样式清除
        for (var i = 0; i < buttons.length; i++) {
            if (buttons[i].className == "on") {
                buttons[i].className = "";
            }
        }
        //数组从0开始，故index需要-1
        buttons[index - 1].className = "on";
    }

    prev.onclick = function() {
        index -= 1;
        if (index < 1) {
            index = 5
        }
        buttonsShow();
        animate(748);
    };

    next.onclick = function() {
        //定时器，index会一直递增，5个小圆点，需要做出判断
        index += 1;
        if (index > 5) {
            index = 1
        }
        animate(-748);
        buttonsShow();
    };

    for (var i = 0; i < buttons.length; i++) {
        (function(i) {
            buttons[i].onclick = function() {

                var clickIndex = parseInt(this.getAttribute('index'));
                var offset = 748 * (index - clickIndex); //这个index是当前图片停留时的index
                animate(offset);
                index = clickIndex; //存放鼠标点击后的位置，用于小圆点的正常显示
                buttonsShow();
            }
        })(i)
    }

    slider.onmouseover = stop;
    slider.onmouseout = play;
    play();
// 按照时间搜索
    function dateForm(){
        var after = document.getElementById("after");
        var before = document.getElementById("before");
        var timeBtn = document.getElementById("timeBtn");
        timeBtn.onclick = function () {
            var afterStr = after.value.split("-");
            var afterValue = afterStr[0] + "年" + afterStr[1] + "月" + afterStr[2] + "日 00:00:00";
            var beforeStr = before.value.split("-");
            var beforeValue = beforeStr[0] + "年" + beforeStr[1] + "月" + beforeStr[2] + "日 00:00:00";

            $("input[name='after']").attr("value", afterValue)
            console.log($("#after").attr("value"))
            $("input[name='before']").attr("value", beforeValue)

        }
    }
    dateForm();
    //时间搜索、热度搜索，输入框可无值
    function checked_one() {
        var timeVal = $('input:radio[value="PUBDATE"]:checked').val();
        var hotVal = $('input:radio[value="HOT"]:checked').val();
        var submit_input = $(".submit-input").val();
        if (timeVal != null || hotVal != null) {
            return true;
        }
        if (submit_input == "") {
            return false;
        }
    }
    checked_one()
};