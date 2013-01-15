require(["/lib/jquery.js",
	"/lib/jquery.svganim-amd.js",
	"/lib/knockout-2.1.0.debug.js",
	"/core/connection.js",
	"/component/util.js",
	"/component/logdb.querybar.js",
	"/component/spreadsheet.js",
	"/component/chart.column.js",
	"/lib/knockout.mapping-latest.debug.js"
	],
	function(_$, _$svganim, ko, Socket, Util, QueryBar, Spreadsheet, Column, komap) {

	var qbar = new QueryBar.instance();

	qbar.Logdb.on("pageLoaded", function(m) {
		var currIdx = grid1.getCurrentIndex();
		vm.pushMany(m.body.result, currIdx);
	});

	qbar.Logdb.on("onTimeline", function(m) {
		console.log("timeline:", m.body);


		if(!!m.body.count) {
			vm.totalCount(m.body.count)
		}

		var tobj = convertTimeline(m.body);
		for(var i = 0; i < tobj.length; i++) {
			tvm.items()[i].value(tobj[i].value())
			tvm.items()[i].bar.label(tobj[i].name)
		}
		
		/*
		tvm.items.removeAll();

		$.each(tobj, function(i, obj){
			tvm.add(obj);
		})
		*/
		
	});;

	qbar.Logdb.on("loaded", function(m) {

	})

	qbar.onSearch = function(self, el) {
		vm.clear();
		grid1.clear();
	}


	qbar.nowQuerying.subscribe(function(val) {
		if(val) {
			$(".search-query").removeClass("complete").addClass("querying");
		}
		else {
			$(".search-query").removeClass("querying").addClass("complete");
		}
	})


	var vm = new QueryBar.ViewModel();


	ko.applyBindings(qbar, document.getElementById("qbar"));


	var grid1 = new Spreadsheet("#qresult", vm, {
		"debug": false,
		"colDataBind": function(rowidx, colidx, prop) {
			return 'text: rowCache[' + rowidx + ']["' + prop + '"]';
		},
		"onRenderRow": function(idx, el) {
			
		},
		"onRender": function() {

		},
		"onRenderRows": Util.throttle(function(top, bottom, el, handler) {
			qbar.Logdb.getResult(qbar.Logdb.getId(), top, bottom - top, function() {
				console.log("after getResult")
				try {
					ko.applyBindings(vm, el.find("tbody")[0]);
				}
				catch(e) {
					console.log(e)
				}

				handler.done();
			});
		}, 200)
	});

	var samplemsg = 
        {
            "id": 6,
            "values": [
                5,
                10,
                20,
                40,
                6,
                80,
                0,
                30,
                60,
                100
            ],
            "count": 100,
            "span_field": "Minute",
            "type": "eof",
            "span_amount": 1,
            "begin": "2012-06-24 23:49:00+0900"
        };

	function convertTimeline(body) {
		var tbegin = new Date(body.begin);
		
		var diff = 0, span = 0;
		if(body.span_field === "Minute") {
			diff = 1 * body.span_amount;
			span = 60000;
			tbegin = new Date(tbegin.getTime() - span)
		}

		return body.values.map(function(v) {
			tbegin = new Date(tbegin.getTime() + diff * span)
			//console.log(tbegin.getISOTimeString())
			return {
				"value": ko.observable(v),
				"name": tbegin.getISOTimeString()
			}
		});
	}

	var mm = convertTimeline(samplemsg);
	
	var tvm = new Column.ViewModel(mm, {
		"valueKey": "value",
		"labelKey": "name"
	});


	tvm.addRefLineY = function(el) {
		if(el.nodeType == 1) {
			//console.log(el)
			var y;
			if(el.nodeName === "line") {
				y = $(el).attr("y1");
				$(el).attr("y1", y - 30);
			}
			else if(el.nodeName === "text") {
				y = $(el).attr("y");
				$(el).attr("y", y - 30);
			}
			
			$(el).css("opacity", "0").animate({
				opacity: 1,
				svgY: y,
				svgY1: y,
				svgY2: y
			}, 500);	
		}
	}

	tvm.removeRefLineY = function(el) {
		if(el.nodeType == 1) {
			//console.log(el)


			$(el).animate({
				opacity: 0,
			}, 200, function() {
				$(el).remove();
			})

		}	
	}

	tvm.addItem = function(el) {
		if(el.nodeType == 1) {
			if(el.nodeName === "rect") {
				$(el).css("opacity", "0").animate({
					opacity: 1
				}, 500);
			}
		}
	}


	ko.applyBindings(tvm, document.getElementById("qtimeline"))
	
	
	$("#dododo").on("click", function() {
		max = max * 1.5;
		var r = Math.floor(Math.random() * max);
		console.log(tvm.items()[1].value(), "=>", r);

		tvm.items()[1].value(r)

		console.log("maxValue", tvm.getMaxValue())
	});

	var max = 100;

	$("#dododo2").on("click", function() {
		/*
		tvm.refLineY.push({
				"value": 1,
				"scaled": 10
			})
		*/
		max = max * 1.5;
		var r = Math.floor(Math.random() * max);
		//console.log(r)
		
		tvm.add({
				"value": ko.observable(r),
				"name": "2012-06-2g"
			})
	})
});
