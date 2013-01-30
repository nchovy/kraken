define(["/lib/knockout-2.1.0.debug.js", "/core/logdb.js", "/component/window.js"], function(ko, logdbManager, Win) {
	var className = "Kuro.Logdb.Querybar";

	ko.bindingHandlers[className] = {
		init: function(el, viewModelAccr, allBindingAccr, viewModel, bindingCtx) {
			return { 'controlsDescendantBindings': true };
		},
		update: function(el, viewModelAccr, allBindingAccr, viewModel, bindingCtx) {
			var viewModel = viewModelAccr(), allBindings = allBindingAccr();
			//viewModel.el = el;

			var gridTemplateName = (!!allBindings.tmpl) ? allBindings.tmpl.id : "tmpl.default." + className;
			var templateEngine = new ko.nativeTemplateEngine();
			
			ko.renderTemplate(gridTemplateName, viewModel, {
				"templateEngine": templateEngine,
				"afterRender": afterRender
			}, el);


			function afterRender() {
				var $el = $(el);

				$el.find(".btn.OpenModalSaveQuery").on("click", function(e) {
					e.preventDefault();
					Win.open("#modalSaveQuery");
				});

				$el.find(".btn.OpenScheduleQuery").on("click", function(e) {
					e.preventDefault();
					console.log("schedule query")
				});
			}
		}
	}	
	
	ko[className] = {
		name: className,
		instance: function(option) {
			var that = this;
			var queryPageSize = 15;

			this.self = this;
			this.name = className;

			this.Logdb;

			if(!!option) {
				queryPageSize = option.queryPageSize || 15;

				if(!!option.query) {
					this.Logdb = option.query;
				}
			}

			if(!this.Logdb) {
				var logdbOption;
				if(!!option.logdbOption) logdbOption = option.logdbOption;
				
				this.Logdb = logdbManager.create(logdbOption);
			}

			//var inputStr = "table limit=10000 local\\interpark-syslog";

			this.input = ko.observable(this.Logdb.activeQuery());

			this.nowQuerying = ko.observable(false);

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

			this.Logdb.on("created", function() {
				that.nowQuerying(true);
			});

			this.Logdb.on("loaded", function() {
				that.nowQuerying(false);
			})

			this.stop = function() {
				that.Logdb.stop();
				that.nowQuerying(false);
			}
		},
		ViewModel: logdbManager.ViewModel
	}

	return ko[className];
})