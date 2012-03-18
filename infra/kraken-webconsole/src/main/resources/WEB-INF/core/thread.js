Thread = function() {
	this.name = "Thread";
	
	this.onstart = function(pid, args) {
		Ext.QuickTips.init();
		
		var myStoreStacktrace; // stacktrace of current thread 
		var rawdata; // raw JSON data
		var threads = {};
		
		function findStacktraceById(id) {
			var matched = null;
			$.each(rawdata.threads, function(idx, pair) {
				if(pair.thread.id == id) {
					matched = pair.stacktrace;
					return;
				}
			});
			
			return matched;
		}
		
		function initThreads(resp) {
			var sortedThreads = resp.threads.sort(function (a, b) {
				var aid = a.thread.id;
				var bid = b.thread.id;
				
				if(aid < bid)
					return -1;
				if(aid > bid)
					return 1;
				return 0;
			});
			
			rawdata = resp;
			threads.threads = [];
			
			$.each(sortedThreads, function(idx, pair) {
				threads.threads.push(pair.thread);
			});
			
			var smGridThreads = new Ext.grid.RowSelectionModel({singleSelect: true});
			smGridThreads.on('rowselect', function(sm, rowIdx, r) {
				
				var stacktraceObj = {};
				stacktraceObj.stacktrace = [];
				
				var matched = findStacktraceById(r.data.id);
				$.each(matched, function(idx, stack) {
					var ln = (stack.line_number == (-1 && -2)) ? '' : ':' + stack.line_number;
					var stackObj = {};
					stackObj.class_method = stack.class_name + '.<b>' + stack.method_name + '</b>';
					stackObj.file = ((stack.file_name == null) ? '' : (stack.file_name + ln));
					
					stacktraceObj.stacktrace.push(stackObj);
				});

				myStoreStacktrace.loadData(stacktraceObj);
			});
			
			MyPanelUi = Ext.extend(Ext.Panel, {
				layout: 'border',
				border: false,
				defaults: {
					split: true
				},
				initComponent: function() {
					this.filter = function() {
						var tbFilter = Ext.getCmp('tbFilter' + pid);
						
						myStoreThreads.filter('name', tbFilter.getValue(), true);
					};
					
					this.tbar = {
						xtype: 'toolbar',
						items: [
							{
								xtype: 'tbtext',
								text: 'Threads Filter &nbsp;'
							},
							{
								id: 'tbFilter' + pid,
								xtype: 'textfield',
								listeners: {
									'render': {
										fn: function() {
											this.getEl().on('keyup', function() {
												this.filter();
											}, this, { buffer: 500 });
										},
										scope: this
									}
								}
							}
						]
					};
					this.items = [
						{
							xtype: 'grid',
							title: 'Threads',
							store: myStoreThreads,
							region: 'north',
							height: 200,
							viewConfig: { forceFit: true },
							sm: smGridThreads,
							columns: [
								{
									xtype: 'gridcolumn',
									dataIndex: 'id',
									header: 'ID',
									sortable: true,
									width: 50,
									align: 'right'
								},
								{
									xtype: 'gridcolumn',
									dataIndex: 'name',
									header: 'Name',
									sortable: true,
									width: 250
								},
								{
									xtype: 'gridcolumn',
									dataIndex: 'priority',
									header: 'Priority',
									sortable: true,
									width: 50,
									align: 'center'
								},
								{
									xtype: 'gridcolumn',
									dataIndex: 'state',
									header: 'State',
									sortable: true,
									width: 100
								}
							]
						},
						{
							xtype: 'grid',
							title: 'Stacktrace',
							store: myStoreStacktrace,
							region: 'center',
							viewConfig: { forceFit: true },
							defaults: {
								sortable: false
							},
							columns: [
								{
									xtype: 'gridcolumn',
									dataIndex: 'class_method',
									width: 250,
									menuDisabled: true
								},
								{
									xtype: 'gridcolumn',
									dataIndex: 'file',
									width: 150,
									menuDisabled: true,
									align: 'right'
								}
							]
						}
					];
					MyPanelUi.superclass.initComponent.call(this);
				}
			});

			MyPanel = Ext.extend(MyPanelUi, {
				initComponent: function() {
					MyPanel.superclass.initComponent.call(this);
				}
			});
			
			StoreThreads = Ext.extend(Ext.data.JsonStore, {
				constructor: function(cfg) {
					cfg = cfg || {};
					StoreThreads.superclass.constructor.call(this, Ext.apply({
						root: 'threads',
						fields: [
							{
								name: 'id'
							},
							{
								name: 'name'
							},
							{
								name: 'priority'
							},
							{
								name: 'state'
							}
						]
					}, cfg));
				}
			});
			var myStoreThreads = new StoreThreads();
			myStoreThreads.loadData(threads);

			StoreStacktrace = Ext.extend(Ext.data.JsonStore, {
				constructor: function(cfg) {
					cfg = cfg || {};
					StoreStacktrace.superclass.constructor.call(this, Ext.apply({
						root: 'stacktrace',
						fields: [
							{
								name: 'class_method'
							},
							{
								name: 'file'
							}
						]
					}, cfg));
				}
			});
			myStoreStacktrace = new StoreStacktrace();
			
			
		    var cmp1 = new MyPanel({});
			var window = windowManager.createWindow(pid, 'Thread',600,450, cmp1);
		}
		
		
		channel.send(1, 'org.krakenapps.webconsole.plugins.ThreadMonitorPlugin.getThreads', {}, initThreads);
		
	}
	
	this.onstop = function() {
		
	}
}

processManager.launch(new Thread()); 