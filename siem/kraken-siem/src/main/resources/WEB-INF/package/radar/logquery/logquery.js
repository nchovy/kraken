require(["/lib/jquery.js",
	"/lib/jquery.svganim-amd.js",
	"/lib/knockout-2.1.0.debug.js",
	"/core/connection.js",
	"/component/util.js",
	"/component/logdb.querybar.js",
	"/component/spreadsheet.js",
	"/component/chart.stackedbar.js",
	"/lib/knockout.mapping-latest.debug.js",
	"/component/window.js",
	"/component/dd.js",
	"/component/list.js",
	"/component/data.grid.js",
	"/bootstrap/js/bootstrap.amd.js",
	"comp.logquery.js",
	"/package/system/orgchart/testdata2.js"
	],
	function(_$, _$svganim, ko, Socket, Util, QueryBar, Spreadsheet, StackedBar, komap, Win, DD, List, Grid, Bootstrap, LogQuery, json2) {

	var Core = parent.Core;

	var initTimelineMsg = {
		"id": 6,
		"values": [1,1,1,1,1,1,1,1,1,1],
		//"values": [5,10,20,40,6,80,0,30,60,100],
		"count": 100,
		"span_field": "Minute",
		"type": "eof",
		"span_amount": 1,
		"begin": "2012-06-24 23:49:00+0900"
	};
	var bar = new StackedBar.ViewModel("#timeline-chart", convertTimeline(initTimelineMsg));

	var vmQueryPane = new List.ViewModel([]);

	vmQueryPane.onSelect = function(lq) {
		if(!!lq.timeline) {
			bar.updateData(lq.timeline);
		}
		else {
			bar.updateData(convertTimeline(initTimelineMsg));
		}

		if(!!lq.totalCount) {
			$("#divTotalCount").text(lq.totalCount + " results");
		}
		else {
			$("#divTotalCount").text("0 result");
		}
	}

	vmQueryPane.onBeforeRemove = function() {
		if(vmQueryPane.length() == 1) return false;
	}

	vmQueryPane.onAfterRemove = function(data) {
		vmQueryPane.selectAt(vmQueryPane.length() - 1);
		vmQueries.remove(data.Logdb)
	}
	
	function addTab(query) {
		var lq;
		if(!!query) {
			lq = new LogQuery.instance({
				"query": query
			});
		}
		else {
			lq = new LogQuery.instance();
		}
		LogQuery.init(lq);

		lq.Logdb.on("onTimeline", function(m) {

			lq.timeline = convertTimeline(m.body);

			if(lq.isSelected()) {

				if(!!m.body.count) {
					lq.totalCount = m.body.count;
					$("#divTotalCount").text(lq.totalCount + " results");
				}

				bar.updateData(lq.timeline);
			}
		});

		vmQueryPane.add(lq);
		vmQueryPane.select(lq);

		return lq;
	}

	ko.applyBindings(vmQueryPane, document.getElementById("tabs"))
	ko.applyBindings(vmQueryPane, document.getElementById("box-query"));

	$("#btnAddTab").on("click", function(e) {
		addTab();
	});
	addTab();


	// toolbar

	$("#btnDownloadResult").on("click", function(e) {
		e.preventDefault();

		Socket.send("org.krakenapps.msgbus.TokenPlugin.issueToken", {
			"token_data": {
				"query_id": qbar.Logdb.getId(),
				"offset": 0,
				"limit": 40,
				"file_name": "hello.csv",
				"type": "csv",
				"charset": "utf-8"
			}
		}, function(m) {
			console.log("Token_id", m.body.token_id);
		})

		Win.close("#modalDownload");
	});
	


	// Variables
	var vmQueries;

	// Pane

	$("#pane-change li a").on("click", function() {
		$("#box-query-status .pane").hide();

		var target = $(this).data("target");

		$("#" + target).show();


		$(this).parents("#pane-change").prev(".btn").find("span.ltext").text($(this).text());
	});
	

	$(".toggle-edit").on("click", function() {
		$(".mode-edit").addClass("roll").fadeIn('fast');
		$(".mode-default").addClass("up").fadeOut('fast');

		vmQueries.isEditMode(true);
		vmQueries.selectAll(false);
	});

	$(".toggle-default").on("click", function() {
		$(".mode-edit").addClass("up").fadeOut('fast');
		$(".mode-default").addClass("roll").fadeIn('fast');

		vmQueries.isEditMode(false);
		vmQueries.selectAll(false);
	});

	function selectOnEditMode(data) {
		var countSelected = vmQueries.items().filter(function(q) {
			return q.isSelected();
		}).length;

		$("#btnRemoveQueries").text("Remove(" + countSelected + ")");
	}

	function hidePopover(e) {
		if(!!e) {
			e.stopPropagation();
		}

		var popover = $(vmQueries.el).find(".popover");
		popover.hide();
		$("#box-query *").off("click", hidePopover);
	}

	function selectOnDefaultMode(data, e) {
		console.log(vmQueries)
		var offset = $(vmQueries.el).find("li.active").offset();

		var popover = $(vmQueries.el).find(".popover")
			.addClass("in")
			.css("top", (offset.top - 140) + "px")
			.css("left", e.pageX + "px")
			.show();

		$("#box-query *").on("click", hidePopover);
	}

	// Running Queries
	Core.LogDB.getQueries(function(queries) {
		vmQueries = queries;

		vmQueries.isEditMode = vmQueries.canSelectMulti;

		vmQueries.singleSelected = ko.computed(function() {
			var blank = {
				activeId: ko.observable("activeId"),
				activeQuery: ko.observable("activeQuery")
			};

			if(!this.isEditMode()) {
				if(this.selected().length > 0) {
					return this.selected()[0];
				}
				else {
					return blank;
				}
			}
			else {
				return blank;
			}
		}, vmQueries);

		vmQueries.clickViewResult = function(query) {
			var qid = query.activeId();

			var existTab;
			vmQueryPane.items().filter(function(t) {
				if(t.Logdb.activeId() == qid) {
					existTab = t;
				}
			});

			if(!!existTab) {
				// active tab
				vmQueryPane.select(existTab);
			}
			else {
				// new tab
				var lq = addTab(query);
				lq.vm.totalCount(query.totalCount())
			}

			hidePopover();
		}

		vmQueries.onSelect = function(data, e) {
			if(vmQueries.isEditMode()) {
				selectOnEditMode.call(this, data);
			}
			else {
				selectOnDefaultMode.call(this, data, e);
			}
		}

		vmQueries.onAfterRemove = function(item) {
			if(item.activeId() === -1) return;
			item.dispose(function() {
				Core.LogDB.remove(item);
			});
		}

		ko.applyBindings(vmQueries, $("#listQueriesBody")[0]);
	});

	$("#btnRemoveQueries").on("click", function() {
		$.each(vmQueries.selected(), function(i, q) {
			//vmQueries.remove(q);

			vmQueryPane.items().filter(function(t) {
				if(t.Logdb.activeId() == q.activeId()) {
					if(vmQueryPane.items().length == 1) {
						addTab();
					}
					vmQueryPane.remove(t);
				}
			});
		});
	})


	// Timeline extras
	function convertTimeline(body) {
		var tbegin = d3.time.format("%Y-%m-%d %X").parse(body.begin.substring(0, 19))
		//var tbegin = new Date(body.begin);
		//console.log(tbegin)
		
		var diff = 0, span = 0;
		if(body.span_field === "Minute") {
			diff = 1 * body.span_amount;
			span = 60000;
			tbegin = new Date(tbegin.getTime() - span)
		}

		var obj = {};
		body.values.forEach(function(v) {
			tbegin = new Date(tbegin.getTime() + diff * span)
			obj[tbegin.getISOTimeString()] = v;
		});

		return [obj];
	}

	// Layout

	if($.browser.msie && parseInt($.browser.version) < 10) {
		console.log("added flexie!")
		$('<link href="logquery9.css" rel="stylesheet"/>').appendTo("head");

		require(["/lib/flexie.amd.js"]);
	}
});
