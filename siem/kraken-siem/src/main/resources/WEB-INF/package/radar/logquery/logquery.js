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
	"/lib/jquery.timeago.js",
	"/component/util.js",
	"/package/system/orgchart/testdata2.js"
	],
	function(_$, _$svganim, ko, Socket, Util, QueryBar, Spreadsheet, StackedBar, komap, Win, DD, List, Grid, Bootstrap, LogQuery, Timeago, Util, json2) {

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
	var label1 = $('<small>').appendTo("#timeline-chart");

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
	}

	var category20 = d3.scale.category20();
	
	function addTab(query) {
		var lq;
		if(!!query) {
			lq = new LogQuery.instance({
				"query": query
			});
		}
		else {
			lq = new LogQuery.instance({
				logdbOption: {
					callback: function(query) {
						// additional property
						query.color = ko.computed(coloringQuery, query)
						query.statusText = ko.computed(getQueryStatus, query);
					}
				}
			});
		}
		var len = vmQueryPane.length();
		LogQuery.init(lq, category20(len));

		lq.Logdb.on("onTimeline", function(m) {
			onTimeline(m, lq);
		});

		vmQueryPane.add(lq);
		vmQueryPane.select(lq);

		return lq;
	}

	function onTimeline(m, lq) {
		lq.timeline = convertTimeline(m.body);
		lq.totalCount = m.body.count;
		lq.rawBody = m.body;

		if(lq.isSelected()) {
			$("#divTotalCount").text(lq.totalCount + " results");
			bar.updateData(lq.timeline);

			label1.text(lq.rawBody.begin.split(" ")[0])
		}
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
		hidePopover();
		$(".mode-edit").addClass("roll").fadeIn('fast');
		$(".mode-default").addClass("up").fadeOut('fast');

		vmQueries.isEditMode(true);
		vmQueries.selectAll(false);
	});

	function toggleDefault() {
		$(".mode-edit").addClass("up").fadeOut('fast');
		$(".mode-default").addClass("roll").fadeIn('fast');

		vmQueries.isEditMode(false);
		vmQueries.selectAll(false);
	}

	$(".toggle-default").on("click", toggleDefault);

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

		function getPos(x, boxwidth) {
			if($(window).width() - (boxwidth / 2) < x) {
				return x - boxwidth;
			}
			else if(x < (boxwidth / 2)) {
				return 20;
			}
			else {
				return x - (boxwidth / 2);
			}
		}

		var popover = $(vmQueries.el).find(".popover");

		popover.addClass("in")
			.css("top", (offset.top - 140) + "px")
			.css("left", getPos(e.pageX, popover.width()) + "px")
			.show();

		$("#box-query *").on("click", hidePopover);
	}

	function coloringQuery() {
		var self = this;
		var hasMatch = false;
		var color;
		vmQueryPane.items().filter(function(t) {
			if(t.Logdb.activeId() == self.activeId()) {
				hasMatch = true;
				color = t.color;
			}
		});

		if(hasMatch) {
			return color;
		}
		else {
			return null;
		}
	}

	function getQueryStatus() {
		if(this.isEnd()) {
			return "End";
		}
		else {
			return "Running";
		}
	}

	// Running Queries
	Core.LogDB.getQueries(function(queries) {
		vmQueries = queries;

		$.each(vmQueries.items(), function(i, q) {
			q.color = ko.computed(coloringQuery, q);
			q.statusText = ko.computed(getQueryStatus, q);
		})

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
				console.log(query)
				// new tab
				if(!query.isEnd()) {
					query.registerTrap();
				}
				var lq = addTab(query);
				lq.vm.totalCount(query.totalCount());
				
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

		vmQueries.onAfterRemove = function(query) {
			if(query.activeId() === -1) return;
			query.dispose();
		}

		vmQueries.closePopover = function() {
			hidePopover();
		}

		ko.applyBindings(vmQueries, $("#listQueriesBody")[0]);
		$(".timeago").timeago();
	});

	$("#btnRemoveQueries").on("click", function() {
		var willremoved = [];
		$.each(vmQueries.selected(), function(i, q) {
			var activeId = q.activeId();
			willremoved.push(q);

			vmQueryPane.items().filter(function(t) {
				if(t.Logdb.activeId() == activeId) {
					if(vmQueryPane.items().length == 1) {
						addTab();
					}
					vmQueryPane.remove(t);
				}
			});
		});

		$.each(willremoved, function(i, q) {
			vmQueries.remove(q);
		});

		$(this).text("Remove");
		toggleDefault();
	});


	// Timeline extras
	function convertTimeline(body) {
		var tbegin = d3.time.format("%Y-%m-%d %X").parse(body.begin.substring(0, 19))
		//var tbegin = new Date(body.begin);
		//console.log(tbegin)
		
		var diff = 1 * body.span_amount;
		var span = 0;
		if(body.span_field === "Minute") {
			span = 60000;
		}
		else if(body.span_field === "Hour") {
			span = 3600000;
		}
		else if(body.span_field === "Day") {
			span = 86400000;
		}
		tbegin = new Date(tbegin.getTime() - span);

		var obj = {};
		body.values.forEach(function(v, i) {
			tbegin = new Date(tbegin.getTime() + diff * span)
			if(body.span_field === "Day") {
				obj[tbegin.getISODateOnlyString()] = v;
			}
			else {
				obj[tbegin.getISOTimeString()] = v;
			}

			if(i == 9) {
				tbegin = new Date(tbegin.getTime() + diff * span)
				if(body.span_field === "Day") {
					obj[tbegin.getISODateOnlyString()] = 0;
				}
				else {
					obj[tbegin.getISOTimeString()] = 0;
				}
				
			}
		});

		console.log(obj)
		return [obj];
	}

	// Layout
	$("#closeQueryStatus").on("click", function() {
		$("#box-query-status").hide();
	})


	if($.browser.msie && parseInt($.browser.version) < 10) {
		console.log("added flexie!")
		$('<link href="logquery9.css" rel="stylesheet"/>').appendTo("head");

		require(["/lib/flexie.amd.js"]);
	}
});
