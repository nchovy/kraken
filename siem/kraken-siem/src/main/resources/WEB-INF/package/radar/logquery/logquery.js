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

	var vmQueryPane = new List.ViewModel([]);
	
	function addTab() {
		var lq = new LogQuery.instance();
		LogQuery.init(lq);

		vmQueryPane.add(lq);
		vmQueryPane.select(lq);
	}

	ko.applyBindings(vmQueryPane, document.getElementById("tabs"))
	ko.applyBindings(vmQueryPane, document.getElementById("box-query"));

	$("#btnAddTab").on("click", function(e) {
		addTab();
	});
	addTab();


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

	function selectOnDefaultMode(data, e) {
		var offset = $(vmQueries.el).find("li.active").offset();

		var popover = $(vmQueries.el).find(".popover")
			.addClass("in")
			.css("top", (offset.top - 140) + "px")
			.css("left", e.pageX + "px")
			.show();

		function hidePopover(e) {
			e.stopPropagation();
			popover.hide();
			$("#box-query *").off("click", hidePopover);
		}

		$("#box-query *").on("click", hidePopover);
	}

	// Running Queries
	Core.LogDB.getQueries(function(queries) {
		console.log(queries());

		vmQueries = new List.ViewModel(queries());

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

		vmQueries.onSelect = function(data, e) {
			if(vmQueries.isEditMode()) {
				selectOnEditMode.call(this, data);
			}
			else {
				console.log(vmQueries.selected.length)
				selectOnDefaultMode.call(this, data, e);
			}
		}

		vmQueries.onAfterRemove = function(item) {
			item.dispose(function() {
				Core.LogDB.remove(item);
			});
		}

		ko.applyBindings(vmQueries, $("#listQueriesBody")[0]);
	});


	$("#btnRemoveQueries").on("click", function() {
		$.each(vmQueries.selected(), function(i, q) {
			vmQueries.remove(q);
		});
	})


	// Timeline

	var samplemsg = 
	{
		"id": 6,
		"values": [1,1,1,1,1,1,1,1,1,1],
		//"values": [5,10,20,40,6,80,0,30,60,100],
		"count": 100,
		"span_field": "Minute",
		"type": "eof",
		"span_amount": 1,
		"begin": "2012-06-24 23:49:00+0900"
	};


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

	var z = convertTimeline(samplemsg);
	var bar = new StackedBar.ViewModel("#timeline-chart", z);

	$("#btnTimelineZoom").on("click", function() {
		var newmsg = 
		{
			"id": 6,
			//"values": [0,0,0,0,0,0,0,0,0,0],
			"values": [5,4,21,4,12,23,2,11,13,17],
			"count": 100,
			"span_field": "Minute",
			"type": "eof",
			"span_amount": 1,
			"begin": "2012-06-24 11:12:00+0900"
		};
		
		bar.updateData( convertTimeline( newmsg ) );
	})

	//console.log( d3.time.format("%Y-%m-%d %X").parse("2012-06-24 11:12:00"))

	// Layout

	if($.browser.msie && parseInt($.browser.version) < 10) {
		console.log("added flexie!")
		$('<link href="logquery9.css" rel="stylesheet"/>').appendTo("head");

		require(["/lib/flexie.amd.js"]);
	}
});
