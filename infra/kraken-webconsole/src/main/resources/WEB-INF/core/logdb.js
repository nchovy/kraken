define(["/core/Connection.js", "/component/util.js", "/core/logdb.viewmodel.js"], function(socket, Util, ViewModel) {

// class
var Logdb = function() {
	var isDebug = false;

	if(!console.group) {
		console.group = function() {}
	}
	if(!console.groupEnd) {
		console.groupEnd = function() {}
	}

	var activeQuery, activePage = 1, pagesize = 15;
	var activeId;
	var that = this;

	var eventObj = {};
	var callback = {
		created: function(query, query_id) {
			if(isDebug) {
				console.group("created");
			}

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

			var query_id = m.body.id;
			var request_count = 0;

			function checkRegister() {
				request_count++;
				if(request_count === 2) {
					afterCreateQuery(querystr, query_id);
				}
			}

			socket.registerTrap("logstorage-query-" + query_id, onTrap, checkRegister);
			socket.registerTrap("logstorage-query-timeline-" + query_id, onTimeline, checkRegister);
			//socket.debugTrap();
		})
	}

	function afterCreateQuery(query, queryId) {

		callback.created(query, queryId);

		clearQuery({
			except: [queryId],
			callback: function() {
				activeQuery = query;
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
					console.log("cannot start query " + activeQuery);
					clearQuery();
					return;
				}

				callback.starting(queryId);
				
				activeId = queryId;
				//console.log(activeId);

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
		if(id != activeId) {
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
		getResult(activeId, page * pagesize);
	}

	this.stop = function(callback) {
		if(isDebug) {
			console.log("stop");
		}

		socket.send("org.krakenapps.logdb.msgbus.LogQueryPlugin.stopQuery", { "id": activeId }, function(m) {
			console.log(m)
			if(!!callback) {
				callback();
			}
		});
	}


	this.dispose = function(callback) {
		this.stop(function() {
			if(isDebug) {
				console.log("dispose")
			}

			socket.send("org.krakenapps.logdb.msgbus.LogQueryPlugin.removeQuery", { "id": activeId }, function(m) {
				if(!!callback) {
					callback();
				}
			});
		})
	}

	this.getResult = getResult;

	this.getFieldInfo = function() {

	}

	this.getId = function() {
		return activeId;
	}

	this.getActiveQuery = function() {
		return activeQuery;
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
	var queries = [];

	function getQueries() {
		return queries;
	}

	function create() {
		var instance = new Logdb();
		queries.push(instance);
		return instance;
	}

	return {
		create: create,
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