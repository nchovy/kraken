define([], function() {

	var Locale = {
		getCurrentLocale: function() {
			return "en";
		}
	};

	var Core = parent.Core;
	if(!Core) {
		Core = parent.Core = {};
	}

	if(!Core.Locale) {
		console.log("register Locale manager globally");
		parent.Core.Locale = Locale;
	}

	return Core.Locale;

});