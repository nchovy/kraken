require(["/lib/jquery.js",
	"/lib/knockout-2.1.0.debug.js",
	"/core/connection.js",
	"/component/util.js",
	"/component/logdb.querybar.js",
	"/component/spreadsheet.js",
	"/lib/knockout.mapping-latest.debug.js"
	],
	function(_$, ko, Socket, Util, QueryBar, Spreadsheet, komap) {

	var qbar = new QueryBar.instance();
	qbar.Logdb.pageLoaded = function(m) {
		var currIdx = grid1.getCurrentIndex();
		vm.pushMany(m.body.result, currIdx);
	}

	qbar.Logdb.onTimeline = function(m, self) {
		console.log("timeline:", m.body);

		if(!!m.body.count) {
			vm.totalCount(m.body.count)
		}
	}

	qbar.Logdb.created = function(query, id, self) {
		//console.log("create:", query)
	}

	qbar.onSearch = function(self, el) {
		vm.clear();
		grid1.clear();
	}


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

	/*
	$("#dododo").on("click", function() {
		vm.columns.push({ headerText: "asdf", rowText: "asdf", clickHeader: null, sortedStatus: ""})
		vm.columns.remove(vm.columns()[2]);
	})
	*/
});
