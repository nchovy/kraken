define(["/lib/knockout-2.1.0.debug.js", "/component/logdb.querybar.js", "/component/spreadsheet.js", "/component/util.js", "/component/window.js", "/component/form.js"], function(ko, QueryBar, Spreadsheet, Util, Win, Form) {
	var className = "Component.LogQuery";

	var dropdownCellHTML = function(txt) { 
		return '<div class="spret-caret btn-group pull-right" style=" margin-right: 0px; margin-left: -18px">\
			<button data-toggle="dropdown" class="btn btn-mini" style="line-height: 16px; padding: 1px 4px; border-radius: 4px">\
				<span class="caret"></span>\
			</button>\
			<ul class="dropdown-menu">\
				<li class="dropdown-submenu">\
					<a href="#">결과 내 검색</a>\
					<ul class="dropdown-menu">\
						<li><span style="padding: 3px 20px; color: #999">결과 내 &gt; ' + txt + '</span></li>\
						<li class="divider"></li>\
						<li><a href="#" data-event-click="equal">같은 결과</a></li>\
						<li><a href="#" data-event-click="notEqual">같지 않은 결과</a></li>\
						<li><a href="#" data-event-click="contain">포함한 결과</a></li>\
					</ul>\
				</li>\
				<li class="dropdown-submenu">\
					<a href="#">다시 검색</a>\
					<ul class="dropdown-menu">\
						<li><span style="padding: 3px 20px; color: #999">다시 검색 &gt; ' + txt + '</span></li>\
						<li class="divider"></li>\
						<li><a href="#">같은 결과</a></li>\
						<li><a href="#">같지 않은 결과</a></li>\
						<li><a href="#">포함한 결과</a></li>\
					</ul>\
				</li>\
			</ul>\
		</div>';
	}

	var dropdownCellHeaderHTML = function(txt) { 
		return '<div class="spret-caret-header btn-group pull-right" style=" margin-right: 0px; margin-left: -18px">\
			<a data-toggle="dropdown" class="close" style="line-height: 16px; padding: 1px 4px; border-radius: 4px">\
				<span class="caret" style="vertical-align: middle"></span>\
			</a>\
			<ul class="dropdown-menu">\
				<li><a href="#" data-bind="click: $parent.sortBy.callFn(this, $data)">정렬 (내림차순)</a></li>\
				<li class="dropdown-submenu">\
					<a href="#">통계</a>\
					<ul class="dropdown-menu">\
						<li><a href="#">상위 N개</a></li>\
						<li><a href="#">개수</a></li>\
						<li><a href="#">고유값의 개수</a></li>\
						<li><a href="#">최대</a></li>\
						<li><a href="#">최소</a></li>\
						<li><a href="#">평균</a></li>\
					</ul>\
				</li>\
				<li><a href="#" data-bind="click: $parent.lookup.callFn(this, $data)">값 변환</a></li>\
				<li><a href="#" data-bind="click: $parent.renameAs.callFn(this, $data)">이름 변경</a></li>\
				<li><a href="#" data-bind="click: $parent.hideField.callFn(this, $data)">필드 숨기기</a></li>\
				<li><a href="#" data-bind="click: $parent.filterBy.callFn(this, $data)">필터링</a></li>\
			</ul>\
		</div>';
	}

	ko.bindingHandlers[className] = {
		init: function(el, viewModelAccr, allBindingAccr, viewModel, bindingCtx) {
			var instance = viewModelAccr(), allBindings = allBindingAccr();

			var gridTemplateName = (!!allBindings.tmpl) ? allBindings.tmpl.id : "tmpl.default." + className;
			var templateEngine = new ko.nativeTemplateEngine();

			function afterRender() {
				instance.el = el;
				var $el = $(el);
				var $boxResult = $el.find("#box-result");
				
				setTimeout(function() {
					instance.spreadsheet = new Spreadsheet($boxResult, instance.vm, {
						"debug": false,
						"headerTemplate": '<span data-bind="text: headerText"></span>' + dropdownCellHeaderHTML(""),
						"onCellClick": function(el, e) {
							$(".spret-caret").remove();

							var txt = $(el).text();
							var caret = $(dropdownCellHTML(txt));
							caret.on("mousedown", function(e) {
								e.stopPropagation();
							});

							$(el).append(caret);

							$(el).find("a[data-event-click]").on("click", function(e) {

								var colname = $(el).attr("data-bind").split("[")[2].split("]")[0].replace(/\"/gi, "");
								var fn = $(this).attr("data-event-click");
								instance.vm[fn].call(this, colname, txt)
							})
						},
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
								//try {
									ko.applyBindings(instance.vm, el.find("tbody")[0]);
								/*}
								catch(e) {
									console.log(e)
								}
*/
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
		init: function(qbar, color) {

			qbar.self = qbar;
			qbar.name = className;

			qbar.color = color;

			qbar.tabName = ko.computed(function() {
				var qtext = qbar.Logdb.activeQuery();
				if(!!qtext) {
					return qtext;
				}
				else {
					return "(New Query)";
				}
			})

			qbar.vm = new QueryBar.ViewModel();

			qbar.vm.sortBy = function(data) {
				var originalQuery = qbar.Logdb.activeQuery();
				qbar.input(originalQuery + " | sort by " + data.rowText);
				qbar.search();
			}

			qbar.vm.lookup = function(data) {
				var originalQuery = qbar.Logdb.activeQuery();
				
			}			

			qbar.vm.renameAs = function(data) {
				Form.clear("#formRenameField");
				Win.open("#modalRenameField");

				$("#modalRenameField label[data-form=before]").text(data.rowText + " → ");

				$("#btnRenameField").on("click", function(e) {
					e.preventDefault();

					var newval = $("#modalRenameField input[data-form=name]").val();
					var originalQuery = qbar.Logdb.activeQuery();

					qbar.input(originalQuery + " | rename " + data.rowText + " as " + newval);
					qbar.search();

					Win.close("#modalRenameField");
					$(this).off("click");
				});
			}

			qbar.vm.hideField = function(data) {
				var originalQuery = qbar.Logdb.activeQuery();
				qbar.input(originalQuery + " | fields - " + data.rowText);
				qbar.search();
			}

			qbar.vm.filterBy = function(data) {
				var originalQuery = qbar.Logdb.activeQuery();
				
			}


			qbar.vm.equal = function(colname, txt) {
				var originalQuery = qbar.Logdb.activeQuery();
				var q = originalQuery + " | search " + colname + " == \"" + txt + "\"";
				
				qbar.input(q);
				qbar.search();
			}

			qbar.vm.notEqual = function(colname, txt) {
				var originalQuery = qbar.Logdb.activeQuery();
				var q = originalQuery + " | search " + colname + " != \"" + txt + "\"";
				
				qbar.input(q);
				qbar.search();
			}

			qbar.vm.contain = function(colname, txt) {
				var originalQuery = qbar.Logdb.activeQuery();
				var q = originalQuery + " | search " + colname + " contain \"" + txt + "\"";
				
				qbar.input(q);
				qbar.search();
			}


			qbar.Logdb.on("pageLoaded", function(m) {
				console.log(m.body);
				var currIdx = qbar.spreadsheet.getCurrentIndex();
				qbar.vm.pushMany(m.body.result, currIdx);
			});

			qbar.Logdb.on("onTimeline", function(m) {
				console.log("timeline:", m.body);

				if(!!m.body.count) {
					qbar.vm.totalCount(m.body.count);
					//$("#divTotalCount").text(m.body.count + " logs");
				}

				//bar.updateData( convertTimeline( m.body ) );

			});;

			qbar.onSearch = function(self, el) {
				$("#divTotalCount").text("0 result");
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