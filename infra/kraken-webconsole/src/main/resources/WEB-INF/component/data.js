define(["/lib/knockout-2.1.0.debug.js", '/component/kuro.js'], function(ko, $K) {

var Data = $K.namespace("Data");

function bindEvent(collection) {
	$.each(collection, function(i, obj) {
		obj.isSelected = ko.observable(false);
	});
}

Data.ViewModel = function (data, option) {
	var self = this;
	this.self = this;

	this.items = (typeof data === 'function') ? data : ko.observableArray(data);
	this.length = ko.computed(function() {
		return self.items().length;
	});

	this.canSelectMulti = ko.observable(false);
	this.selected = ko.observableArray([]);

	function observeValue(collection) {
		if(!!option) {
			if(!!option.observeKeys) {

				$.each(collection, function(i, obj) {
					$.each(option.observeKeys, function(j, key) {
						var val = obj[key];

						obj[key] = ko.observable(obj[key]);
					});
				});

			}
		}
	}

	bindEvent(this.items());
	observeValue(this.items());
}

function setPublicMethod(self) {

	self.add = function(item) {
		bindEvent([item]);
		this.items.push(item);
	}

	self.insert = function(item, index) {
		bindEvent([item]);
		this.items.splice(index, 0, item);
	}

	self.remove = function(item) {
		var handled = false;

		if(!!this.onBeforeRemove) {
			var ret = this.onBeforeRemove(item);
			if(ret === false) handled = true;
		}

		var _return = [];
		if(!handled) {
			_return = this.items.remove(item);
			this.selected.remove(item);

			if(!!this.onAfterRemove) {
				this.onAfterRemove(item);
			}
		}

		return _return;
	}
	
	self.removeAt = function(index) {
		var handled = false;

		var item = this.items()[index];

		if(!!this.onBeforeRemove) {
			var ret = this.onBeforeRemove(item);
			if(ret === false) handled = true;
		}

		var _return = [];
		if(!handled) {
			_return = this.items.remove(item);
			this.selected.remove(item);

			if(!!this.onAfterRemove) {
				this.onAfterRemove(item);
			}
		}

		return _return;
	}

	self.select = function(item, e) {
		if(this.canSelectMulti() === undefined) { 
			this.canSelectMulti(false)
		}

		if(!this.canSelectMulti()) {
			if(this.selected().length > 0) {
				this.selected()[0].isSelected(false);
			}
			
			this.selected.removeAll();

			item.isSelected(true);
			this.selected.push(item);
		}
		else {
			item.isSelected(!item.isSelected());

			if(item.isSelected()) {
				this.selected.push(item);
			}
			else {
				this.selected.remove(item);
			}
		}

		if(!!this.onSelect) {
			if(e) {
				this.onSelect(item, e);
			}
			else {
				this.onSelect(item);
			}
		}
	}

	self.selectAll = function(toggle) {
		var self = this;
		if(toggle) {
			$.each(this.items(), function(i, obj) {
				self.select(obj);
			});
		}
		else {
			$.each(this.items(), function(i, obj) {
				obj.isSelected(false);
				self.selected.removeAll();
			});
		}
	}

	self.selectAt = function(idx) {
		if(idx < 0 || idx >= this.items().length) {
			console.log("out of index: " + idx)
			return;
		}
		this.select(this.items()[idx]);
	}


}

setPublicMethod(Data.ViewModel.prototype);

return Data;

});