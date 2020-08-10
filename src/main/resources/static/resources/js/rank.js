$(function () {
	function displaywindowsSize() {
		var w = document.documentElement.clientWidth;
		var h = document.documentElement.clientHeight;
		var topBoxCenter = document.getElementById("top-box-center");
		var boxCenter = document.getElementById("box-center");
		var boxCenter2 = document.getElementById("box-center2");
		var mainContentWrap = document.getElementById("main-content-wrap");
		if(w < 1200) {
			topBoxCenter.style.width = 990 + "px";
			boxCenter.style.width = 990 + "px";
			boxCenter2.style.width = 990 + "px";
			mainContentWrap.style.left = 100+"px";
		}
		if(w > 1200) {
			topBoxCenter.style.width = 1200 + "px";
			boxCenter.style.width = 1200 + "px";
			boxCenter2.style.width = 1200 + "px";
			mainContentWrap.style.left = 200+"px";
		}
	}
	window.addEventListener("resize", displaywindowsSize);
	displaywindowsSize();
	var classifyList = document.getElementById("classify-list");
	$("#first").mouseover(function() {
		classifyList.style.top = "0";
	});
	$("#first").mouseout(function() {
		classifyList.style.top = -420 + "px";
	});
	classifyList.onmouseover = function() {
		classifyList.style.top = "0";
	};
	classifyList.onmouseout = function() {
		classifyList.style.top = -420 + "px";
	};
});