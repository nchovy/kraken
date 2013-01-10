define(["/component/kuro.js"], function($K) {

var Util = $K.namespace("Util");

function throttle(fn, delay) {
	var timer = null;
	return function () {
		var context = this, args = arguments;
		clearTimeout(timer);
		timer = setTimeout(function () {
			fn.apply(context, args);
		}, delay);
	};
}

Util.throttle = throttle;

return Util;

});