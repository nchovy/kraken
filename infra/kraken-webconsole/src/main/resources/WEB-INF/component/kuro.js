define(['/lib/jquery.js'], function(_$) {

if(!Function.prototype.inherit) {
	(function() {
		function F() {}

		Function.prototype.inherit = function(superFn) {
			F.prototype = superFn.prototype;
			this.prototype = new F();
			this.prototype.constructor = this;
			this.prototype._super = superFn.prototype;
		}
	}());
}

Function.prototype.callFn = function() {
	var self = this;
	var target = arguments[0];
	var args = Array.prototype.slice.call(arguments);
	args.splice(0,1);

	return (function() {
		self.apply(target, args);
	})
}

function pad(n) { return n < 10 ? '0' + n : n }

Date.prototype.getISODateString = function() {

	return this.getFullYear() + '/'
		+ pad(this.getMonth() + 1) + '/'
		+ pad(this.getDate()) + ' '
		+ pad(this.getHours()) + ':'
		+ pad(this.getMinutes()) + ':'
		+ pad(this.getSeconds()) + ' '
		+ pad(this.getMilliseconds());
}

Date.prototype.getISODateOnlyString = function() {
	return this.getFullYear() + '/'
		+ pad(this.getMonth() + 1) + '/'
		+ pad(this.getDate());
}

Date.prototype.getISOTimeString = function() {
	return pad(this.getHours()) + ':'
		+ pad(this.getMinutes()) + ':'
		+ pad(this.getSeconds());
}

Array.prototype.orderBy = function(property) {
	var arr = [];
	for (var i = 0; i < this.length; i++) {
		arr.push(this[i][property]);
	};
	return arr;
}

Array.prototype.diff = function(a) {
	return this.filter(function(i) {return !(a.indexOf(i) > -1);});
};

Array.prototype.insertAt = function(item, idx) {
	this.splice(idx, 0, item);
}

Array.prototype.removeAt = function(idx) {
	this.splice(idx, 1);
}

Array.prototype.removeAll = function() {
	//this.splice(0, this.length);
	this.length = 0;
}

var Kuro = (function() {
	function namespace(string) {
		var object = this;
		var levels = string.split(".");

		for (var i=0, l = levels.length; i<l; i++) {
			if(typeof object[levels[i]] == "undefined") {
				object[levels[i]] = {};
			}

			object = object[levels[i]];
		}
		return object;
	}

	function log(str) {
		$("<div>").text(str).prependTo(logger);
	}

	var logger = $("<div>").css("top","0")
		.css("left","0")
		.css("position","fixed")
		.height(200)
		.css("width","100%")
		.css("z-index", "9999")
		.css("background","white")
		.css("border","1px solid red")
		.css("-webkit-overflow-scrolling", "touch")
		.css("overflow", "scroll")
		//.appendTo("body");


	return {
		namespace: namespace,
		log: log
	}
}());

if(!window.Kuro) {
	window.Kuro = Kuro;
}

return Kuro

});