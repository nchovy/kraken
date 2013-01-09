define(["/lib/knockout-2.1.0.debug.js", "/core/logdb.js"], function(ko, logdbManager) {
	var className = "Kuro.Logdb.Querybar";

	ko.bindingHandlers[className] = {
		init: function(el, viewModelAccr, allBindingAccr, viewModel, bindingCtx) {
			return { 'controlsDescendantBindings': true };
		},
		update: function(el, viewModelAccr, allBindingAccr, viewModel, bindingCtx) {
			var viewModel = viewModelAccr(), allBindings = allBindingAccr();

			var gridTemplateName = (!!allBindings.tmpl) ? allBindings.tmpl.id : "tmpl.default." + className;
			var templateEngine = new ko.nativeTemplateEngine();
			
			ko.renderTemplate(gridTemplateName, viewModel, { templateEngine: templateEngine }, el);
		}
	}	
	
	ko[className] = {
		name: className,
		instance: function(option) {
			var that = this;
			var queryPageSize = 15;

			this.self = this;
			this.name = className;

			if(!!option) {
				//console.log("has option")
				queryPageSize = option.queryPageSize;
			}


			this.Logdb = logdbManager.create();

			var inputStr = "table limit=100 local\\interpark-syslog";

			this.input = ko.observable(inputStr);

			this.search = function(a, b) {
				if(that.input().length === 0) {
					console.log("please type query");
				}
				else {
					that.Logdb.dispose(function() {
						that.Logdb.search(that.input(), queryPageSize);
					});
				}

				if(!!that.onSearch) {
					that.onSearch(that, a, b);
				}
			}
		},
		ViewModel: logdbManager.ViewModel
	}

	return ko[className];
})