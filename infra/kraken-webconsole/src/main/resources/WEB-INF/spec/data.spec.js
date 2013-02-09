require(["/lib/knockout-2.1.0.debug.js", "/component/data.js"], function(ko, Data) {

module("kuro.data");

test("basic", function() {	
	equal(Data, Kuro.Data, "namespace registered");

	var vm1 = new Data.ViewModel([]);


});

test("add", function() {
	var vm1 = new Data.ViewModel([]);

	var obj1 = {
		"hello": "world"
	};

	var obj2 = {
		"annyeong": "sesang"
	};

	vm1.add(obj1);

	equal(vm1.items()[0], obj1, "add obj1 successfully");
	equal(vm1.items()[0].isSelected(), false, "isSelected prop attached in obj1");
	equal(vm1.length(), 1, "check length, length is 1");

	vm1.add(obj2);

	equal(vm1.items()[1], obj2, "add obj2 successfully");
	equal(vm1.length(), 2, "length is 2");
});

test("select", function() {
	var vm1 = new Data.ViewModel([]);

	var obj1 = {
		"hello": "world"
	};

	var obj2 = {
		"annyeong": "sesang"
	};

	vm1.add(obj1);
	vm1.add(obj2);

	vm1.select(obj1);

	equal(obj1.isSelected(), true, "obj1 selected");
	equal(vm1.selected()[0], obj1, "selected item is obj1");

	vm1.select(obj2);
	equal(obj2.isSelected(), true, "obj2 selected");
	equal(obj1.isSelected(), false, "obj1 unselected automatically");
	equal(vm1.selected().length, 1, "if ViewModel cannot multiple select, only one item in 'selected'");
	equal(vm1.selected()[0], obj2, "selected item is obj2");

	vm1.canSelectMulti(true);
	vm1.selectAt(0);
	equal(vm1.selected().length, 2, "ViewModel now can multiple select, and select obj1 using selectAt(0), then length of selected items is 2");
	equal(vm1.selected()[1], obj1, "lastest selected item is obj1");

});

test("selectAll", function() {
	var vm1 = new Data.ViewModel([]);

	var obj1 = {
		"hello": "world"
	};

	var obj2 = {
		"annyeong": "sesang"
	};

	vm1.add(obj1);
	vm1.add(obj2);

	vm1.selectAll(true);

	equal(vm1.selected().length, 1, "if ViewModel cannot multiple select, 1 lastest item in 'selected'");
	equal(obj1.isSelected(), false, "obj1 unselected");
	equal(obj2.isSelected(), true, "obj2 selected");

	vm1.selectAll(false);

	equal(vm1.selected().length, 0, "unselected all");

	vm1.canSelectMulti(true);
	vm1.selectAll(true);

	equal(vm1.selected().length, 2, "if ViewModel can multiple select, 2 items in 'selected'");
	equal(obj1.isSelected(), true, "obj1 selected");
	equal(obj2.isSelected(), true, "obj2 selected");

});

test("select event", function() {
	var vm1 = new Data.ViewModel([]);

	var obj1 = {
		"hello": "world"
	};

	var obj2 = {
		"annyeong": "sesang"
	};

	vm1.add(obj1);
	vm1.add(obj2);

	vm1.onSelect = function(item) {
		if(item == obj1) {
			equal(item, obj1, "event called: obj1 selected using selectAt(0)");
		}

		if(item == obj2) {
			equal(item, obj2, "event called: obj2 selected using select(obj2)");
		}
	}

	vm1.selectAt(0);
	vm1.select(obj2);

});

test("remove", function() {
	var vm1 = new Data.ViewModel([]);

	var obj1 = {
		"hello": "world"
	};

	var obj2 = {
		"annyeong": "sesang"
	};

	vm1.add(obj1);
	vm1.remove(obj1);

	equal(vm1.length(), 0, "add and remove in blank array, then array is blank now");

	vm1.add(obj1);
	vm1.add(obj2);

	equal(vm1.removeAt(0)[0], obj1, "removed obj1 using removeAt(0), then return value is obj1");
	equal(vm1.length(), 1, "removed obj1 using removeAt(0), then length is 1");
	equal(vm1.items()[0], obj2, "index of 0 is obj2, not obj1");

	equal(vm1.remove(obj1).length, 0, "no more obj1 in array, then return value is blank");

	equal(vm1.remove(obj2)[0], obj2, "removed obj2 using remove(obj2), then return value is obj2");
	equal(vm1.length(), 0, "removed obj2 using remove(obj2), then length is 0");
});

test("remove event", function() {
	var vm1 = new Data.ViewModel([]);

	var obj1 = {
		"hello": "world"
	};

	var obj2 = {
		"annyeong": "sesang"
	};

	var checkorder = [];

	vm1.onBeforeRemove = function(item) {
		if(item == obj1) {
			return false; // cannot remove obj1
		}
		
		if(item == obj2) {
			equal(item, obj2, "onBeforeRemove called when removing obj2");
			checkorder.push("onBeforeRemove");
			return true;
		}
	}

	vm1.onAfterRemove = function(item) {
		if(item == obj2) {
			equal(item, obj2, "onAfterRemove called when removing obj2");
			checkorder.push("onAfterRemove");
		}
	}

	vm1.add(obj1);
	vm1.add(obj2);

	equal(vm1.length(), 2, "obj1, obj2 added");

	equal(vm1.remove(obj1).length, 0, "if obj1 will be removed, return false at onBeforeRemove. then cannot remove obj1");
	equal(vm1.length(), 2, "array length still is 2");

	equal(vm1.remove(obj2)[0], obj2, "obj2 removed successfully");
	equal(checkorder[0], "onBeforeRemove", "onAfterRemove must be called after onBeforeRemove");
	equal(checkorder[1], "onAfterRemove", "onAfterRemove must be called after onBeforeRemove");
	equal(vm1.length(), 1, "array length is 1");

	vm1.onBeforeRemove = null;
	equal(vm1.remove(obj1)[0], obj1, "onBeforeRemove is null, then obj1 removed successfully");
})

test("remove selected item", function() {
	var vm1 = new Data.ViewModel([]);

	var obj1 = {
		"hello": "world"
	};

	vm1.add(obj1);

	vm1.select(obj1);
	equal(vm1.selected()[0], obj1, "obj1 in 'selected' array");
	vm1.remove(obj1);
	equal(vm1.selected().length, 0, "obj1 removed, then 'selected' array is blank");
	
});


});