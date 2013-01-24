define(["/lib/knockout-2.1.0.debug.js", "/component/logdb.querybar.js", "/component/spreadsheet.js", "/component/util.js", "/component/window.js"], function(ko, QueryBar, Spreadsheet, Util, Win) {
	var className = "Component.LogQuery";

	ko.bindingHandlers[className] = {
		init: function(el, viewModelAccr, allBindingAccr, viewModel, bindingCtx) {
			var instance = viewModelAccr(), allBindings = allBindingAccr();

			var gridTemplateName = (!!allBindings.tmpl) ? allBindings.tmpl.id : "tmpl.default." + className;
			var templateEngine = new ko.nativeTemplateEngine();

			function afterRender() {
				var $el = $(el);
				var $boxResult = $el.find("#box-result");
				
				setTimeout(function() {
					instance.spreadsheet = new Spreadsheet($boxResult, instance.vm, {
						"debug": false,
						"colDataBind": function(rowidx, colidx, prop) {
							return 'text: rowCache[' + rowidx + ']["' + prop + '"]';
						},
						"onRenderRow": function(idx, el) {
							
						},
						"onRender": function() {

						},
						"onRenderRows": Util.throttle(function(top, bottom, el, handler) {
							if(instance.Logdb.activeId() === -1) return;

							instance.Logdb.getResult(instance.Logdb.activeId(), top, bottom - top, function() {
								console.log("after getResult", top, bottom - top)
								try {
									ko.applyBindings(instance.vm, el.find("tbody")[0]);
								}
								catch(e) {
									console.log(e)
								}

								handler.done();
							});
						}, 200)
					});
				}, 100);

				$el.find(".btn.OpenModalDownload").on("click", function() {
					Win.open("#modalDownload");
				});

				$el.find(".btn.Minimize").on("click", function() {
					if($("#box-query").hasClass("scaled")) {
						$("#box-query").removeClass("scaled");
					}
					else {
						$("#box-query").addClass("scaled")
					}
					
				});
			}

			ko.renderTemplate(gridTemplateName, instance, { 
				"templateEngine": templateEngine,
				"afterRender": afterRender
			}, el);
		},
		update: function(el, viewModelAccr, allBindingAccr, viewModel, bindingCtx) {
		}
	}	
	
	ko[className] = {
		name: className,
		instance: QueryBar.instance,
		init: function(qbar) {

			qbar.self = qbar;
			qbar.name = className;

			qbar.vm = new QueryBar.ViewModel();

			qbar.Logdb.on("pageLoaded", function(m) {
				console.log(m.body);
				var currIdx = qbar.spreadsheet.getCurrentIndex();
				qbar.vm.pushMany(m.body.result, currIdx);
			});

			qbar.Logdb.on("onTimeline", function(m) {
				console.log("timeline:", m.body);

				if(!!m.body.count) {
					qbar.vm.totalCount(m.body.count);
					$("#divTotalCount").text(m.body.count + " logs");
				}

				//bar.updateData( convertTimeline( m.body ) );

			});;

			qbar.onSearch = function(self, el) {
				$("#divTotalCount").text("0 log");
				qbar.vm.clear();
				qbar.spreadsheet.clear();
			}

			qbar.nowQuerying.subscribe(function(val) {
				var $el = $(qbar.el);
				if(val) {
					$el.find(".search-query").removeClass("complete").addClass("querying");
				}
				else {
					$el.find(".search-query").removeClass("querying").addClass("complete");
				}
			});

		}
	}

	return ko[className];
})