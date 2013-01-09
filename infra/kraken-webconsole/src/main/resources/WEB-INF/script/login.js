require(["/lib/jquery.js", "/core/connection.js", "/core/login.js"], 
	function(_$, socket, loginManager) {

	$("#txtId").val("root");
	$("#txtPassword").val("kraken");

	$("#btnLogin").on("click", function(e) {
		e.preventDefault();
		e.stopPropagation();

		var id = $("#txtId").val();
		var pw = $("#txtPassword").val();

		loginManager.doLogin(id, pw, function(m, raw) {

			console.log(raw)
			
			if(m.isError) {
				alert(raw[0].errorMessage);
				return;
			}

			if(m.body.result === "success") {
				location.href = "home.html";
			}
		});
	});


});