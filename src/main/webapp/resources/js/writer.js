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


})
