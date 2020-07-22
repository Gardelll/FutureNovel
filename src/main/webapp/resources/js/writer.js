$(function () {



	$("body").delegate("#book-name,#title-name", "propertuchange input", function () {
		if ($("#book-name").val().length > 0 && $("#title-name").val().length > 0) {
			//让发布按钮可用
			$("#writer-release").prop("disabled", false);
		} else {
			//让发布按钮不可用
			$("#writer-release").prop("disabled", true);
		}
	});

	//获取当前时间的方法
	function getNowFormatDate() {
		var date = new Date();
		var seperator1 = "-";
		var seperator2 = ":";
		var month = date.getMonth() + 1;
		var strDate = date.getDate();
		var minute = date.getMinutes();
		if (month >= 1 && month <= 9) {
			month = "0" + month;
		}
		if (strDate >= 0 && strDate <= 9) {
			strDate = "0" + strDate;
		}
		if (minute >= 0 && minute <= 9) {
			minute = "0" + minute;
		}
		var currentdate = date.getFullYear() + seperator1 + month + seperator1 + strDate
			+ " " + date.getHours() + seperator2 + minute;
		return currentdate;
	}

	$("#writer-release").click(function () {
		var Nowtime = getNowFormatDate();
		var $BookName = $("#book-name").val();
		var $TitieName = $("#title-name").val();
		var activeEditor = tinymce.activeEditor;
		var editBody = activeEditor.getBody();
		activeEditor.selection.select(editBody);
		var text = activeEditor.selection.getContent({"format": "html"});
		var Text = activeEditor.selection.getContent({"format": "text"});
		var TextLength = Text.length;
		if (text.length == 1) {
			alert("内容不能为空");
		} else {
			var $xiaoshuo = createEle($BookName, $TitieName, text, Nowtime,TextLength);
			$(".writer-list").prepend($xiaoshuo);
//			$(".writer-lis").click(function(){
//			if($(".works-content").height()==0){
//				$(".works-content").css("height",65+"px");
//				$("#left-not").css("transform","rotateZ(90deg)");
//			}else{
//				$(".works-content").css("height",0);
//				$("#left-not").css("transform","");
//			}
//			});
		}
	});

//	$("#writer-release").click(function(){
//		console.log($(".content").val());

//		if($("#writer-book-name").val()==""||$("#writer-book-chapter").val()==""){
//			alert("书名或章节名不能为空");	
//		}else{
//			var $shuming = $("#writer-book-name").val();
//			var $zhangjie = $("#writer-book-chapter").val();
//			//var $text = $(".content").val();
//			var $xiaoshuo = createEle($shuming,$zhangjie,$text);
//			$(".writer-list").prepend($xiaoshuo);
//		}
//		-
//	});
	function createEle(shuming, zhangjie, text, time,TextLength) {
		var $xiaoshuo = $("<li><div class='writer-lis'><p>" + "" + shuming + "" + "</p><p>" + "" + zhangjie + "" + "</p></div><div class='works-content'><span class='neirong'>" + "" + text + "" + "</span><span class='neirong'style='margin: 0;'>...</span><span id='writer-data'>" + "" + time + "" + "</span><span id='words-num' style='margin-left:15px;'>" + "" + "字数：" + "" + TextLength + "" + "</span></div></li>")
		return $xiaoshuo;
	}
})