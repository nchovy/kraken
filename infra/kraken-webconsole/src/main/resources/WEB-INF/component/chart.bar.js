define(["/lib/jquery.js", "/lib/knockout-2.1.0.debug.js", "/lib/d3.v2.amd.js", "/component/kuro.js", "/component/data.js"], function(_$, ko, d3, $K, Data) {
	var componentName = "Kuro.Chart.Bar";
	var Bar = $K.namespace("Chart.Bar");

	Bar.ViewModel = function(data, options) {
		Data.ViewModel.call(this, data);

		options = $.extend({}, options);

		this.valueKey = options.valueKey;
		this.labelKey = options.labelKey;

		var vals = this.items().map(function(d) {
			return d[options.valueKey];
		});
		
		var xfn = d3.scale.linear()
				.domain([0, d3.max(vals)])
				.range([0, d3.max(vals)]);

		this.refLineX = d3.nest().map(xfn.ticks(5)).map(function(d) {
			return Math.floor(xfn(d));
		});

		//this.refLineX = [0, 20, 40, 60, 80];
	}

	function getComputedObject(item, vm) {
		item.bar = {};

		if(!vm.scale) {

			var vals = vm.items().map(function(d) {
				return d[vm.valueKey];
			});

			vm.scale = ($(vm.el).width() - 20) / d3.max(vals); // 20 equals translate-x * 2
			console.log(vm.scale);
		}

		item.bar.posY = 50 * vm.items.indexOf(item) + 10;
		item.bar.width = item[vm.valueKey] * vm.scale;
		item.bar.label = item[vm.labelKey];
		return item;
	}

	Bar.ViewModel.inherit(Data.ViewModel);

	Bar.ViewModel.prototype.add = function(item) {
		this.computedItems.push(getComputedObject(item, this));
		return this._super.add.call(this, item);
	}

	ko.bindingHandlers[componentName] = {
		init: function(el, viewModelAccr, allBindingAccr, viewModel, bindingCtx) {
			return { 'controlsDescendantBindings': true };
		},
		update: function(el, viewModelAccr, allBindingAccr, viewModel, bindingCtx) {
			var allBindings = allBindingAccr();
			
			var gridTemplateName = (!!allBindings.tmpl) ? allBindings.tmpl.id : "tmpl.default." + componentName;
			viewModel.templateName = gridTemplateName;
			viewModel.el = el;

			viewModel.computedItems = ko.observableArray(viewModel.items().map(function(item) {
				return getComputedObject(item, viewModel);
			}));

			viewModel.height = $(viewModel.el).height() - 30; // 30 equals translate-y * 2
			viewModel.barHeight = 30;

			var templateEngine = new ko.nativeTemplateEngine();
			
			ko.renderTemplate(gridTemplateName, viewModel, { templateEngine: templateEngine }, el);
		}
	}

	ko[componentName] = {
		ViewModel: Bar.ViewModel
	}

	return ko[componentName];
});