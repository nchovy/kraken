var socket;
var channel;
var processManager;
var programManager;
var windowManager;

if (window.WebSocket) {
	socket = new WebSocket("ws://" + window.location.host + "/websocket");
	socket.onmessage = function(event) { 
		/* alert('received: ' + event.data); */ 
		channel.onreceive(event.data);
	};
	socket.onopen = function(event) { 
		$('#main p').remove();
		var fullmask = $('<div>').addClass('fullmask').appendTo('#main');
		var cell = $('<div>').addClass('cell').addClass('closewin')
						.html(closeContent)
						.appendTo(fullmask)
						.setCellsize(50, 27)
						.load('/login.html', LoginPage.initialize);
		
		// create global objects
		channel = new Channel();
		processManager = new ProcessManager();
		programManager = new ProgramManager();
		windowManager = new WindowManager();
	};
	socket.onerror = function(error) {
		console.error(error);
	};
	
	var closeContent = "";
	$.get('close.html', function(data) {
		closeContent = data;
	});
	
	socket.onclose = function(event) {
		onClose();
	};
} else {
	alert("Your browser does not support Web Socket.");
}

$.fn.setCellsize = function(w, h) {
	this.css('width', w + 'em');
	this.css('height', h + 'em');
	this.css('margin-left', -(w/2) + 'em');
	this.css('margin-top', -(h/2) + 'em');
	
	return this;
}

function onClose() {
	var fullmask = $('<div>').addClass('fullmask').appendTo('#main');
	var cell = $('<div>').addClass('cell').addClass('closewin').html(closeContent).setCellsize(30, 10).appendTo(fullmask);
	
	/*
	var win = new Ext.Window({
		layout: 'fit',
		title: 'Connection closed',
		width: 300,
		height: 80,
		closeAction: 'close',
		plain: true,
		//items : new PanUi(),
		html: 'Connection closed! Check Kraken console.',
		modal: true,
		maximizable: true,
		renderTo: Ext.getBody(),
	});

	win.show(this);
	*/
}

// Utility functions

Ext.apply(Ext.form.VTypes, {
    IPAddress:  function(v) {
        return /^\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}$/.test(v);
    },
    IPAddressText: 'Must be a numeric IP address',
    IPAddressMask: /[\d\.]/i
});

Date.prototype.fm = function() {
	return this.format('Y-m-d H:i:s');
};

Array.prototype.wrap = function() {
	var arr = [];
	$.each(this, function(idx, el) { arr.push([el]); });
	return arr;
};

Array.prototype.findAll = function (func) {
    var result = [];
    if (func == null || typeof func != 'function') {
        return this;
    }
    for (var i = 0; i < this.length; i++) {
        if (func(this[i])) {
            result.push(this[i]);
        }
    }
    return result;
};

String.prototype.capitalize = function() {
	return this.charAt(0).toUpperCase() + this.slice(1);
}

String.prototype.addNum = function() {
	var r = /\(\d*\)$/;
	if (r.test(this)) {
		var num_with_braket = this.match(r)[0];
		var num_string = num_with_braket.match(/\d+/)[0];
		var name = this.replace(num_with_braket, '');
		var num = parseInt(num_string);
		return name + '(' + (num + 1).toString() + ')';
	} else {
		return this + ' (1)';
	}
}

Ext.ErrorFn = function(resp) {
	console.log('--error--');
	console.log(resp);
	
	Ext.Msg.alert('Error', 'An error occured!<br/>Please check browser console.');
}

Ext.override(Ext.tree.TreeLoader, {
	requestData : function(node, callback, scope){
		if(this.fireEvent("beforeload", this, node, callback) !== false){
			if(this.directFn){
				var args = this.getParams(node);
				args.push(this.processDirectResponse.createDelegate(this, [{callback: callback, node: node, scope: scope}], true));
				args.push(node);
				this.directFn.apply(window, args);
			}else{
				this.transId = Ext.Ajax.request({
					method:this.requestMethod,
					url: this.dataUrl||this.url,
					success: this.handleResponse,
					failure: this.handleFailure,
					scope: this,
					argument: {callback: callback, node: node, scope: scope},
					params: this.getParams(node)
				});
			}
		}else{
			
			
			this.runCallback(callback, scope || node, []);
		}
	}
});

var Observer = function () {
	this.observations = [];
};

var Observation = function (name, func) {
	this.name = name;
	this.func = func;
};

Observer.prototype = {
	addListener: function (name, func) {
		var exists = this.observations.findAll(function (i) {
			return i.name == name && i.func == func;
		}).length > 0;
		if (!exists) {
			this.observations.push(new Observation(name, func));
		}
	},
	removeListener: function (name, func) {
		this.observations.remove(function (i) {
			return i.name == name && i.func == func;
		});
	},
	fire: function (name, data, scope) {
		var funcs = this.observations.findAll(function (i) {
			return i.name == name;
		});
		funcs.forEach(function (i) {
			i.func.call(scope || window, data);
		});
	}
};

var HashTable = function() {
	var obj = {};
	var readonlyProps = [];
	this.observer = new Observer();
	
	for(var i = 0; i < arguments.length; i++ ) {
		var type = typeof arguments[i];
		
		if(type == 'string') {
			obj[arguments[i]] = null;
		}
		else if(type == 'object') {
			var cfg = arguments[i];
			if(cfg.hasOwnProperty('key')) {
				obj[cfg.key] = (cfg.hasOwnProperty('value') ? cfg.value : null);
				if(!!cfg.readonly && cfg.readonly == true) {
					readonlyProps.push(cfg.key);
				}
			}
			else {
				throw 'Cannot initialize HashTable';
			}
		}
		else {
			obj[arguments[i].toString()] = null;
		}
	}
	
	this.setValue = function(prop, val) {
		if(obj.hasOwnProperty(prop) && typeof prop == "string") {
			obj[prop] = val;
			this.observer.fire('setValue', arguments);
		}
	};
	
	this.get = function() {
		return obj;
	};
	
	this.getValue = function(prop) {
		if(obj.hasOwnProperty(prop) && typeof prop == "string") {
			return obj[prop];
		}
		else return null;
	};
	
	this.getReadonlyProperties = function() {
		return readonlyProps;
	};
}

var Binding = function(hashtable) {
	var that = this;
	this.bind = function() {
		for(prop in that) {
			if(prop == 'bind') continue;
			
			// right way
			var field = that[prop];
			field.on('change', function(f, prop) {
				return function() {
					hashtable.setValue(prop, f.getValue());
					
					if(!f.validate())
						f.markInvalid();
				}
			}(field, prop));
			
			// wrong way
			/*
			that[prop].on('change', function(f, val) {
				hashtable.setValue(prop, val);
			});
			*/
		}
	};
};

// end

Channel = function() {
	var waitingReqs = {};
	var errorReqs = {};
	var idCounter = 1;

	this.nextId = function() {
		idCounter = idCounter + 1;
		
		return idCounter + '';
	}
	
	this.rawSend = function(message, callback, error_callback) {
		if (!window.WebSocket) { return; }
		if (socket.readyState == WebSocket.OPEN) {
			waitingReqs[message[0].guid] = callback;
			errorReqs[message[0].guid] = error_callback;
			
			var json = JSON.stringify(message);
			//console.log(json);
			socket.send(json);
		} else {
			alert("The socket is not open.");
		}
	}
	
	var trapCallbackMap = {};
	
	this.registerTrap = function(method, callback) {
		var map = trapCallbackMap;
		map[method] = callback;
	}
	
	this.fireCallback = function(msg) {
		if(msg[0].type == 'Trap') {
			var map = trapCallbackMap;
			var method = msg[0].method;
			map[method](msg[1]);
		}
		else if(msg[0].type == 'Response') {
			var reqId = msg[0].requestId;
			
			// get and remove from waiting map
			var callback = waitingReqs[reqId];
			var error_callback = errorReqs[reqId];
			delete waitingReqs[reqId];
			delete errorReqs[reqId];
			
			if(!!msg[0].errorMessage) {
				if(error_callback != null) {
					error_callback(msg[0]);
				}
				else { 
					console.log('An error occurred from ' + msg[0].method);
					console.log(msg);
				}
			}
			else {
				// invoke callback
				callback(msg[1]);
			}
		}		
	}
	
	StreamStore = new Ext.data.JsonStore({
		fields: [ 'type', 'method', 'time' ],
		root: 'list'
	});
	
	var stream = { list: [] };
	this.streaming = false;
		
	this.getStream = function() {
		return stream;
	}
	
	this.getStreamStore = function() {
		return StreamStore;
	}
	
	this.clearStream = function() {
		stream.list = [];
		StreamStore.removeAll();
	}
	
	this.onreceive = function(data) {
		//console.log(data);
		var msg = JSON.parse(data);
		if(this.streaming) {
			var m = msg[0];
			m['time'] = new Date();
			
			this.getStream().list.unshift(m);
			var newMsg = new StreamStore.recordType(m);
			StreamStore.insert(0, newMsg);
		}
		
		this.fireCallback(msg);
	}
	
	this.send = function(pid, method, params, callback, error_callback) {
		var guid = this.nextId();
		
		var msg = [ 
			{ 
				"guid": guid,
				"type": "Request",
				"source": pid,
				"target": null,
				"method": method
			}, 
			params 
		];
		
		if(this.streaming) {
			var m = msg[0];
			m['time'] = new Date();
			
			this.getStream().list.unshift(m);
			var newMsg = new StreamStore.recordType(m);
			StreamStore.insert(0, newMsg);
		}
		this.rawSend(msg, callback, error_callback);
	}
	
	StreamStore.loadData(stream);
}

LoginPage = function() {
}

LoginPage.initialize = function() {
	$("#username").val("admin");
	$("#password").val("kraken");
	
	$("#username").focus(function() { $("#login-err").hide(); });
	$("#password").focus(function() { $("#login-err").hide(); })
				  .keypress(function(e) {
					if (e.keyCode == '13') {
						$("#login button").click();
					}
				  });
	
	$("#username").focus();
	
	$("#login button").click(function() {

		var username = $("#username").val();
		var password = $("#password").val();
		
		var token = { "user": username, "password": password };
		
		channel.send(1, "org.krakenapps.webconsole.plugins.AccountPlugin.login", token, function(resp) {
			resp.locale = 'en';
			
			programManager.setLocale(resp.locale);
			
			if(resp.result) {
				$('.fullmask').fadeOut('fast', function() {
					$('.fullmask').remove();
					$('#main').hide().load('/start.html', StartPage.initialize); 
				});
				
			}
			else {
				$("#password").val('');
				$("#login-err").show('highlight');
			}
		});
		//$('#main').load('/start.html', StartPage.initialize); 
	});;
}

StartPage = function() {
}

StartPage.initStartMenu = function() {
	function addLogoutMenu() {
		programManager.startMenu.add('-');
		programManager.startMenu.add({
			text: 'Logout',
			handler: function() {
				location.reload(true);
			}
		});
	}
	
	function buildStartMenu() {
		var sortNameAlphabetically = function (a, b) {
			var nameA = a.name.toLowerCase();
			var nameB = b.name.toLowerCase();
			
			if (nameA < nameB)
				return -1;
			if (nameA > nameB)
				return 1;
			return 0;
		}
		
		var packs = programManager.getPackages();
		
		var syspack;
		var sysidx;
		$.each(packs, function(idx, pack) {
			if(pack.name == 'System') {
				syspack = pack;
				sysidx = idx;
				return;
			}
		});
		
		packs.splice(sysidx, 1);
		packs.sort(sortNameAlphabetically);
		packs.unshift(syspack);
		
		$.each(packs, function(idx, pack) {
			var programMenu = new Ext.menu.Menu({});
			
			pack.programs.sort(sortNameAlphabetically)
			
			$.each(pack.programs, function(iprogram, program) {
				programMenu.addMenuItem({ 
					text: program.name,
					handler: function() {
						programManager.startProgram(program.name, program.path);
					}
				});
			});
			
			programManager.startMenu.add({
				text: pack.name,
				id: 'menu' + pack.id,
				menu: programMenu
			});
		});
		addLogoutMenu();
	}

	channel.send(1, "org.krakenapps.webconsole.plugins.ProgramPlugin.getPrograms", { },
		function(resp) {
			programManager.clearPackages();
			programManager.clearPrograms();
			programManager.startMenu.removeAll();
			
			$.each(resp.packages, function(idx, pack) {
				programManager.addPackage(pack.id, pack.name);
			});
			
			$.each(resp.programs, function(idx, p) {
				programManager.addProgram(p.name, p.path, p.package_id, p.program_id);
			});
			
			buildStartMenu();
		}
	);
}

StartPage.initialize = function() {
	$('#main').fadeIn('slow');
	StartPage.initStartMenu();
	
	$("#startButton div").css("float", "left");
	Ext.QuickTips.init();
	
	// need to convert to lazy loading
	$("head").append("<link>");
	var css = $("head").children(":last");
	css.attr({
		rel:  "stylesheet",
		type: "text/css",
		href: "/css/skin.css"
    });
}

Window = Ext.extend(Ext.Window, {
	pid: null,
	//parent: null,
    
	initComponent: function() {
		this.id = this.title.replace(/ /g, "") + "_" + this.pid;
		Window.superclass.initComponent.call(this);
	},
	    
    maximize : function(){
        if(!this.maximized){
            this.expand(false);
            this.restoreSize = this.getSize();
            this.restorePos = this.getPosition(true);
            if (this.maximizable){
                this.tools.maximize.hide();
                this.tools.restore.show();
            }
            this.maximized = true;
            this.el.disableShadow();

            if(this.dd){
                this.dd.lock();
            }
            if(this.collapsible){
                this.tools.toggle.hide();
            }
            this.el.addClass('x-window-maximized');
            this.container.addClass('x-window-maximized-ct');

            this.setPosition(0, $('#taskbar').height() + Window.topSpace);
            this.fitContainer();
            this.fireEvent('maximize', this);
        }
        return this;
    },
	
    onRender : function(ct, position){
        Ext.Window.superclass.onRender.call(this, ct, position);
		
        if(this.plain){
            this.el.addClass('x-window-plain');
        }

        
        this.focusEl = this.el.createChild({
                    tag: 'a', href:'#', cls:'x-dlg-focus',
                    tabIndex:'-1', html: '&#160;'});
        this.focusEl.swallowEvent('click', true);

        this.proxy = this.el.createProxy('x-window-proxy');
        this.proxy.enableDisplayMode('block');

        if(this.modal){
			/*
            this.mask = this.container.createChild({cls:'ext-el-mask'}, this.el.dom);
            this.mask.enableDisplayMode('block');
            this.mask.hide();
            this.mon(this.mask, 'click', this.focus, this);
			*/
			if(this.parent != null) {
				var parentBody = $('#' + this.parent.id);
				var parentMask = $('<div>');
				parentMask.addClass('ext-el-mask')
					.css('position', 'absolute')
					.css('top', '0px')
					.css('left', '0px')
					.width(this.parent.el.dom.clientWidth)
					.height(this.parent.el.dom.clientHeight)
					.appendTo(parentBody)
					.mousedown(function(e) {
						e.stopPropagation();
					});
					
				this.mask = Ext.get(parentMask.get(0));

				this.mask.enableDisplayMode('block');
				this.mask.hide();
				this.mon(this.mask, 'click', this.focus, this);
				
			}
        }
        if(this.maximizable){
            this.mon(this.header, 'dblclick', this.toggleMaximize, this);
        }
    },
	
    beforeShow : function(){
        delete this.el.lastXY;
        delete this.el.lastLT;
        if(this.x === undefined || this.y === undefined){
            var xy = this.el.getAlignToXY(this.container, 'c-c');
            var pos = this.el.translatePoints(xy[0], xy[1]);
            this.x = this.x === undefined? pos.left : this.x;
            this.y = this.y === undefined? pos.top : this.y;
        }
        //this.el.setLeftTop(this.x, this.y);
		this.el.setLeftTop(this.x, 60); //modified!! 스크린 사이즈보다 창 크기가 크면 이렇게 해야하는데...

        if(this.expandOnShow){
            this.expand(false);
        }

        if(this.modal){
            Ext.getBody().addClass('x-body-masked');
            this.mask.setSize(this.parent.el.dom.clientWidth, this.parent.el.dom.clientHeight); // modified!
            this.mask.show();
        }
    },
	
    onWindowResize : function(){
        if(this.maximized){
            this.fitContainer();
        }
        if(this.modal){
		/*
            this.mask.setSize('100%', '100%');
            var force = this.mask.dom.offsetHeight;
            this.mask.setSize(Ext.lib.Dom.getViewWidth(true), Ext.lib.Dom.getViewHeight(true));
			*/
        }
        this.doConstrain();
    }
});

Window.topSpace = 8;

WindowManager = function() {
	// process to multiple windows mapping
	var windows = []
	var windowGroup = new Ext.WindowGroup();

	this.createWindow = function(pid, title, width, height, items) {
		var desktopEl = Ext.get('workspace');
		var taskbarEl = Ext.get('taskbar');
		
		var win = new Window({
			pid: pid,
			layout: 'fit',
			title: title,
			width: width,
			height: height,
			closeAction: 'close',
			plain: true,
			items : items,
			maximizable: true,
			renderTo: desktopEl,
		});

		win.show(this);		
		win.addListener('destroy', function() {
			processManager.terminate(pid);
			
		});
		windows.push(win);
		
		return win;
	}
	
	this.createChildWindow = function(cfg) {
		var desktopEl = Ext.get('workspace');
		var taskbarEl = Ext.get('taskbar');
		
		var config = {
			pid: cfg.parent.pid,
			layout: 'fit',
			closeAction: 'close',
			plain: true,
			renderTo: desktopEl
		};
		
		Ext.apply(config, cfg);
		
		var win = new Window(config);

		win.show(this);
		win.addListener('destroy', function(thiswin) {
			var i;
			$.each(windows, function(idx, w) {
				if(windows[idx] == thiswin) {
					i = idx;
				}
			});
			windows.splice(i, 1);
		});
		windows.push(win);
		
		return win;
	}
	
	this.getWindows = function() {
		return windows;
	}
	
	this.getWindowByPid = function(pid) {
		var window;
		$.each(this.getWindows(), function(idx, win) {
			if(pid == win.pid) {
				window = win;
			}
		});
		
		return window;
	}
}

ProcessManager = function() {
	var processes = {};
	
	var processStore = new Ext.data.ArrayStore({
		fields: [
			{ name: 'pid', type: 'int' },
			{ name: 'name' }
		],
		listeners: {
			add: function() {
			},
			remove: function() {
			}
		}
	});
	
	
	this.launch = function(plugin) {
		// generate pid and associate it
		var pid = Math.floor(Math.random() * 50000) + '';
		processes[pid] = plugin;
		
		// add process to store
		var newTaskRecord = new processStore.recordType({
			pid: pid,
			name: plugin.name
		});
		processStore.add(newTaskRecord);
		
		// assign id to taskbar item
		var recentTask = programManager.taskbar.getComponent(programManager.taskbar.items.length - 1);
		if(Ext.ComponentMgr.isRegistered(recentTask)){
			//registered
		}
		else {
			//unregistered
			recentTask.id = 'task_' + pid;
			Ext.ComponentMgr.register(recentTask);
		}
		
		recentTask.addListener('arrowclick', function() {
			$('#' + this.menu.id).css('z-index', '60002');
		});
		
		recentTask.addListener('click', function() {
			windowManager.getWindowByPid(pid).toFront();
		});
		
		recentTask.menu.addMenuItem({
			text: 'Close',
			handler: function() {
				windowManager.getWindowByPid(pid).destroy();
				recentTask.destroy();
			}
		});
		
		// start plugin
		plugin.onstart(pid, []);
		return pid;
	}
	
	this.getProcesses = function() {
		return jQuery.extend({}, processes);
	}
	
	this.getProcessStore = function() {
		return processStore;
	}
	
	this.terminate = function(pid) { // 직접 호출하면 좀비 윈도우 생김
		Ext.getCmp('task_' + pid).destroy();
	
		plugin = processes[pid];
		plugin.onstop();
		delete processes[pid];
		
		var willRemoveTaskRecord = processStore.query('pid', pid);
		processStore.remove(willRemoveTaskRecord.items[0]);
	}
	
	processStore.loadData(processes);
}

Package = function(id, name) {
	this.id = id;
	this.name = name;
	this.programs = [];
	
	this.findById = function(packageId) {
		if(packageId == this.id) return this;
	}
}


ProgramManager = function() {
	var programs = [];
	var packages = [];
	var locale = 'en';
	
	this.loadJS = function(path) {
		var scripttag = $('<script>').attr('type', 'text/javascript').attr('src', path);
		$('head').append(scripttag);
	}
	
	this.loadCSS = function(path) {
		$('<link rel="stylesheet" type="text/css" href="'+path+'" >').appendTo("head");
	}
	
	this.setLocale = function(lo) {
		this.locale = lo;
	}
	
	this.getLocale = function() {
		return this.locale;
	}
	
	this.addProgram = function(name, path, packageId, programId) {
		var program = { 'name': name, 'path': path, 'package_id': packageId, 'program_id': programId }
		programs.push(program);
		
		var p = null;
		
		$.each(packages, function(idx, pack) {
			if(pack.findById(packageId) != null) {
				p = pack;
				p.programs.push(program);
			}
		})
	}
	
	
	var lastWidget = null;
	
	this.startWidget = function(plugin) {
		plugin.onstart();
	}
	
	this.registerWidget = function(pid, name, width, height, cmp) {
		lastWidget = cmp;
	}
	
	this.initWidget = function(path, callback) {
		$.ajax({
			url: path,
			dataFilter: function(data, arr) {
				data = data.replace('processManager.launch', 'programManager.startWidget');
				data = data.replace('windowManager.createWindow', 'programManager.registerWidget');
				
				return data;
			},
			dataType: 'script',
			success: callback
		});
	}
	
	this.getLastestWidget = function() {
		return lastWidget;
	}
	
	
	this.startProgram = function(name, path) {
		$.getScript(path);
		
		programManager.taskbar.add({
			xtype: 'splitbutton',
			text: name,
			menu: [
				{
					text: 'Add to Shortcut',
					handler: function() {
						var link = $('<a>').attr('href', '#').click(function() {
							programManager.startProgram(name, path);
						});
						link.text(name);
						var dv = $('<div>').append(link);
						dv.appendTo('#workspace');
					}
				}
			]
		});
		programManager.taskbar.doLayout();
	}
	
	this.addPackage = function(id, name) {
		packages.push(new Package(id, name));
	}
	
	this.getPackages = function() {
		return packages;
	}

	this.getPrograms = function() {
		return programs;
	}
	
	this.clearPackages = function() {
		packages = [];
	}
	
	this.clearPrograms = function() {
		programs = [];
	}
}

WizardPanel = Ext.extend(Ext.Panel, {
	step: null,
	
	constructor: function(config) {
		config = Ext.apply({
		}, config);
		
		WizardPanel.superclass.constructor.call(this, config);
	},
	
	getStep: function() {
		return this.step;
	},
	
	setStep: function(step) {
		this.step = step;
	},
	
	clearStep: function() {
		this.step = null;
	},
	
	isLast: function() {
		var ret = false;
		var items = this.ownerCt.items.items;
		
		var lastitem = items[items.length - 1];
		if(this == lastitem) {
			ret = true;
		}
		
		return ret;
	}
});

WizardContainer = Ext.extend(Ext.Panel, {
	constructor: function(config) {
		config = Ext.apply({
			layout: 'card',				
			border: false,
			defaults: { 
				border: false, 
				bodyStyle: 'padding: 15px',
				layoutConfig: {
					labelSeparator: ' '
				}
			},
			region: 'center',
			activeItem: 0
		}, config);
		
		WizardContainer.superclass.constructor.call(this, config);
	},
	
	goStep: function(step) {
		var target;
		$.each(this.items.items, function(i, wizardPanel) {
			if(step == wizardPanel.getStep()) {
				target = i;
			}
		});
		this.getLayout().setActiveItem(target);
	},
	
	getCurrent: function() {
		return this.getLayout().activeItem;
	}

});