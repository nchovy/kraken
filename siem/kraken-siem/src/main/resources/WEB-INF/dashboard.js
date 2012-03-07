var EventArray = function() { 
	//console.log('asdf');
};
EventArray.prototype = new Array();
EventArray.prototype.push = function() {
	console.log('push');
	for(var i = 0; i < arguments.length; i++ ) {
		Array.prototype.push.call(this, arguments[i]);
	}
};

EventArray.prototype.unshift = function(idx) {
	console.log('unshift');
	for(var i = arguments.length - 1; i >= 0; i-- ) {
		Array.prototype.unshift.call(this, arguments[i]);
	}
};

EventArray.prototype.addListener = function(method, fn) {
	//Every
}

var zz;

Dashboard = function() {
	this.name = "Dashboard";
	
	this.msg = new EventArray(6);
	
	this.onstart = function(pid, args) {
		programManager.loadCSS('js/ext/portal/Portal.css');
		programManager.loadJS('js/ext/portal/Portal.js');
		programManager.loadJS('js/ext/portal/PortalColumn.js');
		programManager.loadJS('js/ext/portal/Portlet.js');
		
		var that = this;
		var portal, firstCol, lastCol;
		
		this.msg.addListener('push', function() {
			console.log('hello');
			this.__super__ = function() {
				console.log('hellohello');
			}
		});
		
		this.msg.push('test', 1,2);
		
		zz = this.msg;
		
		function addRssWidget() {
			var rss = { 
				channel: [],
				title: null,
				tools: [
					{
						id:'gear',
						handler: function(){
							Ext.Msg.prompt('RSS Widget', 'Please input RSS address.', function(c, v) {
								if(c == 'ok') {
									getFeedData(v, function() {
										FeedStore.removeAll();
										FeedStore.loadData(rss);
									});
								}
							});
						}
					},
					{
						id:'close',
						handler: function(e, target, panel) {
							panel.ownerCt.remove(panel, true);
						}
					}
				]
			};
			var feedStoreField = [ 'description', 'link', 'pubDate', 'title' ];
			var FeedStore = new Ext.data.JsonStore({
				fields: feedStoreField,
				root: 'channel'
			});
			
			function getFeedData(url, callback) {
				rss.channel = [];
				
				$.get(url, function(data) {
					var itemDom = $(data, 'channel').find('item');
					
					$.each(itemDom, function(idx, p) {
						var item = {};
						$.each($(p).children(), function(jdx, s) {
							
							try {
								item[s.nodeName] = s.firstChild.nodeValue;
							}
							catch(e) { }
							
							$.each(feedStoreField, function(kdx, f) {
								if(!item.hasOwnProperty(f)) {
									item[f] = '';
								}
							});
						});
						
						rss.channel.push(item);
						portlet.setTitle('<img src="/img/16-rss-square.png" style="width:16px;height:16px;margin-right:4px;vertical-align:sub">' + $(data).find('channel > title').text());
					});
					if(callback != null) callback();
				});
			}
			
			var portlet = new Ext.ux.Portlet({
				title: 'RSS Feed',
				tools: rss.tools,
				layout:'fit',
				sm: new Ext.grid.RowSelectionModel({singleSelect:true}),
				items: [
					{
						xtype: 'grid',
						store: FeedStore,
						height: 200,
						viewConfig: { forceFit: true },
						columns: [
							{
								xtype: 'gridcolumn',
								dataIndex: 'title',
								header: 'Title',
								sortable: true,
								width: 100
							}
						],
						listeners: {
							rowdblclick: function(cmp, i) {
								var selected = cmp.getSelectionModel().getSelected().data;
								window.open(selected.link);
							}
						}
					}
				]
			});
			
			getFeedData('/siem/blogosphere.xml', function() {
				firstCol.insert(0, portlet);
				
				FeedStore.removeAll();
				FeedStore.loadData(rss);
				portal.doLayout();
			});
		}
		
		function addWidget() {

			function addWidget(name, path) {
				programManager.initWidget(path, function() {
					var portlet = new Ext.ux.Portlet({
						title: name,
						items: programManager.getLastestWidget()
					});
					
					firstCol.insert(0, portlet);
					portal.doLayout();
				});
			}
			
			addWidget('sample', 'siem/dashboard_widget.js');
			
			//addWidget('sample2', 'siem/dashboard_widget2.js');
			
			
			/*
			firstCol.insert(0, new Ext.ux.Portlet({
				title: 'smaple',
				html: '<b>this is smaple, not sample</b>',
				tools: tools
			}));
			*/
			
			/*
			firstCol.insert(0, {
				xtype: 'portlet',
				title: 'Testgrid',
				html: 'asdfasdfasdf',
				tools: tools,
				items: [
					{
						xtype: 'grid',
						store: RuleStore,
						height: 120,
						viewConfig: { forceFit: true },
						columns: [
							{
								xtype: 'gridcolumn',
								dataIndex: 'severity',
								header: 'Severity',
								sortable: true,
								width: 70
							},
							{
								xtype: 'gridcolumn',
								dataIndex: 'first_seen',
								header: 'First Seen',
								sortable: true,
								width: 180
							}
						]
					}
				]
			});
			*/
			
			portal.doLayout();
			
			
			/*
			firstCol.insert(0, {
				xtype: 'grid',
				split: true,
				store: RuleStore,
				height: 120,
				viewConfig: { forceFit: true },
				columns: [
					{
						xtype: 'gridcolumn',
						dataIndex: 'severity',
						header: 'Severity',
						sortable: true,
						width: 70
					},
					{
						xtype: 'gridcolumn',
						dataIndex: 'first_seen',
						header: 'First Seen',
						sortable: true,
						width: 180
					}
				],
				tools: tools,
				anchor: '100%',
				collapsible: true,
				draggable: true,
				cls: 'x-portlet',
				title: 'Test Grid'
			});
			*/
		}
		
		// initialize store
		var RuleStore = new Ext.data.JsonStore({
			fields: [ "category", "count", "dst_ip", "dst_port", "first_seen", "host", "key_id", "key_source", "last_seen", "msg_key", "msg_properties", "severity", "src_ip", "src_port",
				"src", "dst" ],
			root: 'events'
		});
		
		var sampleRule = {
			events: [ 
				{
					"host": null,
					"count": 3,
					"dst_port": null,
					"dst_ip": "localhost/127.0.0.1",
					"severity": 3,
					"src_port": null,
					"key_id": 91,
					"first_seen": "2011-01-23 06:30:22.0",
					"category": "Attack",
					"src_ip": "/61.7.235.206",
					"msg_key": "login-bruteforce",
					"last_seen": "2011-01-23 06:30:29.0",
					"key_source": 0
				},
				{
					"host": null,
					"count": 4,
					"dst_port": null,
					"dst_ip": "localhost/127.0.0.1",
					"severity": 3,
					"src_port": null,
					"key_id": 92,
					"first_seen": "2011-01-23 06:30:22.0",
					"category": "Attack",
					"src_ip": "/61.7.235.206",
					"msg_key": "login-bruteforce",
					"last_seen": "2011-01-23 06:30:31.0",
					"key_source": 0
				} 
			]
		};
		var initRule = {
			events: []
		};
		
		RuleStore.loadData(sampleRule);
		
		var tools = [
			{
				id:'gear',
				handler: function(){
					Ext.Msg.alert('Message', 'The Settings tool was clicked.');
				}
			},
			{
				id:'close',
				handler: function(e, target, panel) {
					panel.ownerCt.remove(panel, true);
				}
			}
		];
		
		var shortBogusMarkup = 'hello world<br/>b<br/>a<br/>z<br/>';
		
		// Layout
		MyWindowUi = Ext.extend(Ext.Panel, {
			layout: 'border',
			border: false,
			initComponent: function() {
				this.items = [
					{
						xtype: 'panel',
						layout: 'border',
						region: 'center',
						border: false,
						tbar: {
							xtype: 'toolbar',
							items: [
								{
									text: 'Add',
									iconCls: 'ico-add',
									handler: addWidget
								},
								{
									text: 'RSS Add',
									iconCls: 'ico-rss',
									handler: addRssWidget
								},
								{
									text: 'asdf',
									handler: function() {
										console.log(that.msg);
									}
								}
							]
						},
						items: [
							{
								xtype: 'portal',
								id: 'portal' + pid,
								region: 'center',
								border: false,
								margins: '0 0 0 0',
								items: [
									{
										id: 'firstCol' + pid,
										columnWidth:.33,
										style:'padding:10px 0 10px 10px',
										items: [
											{
												title: 'Another Panel 1',
												tools: tools,
												html: shortBogusMarkup												
											}
										]
									},
									{
										columnWidth:.33,
										style:'padding:10px 0 10px 10px',
										items:[
											{
												title: 'Panel 2',
												tools: tools,
												html: shortBogusMarkup
											}
										]
									},
									{
										id: 'lastCol' + pid,
										columnWidth:.33,
										style:'padding:10px',
										items:[
											{
												title: 'Panel 3',
												tools: tools,
												html: shortBogusMarkup
											}
										]
									}
								]
							},
							{
								xtype: 'grid',
								id: 'gridBlockedIp' + pid,
								region: 'south',
								split: true,
								store: RuleStore,
								height: 120,
								viewConfig: { forceFit: true },
								columns: [
									{
										xtype: 'gridcolumn',
										dataIndex: 'severity',
										header: 'Severity',
										sortable: true,
										width: 70
									},
									{
										xtype: 'gridcolumn',
										dataIndex: 'first_seen',
										header: 'First Seen',
										sortable: true,
										width: 180
									},
									{
										xtype: 'gridcolumn',
										dataIndex: 'last_seen',
										header: 'Last Seen',
										sortable: true,
										width: 180
									},
									{
										xtype: 'gridcolumn',
										dataIndex: 'category',
										header: 'Category',
										sortable: true,
										width: 80
									},
									{
										xtype: 'gridcolumn',
										dataIndex: 'src',
										header: 'Source',
										sortable: true,
										width: 160
									},
									{
										xtype: 'gridcolumn',
										dataIndex: 'dst',
										header: 'Destination',
										sortable: true,
										width: 160
									},
									{
										xtype: 'gridcolumn',
										dataIndex: 'msg_key',
										header: 'Message',
										sortable: true,
										width: 150
									},
									{
										xtype: 'gridcolumn',
										dataIndex: 'count',
										header: 'count',
										sortable: true,
										width: 60
									},
								]
							}
						]
					},
				]
				MyWindowUi.superclass.initComponent.call(this);
			}
		});

		MyWindow = Ext.extend(MyWindowUi, {
			initComponent: function() {
				MyWindow.superclass.initComponent.call(this);
			}
		});
		
		Ext.QuickTips.init();
		var cmp1 = new MyWindow({ });
		
		var dashboardwindow = windowManager.createWindow(pid, this.name, 1020, 600, cmp1);
		cmp1.show();
		
		portal = Ext.getCmp('portal' + pid);
		lastCol = Ext.getCmp('lastCol' + pid);
		firstCol = Ext.getCmp('firstCol' + pid)
	}
	
	this.onstop = function() {
		
	}
}

processManager.launch(new Dashboard()); 