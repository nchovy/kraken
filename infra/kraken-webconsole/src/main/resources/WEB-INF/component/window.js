define(['/lib/jquery.js', '/component/kuro.js'], function(_$, $K) {

var win = $K.namespace("Window");
var backdrop;

$("aside.modal").each(function(i, el) {

	$(el).find(".modal-close").on("click", function(e) {
		e.preventDefault();

		win.close(el);
	});
});

$(document).ready(function() {
	backdrop = $('<div class="modal-backdrop hidden">').appendTo("body");
})

win.close = function(el) {
	backdrop.addClass("hidden");
	$(el).hide();
}

win.open = function(el) {
	backdrop.removeClass("hidden");
	$(el).show();
}

return win;

});