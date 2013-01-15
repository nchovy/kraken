define(["/lib/jquery.js", "/lib/knockout-2.1.0.debug.js", "/component/kuro.js", "/component/data.js"], function(_$, ko, $K, Data) {
	var className = "Kuro.Tree";
	var Tree = $K.namespace("Tree");

	Tree.ViewModel = function(data) {
		Data.ViewModel.call(this, data);

		this.items = this.children = convertChildrenToObservable(data);

		function convertChildrenToObservable(items) {
			var observableChildren = [];
			$.each(items, function(i, v) {
				var obj = $.extend({}, v);
				obj.name = ko.observable(v.name);
				obj.children = convertChildrenToObservable(v.children);

				observableChildren.push(obj);
			});
			return ko.observableArray(observableChildren);
		}

		bindEvent(this.children(), this);
	}

	var onSelect = function(self, el) {
		$(self.vm.el).find(".active").removeClass("active");
		$(el).parent("li").addClass("active");
	}

	var accordion = function(self, el) {
		var i = $(el);
		if(i.hasClass("icon-plus")) {
			i.removeClass("icon-plus").addClass("icon-minus");
		}
		else {
			i.removeClass("icon-minus").addClass("icon-plus");
		}
		//i.text(i.text() === "-" ? "+" : "-");
		i.parent().nextAll("ul").toggle();
	}

	function bindEvent(collection, vm) {
		$.each(collection, function(i, obj) {
			obj.onSelect = function(self, e) {
				onSelect(self, e.delegateTarget);
				self.vm.select(self);
			}

			obj.accordion = function(self, e) {
				e.stopPropagation();
				accordion(self, e.delegateTarget);
			}

			obj.isSelected = ko.observable(false);

			if(obj.children().length > 0) {
				bindEvent(obj.children(), vm);
			}

			obj.vm = vm;
		});
	}

	Tree.ViewModel.inherit(Data.ViewModel);

	Tree.ViewModel.prototype.add = function(item, parentGuid) {
		/*
		item.children = ko.observableArray([]);
		bindEvent([item], this);
		console.log(item)
		return this._super.add.call(this, item);
		*/
	}

	Tree.ViewModel.prototype.remove = function(guid) {

	}

	ko.bindingHandlers[className] = {
		init: function(el, viewModelAccr, allBindingAccr, viewModel, bindingCtx) {
			return { 'controlsDescendantBindings': true };
		},
		update: function(el, viewModelAccr, allBindingAccr, viewModel, bindingCtx) {
			var viewModel = viewModelAccr(), allBindings = allBindingAccr();
			
			var gridTemplateName = (!!allBindings.tmpl) ? allBindings.tmpl.id : "tmpl.default." + className;
			viewModel.templateName = gridTemplateName;
			viewModel.el = el;
			
			//console.log(viewModel);
			//console.log(allBindings);

			var templateEngine = new ko.nativeTemplateEngine();
			
			ko.renderTemplate(gridTemplateName, viewModel, { templateEngine: templateEngine }, el);
		}
	}

	ko[className] = {
		name: className,
		ViewModel: Tree.ViewModel
	}

	return ko[className];
});