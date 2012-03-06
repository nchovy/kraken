var getMore;

Logviewer = function() {
	this.name = "Log Viewer";
	
	this.onstart = function(pid, args) {
		var gridLog, treeLoggers, rawtbRange;
		var totalLength = 0;
		var Loggers = [];
		
		function init() {
			getLoggers(function() {
				var root = treeLoggers.setRootNode(new Ext.tree.AsyncTreeNode({
					text: 'Loggers',
					expanded: true,
					children: Loggers
				}));
				
				var sm = treeLoggers.getSelectionModel();
				sm.clearSelections(true);
				sm.select(root.firstChild);
				
				try { 
					root.firstChild.fireEvent('click', Ext.emptyFn);
				}
				catch (err) {}
				
			});
		}
		
		function getLoggers(callback) {
			channel.send(1, 'org.krakenapps.siem.msgbus.LoggerPlugin.getLoggers', {},
			function(resp) {
				Loggers = [];
				$.each(resp.loggers, function (idx, l) {
					Loggers.push({
						text: l.fullname,
						leaf: true,
						type: 'logger',
						iconCls: 'ico-db',
						listeners: {
							'click': function() {
								rawtbRange.resetVal();
								totalLength = 0;
								traverse(l.fullname);
							}
						}
					});
				});
				
				if(callback != null) callback();
			});
		}
		
		function traverse(logger) {
			var parameters = {
				"logger": logger,
				"from": rawtbRange.hiddenVal.startDate.fm() + "+0900",
				"to": rawtbRange.hiddenVal.endDate.fm() + "+0900",
				"offset": totalLength, 
				"limit": 10 
			}
			
						
			channel.send(1, 'org.krakenapps.siem.msgbus.LoggerPlugin.traverse', parameters,
			function(resp) {
				
				var o = $('#' + gridLog.getView().el.id + ' > .x-grid3-viewport');
				var scroller = o.find('.x-grid3-scroller');
				
				RuleStore.loadData(resp);
				totalLength = resp.logs.length;
				
				scroller.scroll(function(a) {
					
					if(a.target.scrollTop == a.target.scrollHeight - scroller.height()) {
						
						gridLog.setTitle('<img src="img/ajax-loader.gif" style="vertical-align:middle"/> Loading older logs...');
						
						getMore(a.target.scrollTop);
					}
				});
				
				
			});
		}
		
		function getCurrentLogger() {
			return treeLoggers.getSelectionModel().selNode.text;
		}
		
		
		//function getMore() {
		
		getMore = function(scrTop) {
			channel.send(1, 'org.krakenapps.siem.msgbus.LoggerPlugin.traverse',
			{
				"logger": getCurrentLogger(),
				"from": rawtbRange.hiddenVal.startDate.fm() + "+0900",
				"to": rawtbRange.hiddenVal.endDate.fm() + "+0900",
				"offset": totalLength, 
				"limit": 10
			},
			function(resp) {
				var o = $('#' + gridLog.getView().el.id + ' > .x-grid3-viewport');
				var scroller = o.find('.x-grid3-scroller');
				
				setTimeout(function() {
					RuleStore.loadData(resp, true);
					totalLength = totalLength + resp.logs.length;
					
					$(scroller).scrollTop(scrTop);
					
					gridLog.setTitle('Logs');
					
					if(resp.logs.length == 0) {
						
						scroller.unbind('scroll');
						return;
					}
				}, 400);
			});
		}
		
		var TreeNodeGroupsOnly = new Ext.tree.AsyncTreeNode({ text: 'Loggers', expanded: true, children: [] });
		
		// initialize store
		var RuleStore = new Ext.data.JsonStore({
			fields: [ "id", "table_name", "data", "date" ],
			root: 'logs'
		});
		
		var sampleRule = {
			logs: [
				{
					"id": 47,
					"table_name": "local\\openssh",
					"data": {
						"line": "Jan 23 06:30:43 navi sshd[32630]: Failed password for root from 61.7.235.206 port 44831 ssh2",
						"date": "2011-01-23 06:30:43+0900"
					},
					"date": "2011-01-23 06:30:43+0900"
				},
				{
					"id": 46,
					"table_name": "local\\openssh",
					"data": {
						"line": "Jan 23 06:30:42 navi sshd[32630]: pam_unix(sshd:auth): authentication failure; logname= uid=0 euid=0 tty=ssh ruser= rhost=61.7.235.206 user=root",
						"date": "2011-01-23 06:30:42+0900"
					},
					"date": "2011-01-23 06:30:42+0900"
				}
			]
		};
		
		var initRule = {
			logs: []
		};
		
		RuleStore.loadData(sampleRule);
		
		function dataParser(val) {
			return val.line;
		}
		
		Ext.apply(Ext.form.VTypes, {
			daterange : function(val, field) {
				var date = field.parseDate(val);

				if(!date) {
					return false;
				}
				if (field.startDateField) {
					var start = Ext.getCmp(field.startDateField);
					if (!start.maxValue || (date.getTime() != start.maxValue.getTime())) {
						start.setMaxValue(date);
						start.validate();
					}
				}
				else if (field.endDateField) {
					var end = Ext.getCmp(field.endDateField);
					if (!end.minValue || (date.getTime() != end.minValue.getTime())) {
						end.setMinValue(date);
						end.validate();
					}
				}
				return true;
			}
		});
		
		// Layout
		MyWindowUi = Ext.extend(Ext.Panel, {
			layout: 'border',
			listeners: {
				"render" : init
			},
			initComponent: function() {
				this.items = [
					{
						xtype: 'treepanel',
						id: 'treeLoggers' + pid,
						region: 'west',
						width: 120,
						split: true,
						root: TreeNodeGroupsOnly
					},
					{
						xtype: 'grid',
						id: 'gridBlockedIp' + pid,
						region: 'center',
						split: true,
						//title: '<button onclick="getMore()">getMore()</button>',
						title: 'Logs',
						store: RuleStore,
						viewConfig: { forceFit: true },
						columns: [
							{
								xtype: 'gridcolumn',
								dataIndex: 'date',
								header: 'Date',
								sortable: true,
								width: 150
							},
							{
								xtype: 'gridcolumn',
								dataIndex: 'table_name',
								header: 'Table Name',
								sortable: true,
								width: 100
							},
							{
								xtype: 'gridcolumn',
								dataIndex: 'id',
								header: 'ID',
								sortable: true,
								width: 40
							},
							{
								xtype: 'gridcolumn',
								dataIndex: 'data',
								header: 'Line',
								sortable: true,
								renderer: dataParser,
								width: 400
							}
						],
						tbar: {
							xtype: 'toolbar',
							enableOverflow: true,
							items: [
								{
									xtype: 'splitbutton',
									text: 'Choose a Range... <input type="text" id="rawtbRange' + pid + '" style="font-family: Courier New; border: 1px solid lightgray; background: rgba(255,255,255,0.5); width: 300px"/>',
									width: 360,
									handler: function() {
										this.showMenu();
									},
									menu: [
										'<span class="menu-title">Recent</span>',
										{
											text: '10 Min',
											val: [Date.MINUTE, -10],
											checked: false,
											group: 'theme',
											checkHandler: presetCheck
										},
										{
											text: '1 Hour',
											val: [Date.HOUR, -1],
											checked: false,
											group: 'theme',
											checkHandler: presetCheck
										},
										{
											text: '1 Day',
											val: [Date.DAY, -1],
											checked: false,
											group: 'theme',
											checkHandler: presetCheck
										},
										{
											text: '1 Week',
											val: [Date.DAY, -7],
											checked: false,
											group: 'theme',
											checkHandler: presetCheck
										},
										{
											text: '1 Year',
											val: [Date.YEAR, -1],
											checked: false,
											group: 'theme',
											checkHandler: presetCheck
										},
										'-',
										'<span class="menu-title">Custom...</span>',
										{
											text: 'Custom...',
											type: 'custom',
											val: [Date.YEAR, -1],
											checked: false,
											group: 'theme',
											checkHandler: presetCheck,
											handler: presetCheck
										},
									]
								},
								{
									xtype: 'button',
									text: 'Query',
									iconCls: 'ico-search',
									handler: function() {
										totalLength = 0;
										traverse(getCurrentLogger());
									},
								}
							]
						}
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
		
		var window = windowManager.createWindow(pid, 'Log Viewer',900,300, cmp1);
		cmp1.show();
		
		gridLog = Ext.getCmp('gridBlockedIp' + pid);
		treeLoggers = Ext.getCmp('treeLoggers' + pid);
		
		rawtbRange = $('#rawtbRange' + pid);
		rawtbRange.hiddenVal = { };
		rawtbRange.resetVal = function() {
			this.val('');
			this.hiddenVal.startDate = new Date().add(Date.YEAR, -1);
			this.hiddenVal.endDate = new Date();
		}
		rawtbRange.resetVal();
		
		function presetCheck(item, checked) {
			if(checked) {
				var startDate = new Date().add(item.val[0], item.val[1]);
				var endDate = new Date();
				
				rawtbRange.val(startDate.fm() + ' ~ ' + endDate.fm());
				rawtbRange.hiddenVal.startDate = startDate;
				rawtbRange.hiddenVal.endDate = endDate;
				
				if(item.type == 'custom') {
					rawtbRange.focus().select();
				}
			}
		}
	}
	
	this.onstop = function() {
		
	}
}

processManager.launch(new Logviewer()); 