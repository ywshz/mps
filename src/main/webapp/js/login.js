$(document).ready(init);
function init() {

	$(".form-signin").submit(function(e){
		$("#passwd").val( md5($("#passwd").val() ));
	});

}
