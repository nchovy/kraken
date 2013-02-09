define(["/lib/knockout-2.1.0.debug.js", "/core/logdb.js"], function(ko, LogDB) {

var ViewModel = function() {
	var self = this;

	this.columns = ko.observableArray([
        /*{ headerText: "dst", rowText: "dst", clickHeader: null, sortedStatus: ""},
        { headerText: "sent", rowText: "sent", clickHeader: null, sortedStatus: ""},
        { headerText: "_id", rowText: "_id", clickHeader: null, sortedStatus: ""},
        { headerText: "facility", rowText: "facility", clickHeader: null, sortedStatus: ""},
        { headerText: "action", rowText: "action", clickHeader: null, sortedStatus: ""}*/
	]);

	var columnsText = function() {
		return $.map(self.columns(), function(obj) { return obj.rowText });
	}
	this.totalCount = ko.observable(0);

	this.rowCache = {

	};

	this.clear = function() {
		self.columns.removeAll();
		self.rowCache = {};
	}

	this.pushMany = function(result, offset) {
		var pageSize = result.length;
		var now = new Date();

		$.each(result, function(i, obj) {

			var columnOrder = [];
			for(prop in obj) {
				columnOrder.push(prop);
			}

			columnOrder.sort();
			
			$.each(columnOrder, function(j, col) {
				if(columnsText().indexOf(col) == -1) {
					var headerObj = {
						headerText: col,
						rowText: col,
						clickHeader: null,
						sortedStatus: ""
					};
					
					self.columns.push(headerObj);
				}
			});

			self.rowCache[offset + i] = obj;
		})

		//console.log(now, pageSize);
		//console.log(this.rowCache)
	}
}

return ViewModel;

});