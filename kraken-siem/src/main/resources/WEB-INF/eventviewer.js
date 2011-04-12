EventViewer = function() {
	this.name = "Event Viewer";
	
	this.onstart = function(pid, args) {
		function init() {
			
			channel.send(1, 'org.krakenapps.siem.msgbus.EventPlugin.getEvents',
			{
				"offset": 0,
				"limit": 10
			},
			function(resp) {
				
				var processedResponse = { events: [] };
				
				$.each(resp.events, function(idx, event) {
					event.src = "";
					if (event.src_ip != null)
						event.src += event.src_ip;
					if (event.src_port != null)
						event.src += ":" + event.src_port;
						
					event.dst = "";
					if (event.dst_ip != null)	
						event.dst += event.dst_ip;
					if (event.dst_port != null)
						event.dst += ":" + event.dst_port;
					processedResponse.events.push(event);
				});
				
				RuleStore.loadData(processedResponse);
			});
		}
		
		var TreeNodeGroupsOnly = new Ext.tree.AsyncTreeNode({ text: 'Loggers', expanded: true, children: [] })
		
		// initialize store
		var RuleStore = new Ext.data.JsonStore({
			fields: [ "category", "count", "dst_ip", "dst_port", "first_seen", "host", "key_id", "key_source", "last_seen", "msg_key", "msg_properties", "severity", "src_ip", "src_port",
				"src", "dst" ],
			root: 'events'
		});
		
		var sampleRule = {
			events: [ /*
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
				} */
			]
		};
		var initRule = {
			events: []
		};
		
		RuleStore.loadData(sampleRule);
		
		// Layout
		MyWindowUi = Ext.extend(Ext.Panel, {
			layout: 'border',
			listeners: {
				"render" : init
			},
			initComponent: function() {
				this.items = [
					{
						xtype: 'grid',
						id: 'gridBlockedIp' + pid,
						region: 'center',
						split: true,
						store: RuleStore,
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
						],
						/*tbar: {
							xtype: 'toolbar',
							items: [
								{
									xtype: 'buttongroup',
									title: 'Remove IP',
									columns: 1,
									items: [
										{
											xtype: 'button',
											text: 'Remove',
											iconCls: 'ico-remove',
											rowspan: 2,
											style: {
												'margin-left' : '5px',
												'margin-right': '5px'
											}
										}
									]
								}
							]
						}*/
					}
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
		
		var window = windowManager.createWindow(pid, 'Event Viewer', 900, 400, cmp1);
		cmp1.show();
	}
	
	this.onstop = function() {
		
	}
}

processManager.launch(new EventViewer()); 