define(['/lib/jquery.js', '/component/kuro.js', "/component/window.js"], function(_$, $K, Win) {

var form = $K.namespace("Form");

(function() {
	function isForm(el, fn) {
		if(el[0].tagName !== "FORM") {
			throw new TypeError("Form." + fn + " failed: element is not FORM tag")
			return false;
		}
		return true;
	}

	function serialize(el) {
		el = $(el);
		if(!isForm(el, arguments.callee.name)) return;

		var ret = {};
		$(el[0]).find("*[data-form]").each(function(i, obj) {
			if(!!obj.dataset.form) {
				ret[obj.dataset.form] = $(obj).val();
			}
		});

		console.log("serialize:", ret)

		return ret;
	}

	function clear(el) {
		el = $(el);
		if(!isForm(el, arguments.callee.name)) return;

		$(el[0]).find("*[data-form]").val("");
	}

	function initform() {
		var btnClose = $("aside.modal-form button.modal-close");
		btnClose.on("click", function(e) {
			e.preventDefault();
			Win.close();
		})
	}

	initform();

	form.serialize = serialize;
	form.clear = clear;

})();

return form;

});