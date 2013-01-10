define([], function() {



function msgobj(single) {
	//console.log(single);
	var response = { 
		body: single[1],
		isError: (single[0].errorCode) ? true : false
	};
	return response;
}

function guidGenerator() {
	var s4 = function() {
		return (((1+Math.random())*0x10000)|0).toString(16).substring(1);
	};
	return (s4()+s4()+"-"+s4()+"-"+s4()+"-"+s4()+"-"+s4()+s4()+s4());
}

function send(method, options, callback, request_option) {

	var defaultoptions = {};
	options = $.extend(defaultoptions, options);

	var request = [
		{
			"guid": guidGenerator(),
			"type": "Request",
			"source": (request_option) ? request_option.source : "",
			"target": "",
			"method": method
		}, options
	];

	$.post("/msgbus/request", JSON.stringify(request), function(raw, status, jqxhr) {
		var full;
		if($.browser.mozilla) {
			full = JSON.parse(jqxhr.responseText);
		}
		else {
			full = JSON.parse(raw);
		}
		
		var response = msgobj(full);

		callback(response, full);
	});
}

function Trap() {
	var isDebug = false;
	var map = {};
	var nowPolling = false;

	var doPoll = function() {
		nowPolling = true;

		if(isDebug) {
			console.log((new Date()).getISODateString() + " doPoll");
		}

		$.ajax({
			url: "/msgbus/trap",
			success: function(resp, status, jqxhr) {
				var full;
				if($.browser.mozilla) {
					full = JSON.parse(jqxhr.responseText);
				}
				else {
					full = JSON.parse(resp);
				}
				//console.log(full)

				$.each(full, function(i, obj) {
					var response = msgobj(obj);
					var method_name = obj[0].method

					if(!map.hasOwnProperty(method_name)) {
						//console.log(method_name + " passed!!!");
						return;
					}
					else {
						//console.log(method_name + " dododo!!!");
						map[method_name]([response], resp, status, jqxhr);
					}

					//console.log(response)
					//if(name === response.method) {
					//callback(arr, resp, status, jqxhr);
					//}
				});
				
			},
			timeout: 30000,
			complete: function() {
				doPoll();
			}
		});
	}

	this.register = function(name, ontrap, callback) {
		if(name == null) return;


		if(isDebug) {
			console.log("register " + name);
		}


		send("org.krakenapps.msgbus.PushPlugin.subscribe", {
			callback: name
		}, function(m) {
			map[name] = ontrap;

			if(!nowPolling) {
				doPoll();
			}

			if(callback != null) {
				callback(m);
			}

		}, {
			source: "0"
		})
	};

	this.unregister = function(name, ontrap, callback) {
		if(name == null) return;

		if(isDebug) {
			console.log("unregister " + name);
		}

		map[name] = null;
		delete map[name];

		send("org.krakenapps.msgbus.PushPlugin.unsubscribe", {
			callback: name
		}, function(resp) {
			if(callback != null) {
				callback(resp);
			}
		});
	};

	this.items = function() { 
		console.log("------------items--------------");
		console.log(map);
	}
}

var trap = new Trap();

return {
	"registerTrap": trap.register,
	"unregisterTrap": trap.unregister,
	"send": send,
	"debugTrap": trap.items
}

});