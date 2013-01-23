define(["/lib/jquery.js", "/lib/knockout-2.1.0.debug.js", "/component/kuro.js", "/component/data.js"], function(_$, ko, $K, Data) {
	var className = "Kuro.List";
	var List = $K.namespace("List");

	List.ViewModel = function(data) {
		Data.ViewModel.call(this, data);
	}

	List.ViewModel.inherit(Data.ViewModel);

	ko.bindingHandlers[className] = {
		init: function(el, viewModelAccr, allBindingAccr, viewModel, bindingCtx) {
			return { 'controlsDescendantBindings': true };
		},
		update: function(el, viewModelAccr, allBindingAccr, viewModel, bindingCtx) {
			var viewModel = viewModelAccr(), allBindings = allBindingAccr();
			viewModel.el = el;
			
			var gridTemplateName = (!!allBindings.tmpl) ? allBindings.tmpl.id : "tmpl.default." + className;
			var templateEngine = new ko.nativeTemplateEngine();
			
			ko.renderTemplate(gridTemplateName, viewModel, { templateEngine: templateEngine }, el);
		}
	}	
	
	ko[className] = {
		name: className,
		ViewModel: List.ViewModel
	}

	return ko[className];
})