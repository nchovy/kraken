require(["/lib/jquery.js",
	"/lib/knockout-2.1.0.debug.js",
	"/lib/knockout.mapping-latest.debug.js",
	"/core/connection.js",
	"/component/form.js", 
	"/component/tree.js",
	"/component/data.grid.js",
	"testdata2.js",
	"/component/window.js",
	"/component/spreadsheet.js",
	"/component/dd.js"],
	function(_$, ko, komap, Socket, Form, Tree, Grid, json2, Win, Spreadsheet, DD) {


	var vmList;
	
	Socket.send("org.krakenapps.dom.msgbus.AreaPlugin.getRootAreas", {}, function(message) {
		console.log(message)

		if(message.isError) {
			alert("Failed to load Areas.");
			return;
		}

		vmList = new Tree.ViewModel(message.body.areas);
		ko.applyBindings(vmList, document.getElementById("treeview"));
	});

	$("#btnOpenModalAdd").on("click", function() {
		Win.open("#modalNewArea");
	});


	$("#btnAddNewArea").on("click", function(e) {
		
		e.preventDefault();
		
		var newarea = Form.serialize("#formArea");
		//newarea.parent = parentGuid;

		Socket.send("org.krakenapps.dom.msgbus.AreaPlugin.createArea", newarea, function(message) {
			console.log(message)
			
			newarea.guid = message.body.guid;
			//vmList.add(newarea, parentGuid);
			vmList.add(newarea);

			Form.clear("#formArea");
			Win.close("#modalNewArea");
		});
	});

	$("#btnRemoveArea").on("click", function() {
		var areaWillRemove = vmList.selected;

		Socket.send("org.krakenapps.dom.msgbus.AreaPlugin.removeArea", {
			"guid": areaWillRemove.guid
		}, function(message) {

			vmList.remove(areaWillRemove);
		});
	});




	ko.mapping = komap;
	/*

	function treeacceptable() {

		var inst = new DD.Droppable("#treeview li a", {
			"accept": ".ghostrow",
			"activeClass": "acceptable",
			"onDrop": function(draggable) {
				console.log(draggable);
			}
		})
	}
	*/

	$.each(json2.areas, function(i, obj) {
		obj.id = i;
	});

	var datavm = ko.mapping.fromJS(json2.areas);

	var vm = new Grid.viewModel({
		data: datavm,
		columns: [
			{ headerText: "v", rowText: "isChecked", clickHeader: null, sortedStatus: "", formatter: $("#isChecked")},
			{ headerText: "vcc", rowText: "isChecked", clickHeader: null, sortedStatus: ""},
			{ headerText: "선택여부", rowText: "isSelected", clickHeader: null, sortedStatus: ""},
			{ headerText: "이름", rowText: "name", clickHeader: null, sortedStatus: ""},
			{ headerText: "설명", rowText: "description", clickHeader: null, sortedStatus: ""},
			{ headerText: "생성일", rowText: "created", clickHeader: null, sortedStatus: ""}
		],
        pageSize: 20,
        isCheckable: true,
        isSelectable: true,
        totalCount: 1000000,
        showPager: false
	});

	vm.getRangeValue = function(range) {
		var str = "";
		console.log(range)

		//return; ///////////// uncomment this
		var cols = $.map(this.columns, function(obj, i) {
			if(i < Math.min(range.from[1], range.to[1]) || i > Math.max(range.from[1], range.to[1])) {
				return null;
			}
			else {
				return obj.rowText;
			}
		});

		var yidxfrom = range.to[0] > range.from[0] ? range.from[0] : range.to[0];
		var yidxto   = range.to[0] > range.from[0] ? range.to[0] : range.from[0];

		var data = this.data();
		for (var i = yidxfrom; i <= yidxto; i++) {
			for (var j = 0; j < cols.length; j++) {

				// temporary
				var k = i % data.length;
				//console.log(data[k].name())

				str = str + data[k][cols[j]]() + "\t";
				//str = str + data[i][cols[j]]() + "\t";
			}
			str = str + "\n";
		}

		$("textarea.ghost").val(str).select();
	}

	console.log(vm);


	function afterworks() {
		console.log(vm.filteredItem()());
		
		var grid1 = new Spreadsheet("#grid1", vm, {
			"debug": false,
			"colDataBind": function(rowidx, colidx, prop) {
				return 'text: filteredItem()()[' + (rowidx % 20) + '].' + prop + '()';
			},
			"onRenderRow": function(idx, el) {
				ko.applyBindings(vm, el[0]);
			},
			"onRenderRows": function(top, bottom) {

			},
			"onScroll": function(ratio) {
				//console.log("onScroll");
			},
			"onSelect": function(range) {
				//vm.getRangeValue(range);
			},
			"onSelectEnd": function(range) {
				vm.getRangeValue(range);
			}

		});

	}

	afterworks();


	$("#btnAdd").on("click", function() {
		Win.open("#modalNewUser");
		$("#txtlogin").focus();
	});

	$("#btnCloseAdd").on("click", function(e) {
		e.preventDefault();
		e.stopPropagation();
		Win.close("#modalNewUser");

	});


	$("#btnRefresh").on("click", function(e) {
		var itv = 300;
		$(".ani2").animate({
			left: "-25%"
		}, itv);

		$(".ani1").animate({
			left: "0%"
		}, itv);

		$(".ani3").animate({
			right: "0%"
		}, itv);
	})

	$("#btnBack").on("click", function(e) {
		var itv = 300;
		$(".ani2").animate({
			left: "0%"
		}, itv);

		$(".ani1").animate({
			left: "25%"
		}, itv);

		$(".ani3").animate({
			right: "-60%"
		}, itv);
	})

	parent.window.gridvm = vm;


});