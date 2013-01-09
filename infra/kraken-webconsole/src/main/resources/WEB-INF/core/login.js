define(['/core/connection.js','/lib/sha1-amd.js'], function(socket, CryptoJS) {



function doLogin(login_name, password, loginCallback) {
	socket.send("org.krakenapps.dom.msgbus.LoginPlugin.hello", {}, function(m,b,c) {

		var nonce = m.body.nonce;
		var hashedpwd = CryptoJS.SHA1(password).toString(CryptoJS.enc.Hex);
		var hash = CryptoJS.SHA1(hashedpwd + nonce).toString(CryptoJS.enc.Hex);

		socket.send("org.krakenapps.dom.msgbus.LoginPlugin.login", {
			"nick": login_name,
			"hash": hash,
			"force": false
		}, loginCallback);
	});
}

return {
	"doLogin": doLogin
}

});