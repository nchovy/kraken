define(["/core/Connection.js", "/component/util.js", "/lib/knockout-2.1.0.debug.js", "/core/logdb.viewmodel.js", "/component/list.js"], function(socket, Util, ko, ViewModel, List) {

// class
var Query = function(jobj) {

	var isDebug = true;

	if(!console.group) {
		console.group = function() {}
	}
	if(!console.groupEnd) {
		console.groupEnd = function() {}
	}

	var query_string, id, is_end, total_count, last_started;
	if(!!jobj) {
		query_string = jobj.query_string;
		id = jobj.id;
		is_end = jobj.is_end;
		total_count = jobj.commands[jobj.commands.length - 1].push_count;
		last_started = jobj.last_started;
	}

	this.activeQuery = ko.observable(query_string);
	this.activeId = ko.observable(id || -1);
	this.isEnd = ko.observable(is_end || false);
	this.totalCount = ko.observable(total_count || 0);
	this.lastStarted = last_started;

	var activePage = 1, pagesize = 15;
	var that = this;

	var eventObj = {};
	var callback = {
		created: function(query, query_id) {
			if(isDebug) {
				console.group("created");
			}

			that.isEnd(false);

			if(!!eventObj.created) {
				$.each(eventObj.created, function(i, fn) { 
					fn.call(that, query, query_id);
				});
			}

			if(isDebug) {
				console.groupEnd();
			}
		},
		starting: function(query_id) {
			if(isDebug) {
				console.group("stating");
				console.groupEnd();
			}
		},
		pageLoading: function(m) {
			if(isDebug) {
				console.group("pageLoading");
				console.groupEnd();
			}
		},
		pageLoaded: function(m) {
			if(isDebug) {
				console.group("pageLoaded");
			}

			if(!!eventObj.pageLoaded) {
				$.each(eventObj.pageLoaded, function(i, fn) { 
					fn.call(that, m);
				});
			}

			if(isDebug) {
				console.groupEnd();
			}
		},
		loaded: function(m) {
			if(isDebug) {
				console.group("loaded");
				console.log(m);
			}

			that.isEnd(true);

			if(!!eventObj.loaded) {
				$.each(eventObj.loaded, function(i, fn) { 
					fn.call(that, m);
				});
			}

			if(isDebug) {
				console.groupEnd();
			}
		},
		onTimeline: function(m) {
			if(isDebug) {
				console.group("onTimeline");
			}
			
			if(!!eventObj.onTimeline) {
				$.each(eventObj.onTimeline, function(i, fn) { 
					fn.call(that, m);
				});
			}

			if(isDebug) {
				console.groupEnd();
			}
		}
	}

	function createQuery(querystr) {
		socket.send("org.krakenapps.logdb.msgbus.LogQueryPlugin.createQuery", { "query": querystr }, function(m) {

			if(m.isError) {
				console.log("cannot create query")
				clearQuery();
				return;
			}

			var queryId = m.body.id;
			that.activeId(queryId);
			that.activeQuery(querystr);

			that.registerTrap();
		})
	}

	this.registerTrap = function() {

		var request_count = 0;

		function checkRegister() {
			request_count++;
			if(request_count === 2) {
				afterCreateQuery(that.activeQuery(), that.activeId());
			}
		}

		socket.registerTrap("logstorage-query-" + that.activeId(), onTrap, checkRegister);
		socket.registerTrap("logstorage-query-timeline-" + that.activeId(), onTimeline, checkRegister);
	}

	function afterCreateQuery(query, queryId) {

		callback.created(query, queryId);

		clearQuery({
			except: [queryId],
			callback: function() {
				startQuery(queryId);
			}
		});
	}


	function startQuery(queryId) {
		socket.send("org.krakenapps.logdb.msgbus.LogQueryPlugin.startQuery",
			{
				id: queryId,
				offset: 0,
				limit: pagesize,
				timeline_limit: 10
			},
			function(m) {
				if(m.isError) {
					console.log("cannot start query " + that.activeQuery());
					clearQuery();
					return;
				}

				callback.starting(queryId);
			}
		);
	}

	function clearQuery(options) {
		if(options == null) return;
		var defaultOptions = {
			except: [],
			contains: null,
			callback: null
		};

		options = $.extend(defaultOptions, options);

		options.callback();
	}

	function getResult(id, offset, limit, trigger) {
		socket.send("org.krakenapps.logdb.msgbus.LogQueryPlugin.getResult",
			{
				id: id,
				offset: offset,
				limit: ((limit == undefined) ? pagesize : limit),
			},
			function(m) {
				callback.pageLoading(m);

				if(m.isError) {
					clearQuery();
					return;
				}

				callback.pageLoaded(m);
				if(!!trigger) {
					trigger();
				}

			}
		);
	}

	function onTrap(resp) {
		if(isDebug) {
			console.group("onTrap");
		}
		var m = resp[0];

		callback.pageLoading(m);

		if(m.isError) {
			clearQuery();
			return;
		}

		var id = m.body.id;
		if(id != that.activeId()) {
			console.log("not same: " + id);
			return;	
		}

		if(m.body.type == "page_loaded") {
			callback.pageLoaded(m);
		}
		else if(m.body.type == "eof") {
			//console.log("eof unregistered")
			socket.unregisterTrap("logstorage-query-" + id, onTrap);

			setTimeout(function() {
				socket.unregisterTrap("logstorage-query-timeline-" + id, onTimeline);
			}, 5000)

			if(m.body.total_count < pagesize) {
				getResult(id, 0)
			}

			that.totalCount(m.body.total_count);

			callback.loaded(m);
		}
		else {
			console.log("error")
			console.log(resp);
		}
		if(isDebug) {
			console.groupEnd();
		}
	}

	function onTimeline(resp) {
		//console.log(resp)
		var m = resp[0];

		if(m.isError) {
			console.log("error")
			console.log(resp);
			return;
		}

		if(!!m.body.span_amount && !!m.body.span_field) {
			callback.onTimeline(resp[0]);
		}
		else {
			console.group("onTimeline");
			console.trace();
			console.log("this is not timeline");
			console.groupEnd();
		}

	}


	this.search = function(query, pageSize) {
		if(isDebug) {
			console.log("search\t" + query);
		}
		pagesize = pageSize;
		createQuery(query);
	}

	this.next = function() {

	}

	this.goPage = function(page) {
		activePage = page + 1;
		getResult(that.activeId(), page * pagesize);
	}

	this.stop = function(callback) {
		if(isDebug) {
			console.log("stop");
		}

		socket.send("org.krakenapps.logdb.msgbus.LogQueryPlugin.stopQuery", { "id": that.activeId() }, function(m) {
			if(!!callback) {
				callback();
			}
		});
	}

	function removeQuery(item, callback) {
		socket.send("org.krakenapps.logdb.msgbus.LogQueryPlugin.removeQuery", { "id": item.activeId() }, function(m) {
			if(!!callback) {
				callback();
			}
		});
	}


	this.dispose = function(callback) {
		if(isDebug) {
			console.log("dispose")
		}

		if(that.isEnd()) {
			removeQuery(that, callback);
		}
		else {
			that.stop(function() {
				removeQuery(that, callback);
			})
		}
	}

	this.getResult = getResult;

	this.getFieldInfo = function() {

	}

	this.debug = {
		on: function() {
			isDebug = true;
		},
		off: function() {
			isDebug = false;
		}
	}

	this.on = function(eventName, fn) {
		if(!eventObj[eventName]) {
			eventObj[eventName] = [];
		}
		eventObj[eventName].push(fn);
	}

}

var logdbManager = (function() {
	//var queries = ko.observableArray([]);
	var queries = new List.ViewModel([]);

	function getQueries(callback) {
		socket.send("org.krakenapps.logdb.msgbus.LogQueryPlugin.queries", {}, function(m) {
			if(m.isError) {
				console.log("cannot load queries")
				clearQuery();
				return;
			}

			$.each(m.body.queries, function(i, jobj) {
				var isExist = false;
				$.each(queries.items(), function(j, qobj) {
					if(qobj.activeId() == jobj.id) isExist = true;
				});

				if(!isExist) {
					var query = new Query(jobj);
					queries.add(query);
				}
			});

			if(callback) {
				callback(queries);
			}
		});
	}

	function create(option) {
		var instance = new Query();
		if(!!option.callback) {
			option.callback(instance);
		}

		queries.add(instance);
		console.log(instance)
		return instance;
	}

	function remove(query) {
		if(query.activeId() === -1) return;
		query.dispose();
		return queries.remove(query);
	}

	return {
		create: create,
		remove: remove,
		getQueries: getQueries,
		ViewModel: ViewModel
	}

})();


	var Core = parent.Core;
	if(!Core) {
		Core = parent.Core = {};
	}

	if(!Core.LogDB) {
		console.log("register LogDB manager globally");
		parent.Core.LogDB = logdbManager;
	}

	return Core.LogDB;

});