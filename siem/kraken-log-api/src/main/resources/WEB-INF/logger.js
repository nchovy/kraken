Logger = function() {
	this.name = "Logger";
	
	this.onstart = function(pid, args) {
		programManager.loadJS('logapi/logger_create.js');
		programManager.loadJS('js/ext/spinner/SpinnerCell.js');
		programManager.loadJS('js/ext/ext-ButtonRowSelectionModel.js');
		
		var gridLogger, gridLoggerFactory, gridParser, gridNormalizer;

		// Logger tab
		function getLoggers(callback) {
			channel.send(1, 'org.krakenapps.log.api.msgbus.LoggerPlugin.getLoggers', {},
				function(resp) {
					LoggerStore.removeAll(false);
					LoggerStore.loadData(resp);
					
					if(callback != null) callback();
				},
				Ext.ErrorFn
			);
		}
		
		function getSelectedLoggers() {
			var arr = [];
			$.each(gridLogger.getSelectionModel().selections.items, function(idx, item) {
				arr.push(item.data.full_name);
			});
			
			return arr;
		}
		
		function removeLoggers() {
			var sel = getSelectedLoggers();
			var params = { "loggers": sel };
			
			Ext.Msg.confirm('Remove Loggers', 'It will remove logger "' + sel.toString() + '"<br/>Are you sure?', function(btn) {
				if(btn == 'yes') {
					channel.send(1, 'org.krakenapps.log.api.msgbus.LoggerPlugin.removeLoggers',
						params,
						function(resp) {
							var pop = Ext.MessageBox.show({ title: 'Remove Loggers', width: 300, msg: 'Logger "<b>' + sel.toString() + '</b>" removed successfully!', closable: false });
							setTimeout(function() { pop.hide(); }, 1000);
							getLoggers();
						},
						Ext.ErrorFn
					);
				}
				else return;
			});
		}

		function startLogger(name, interval) {

			channel.send(1, 'org.krakenapps.log.api.msgbus.LoggerPlugin.startLogger', 
				{
					'logger': name,
					'interval': interval
				},
				function(resp) {
					activateLoggerTab();
				},
				Ext.ErrorFn
			);
		}
		
		function stopLogger(name) {

			channel.send(1, 'org.krakenapps.log.api.msgbus.LoggerPlugin.stopLogger', 
				{
					'logger': name,
					'wait_time': 5000 // hard coding
				},
				function(resp) {
					activateLoggerTab();
				},
				Ext.ErrorFn
			);
		}
		
		// Logger factory tab
		function getLoggerFactories(callback) {
			channel.send(1, 'org.krakenapps.log.api.msgbus.LoggerPlugin.getLoggerFactories',
				{
					'locale': programManager.getLocale()
				},
				function(resp) {
					
					LoggerFactoryStore.loadData(resp);
					
					if(callback != null) callback();
				},
				Ext.ErrorFn
			);
		}
		
		// Parser tab
		function getParsers(callback) {
			channel.send(1, 'org.krakenapps.log.api.msgbus.LoggerPlugin.getParsers', {},
				function(resp) {
					
					ParserStore.loadData(resp.parsers.wrap());
				},
				Ext.ErrorFn
			);
		}
		
		// Normalizer tab
		function getNormalizers(callback) {
			channel.send(1, 'org.krakenapps.log.api.msgbus.LoggerPlugin.getNormalizers', {},
				function(resp) {
					NormalizerStore.loadData(resp.normalizers.wrap());
				},
				Ext.ErrorFn
			);
		}
		
		// methods about tab
		
		function activateLoggerTab() {
			setTimeout(function() {
				getLoggers();
			}, 100);
		}
		
		function activateLoggerFactoryTab() {
			getLoggerFactories();
		}
		
		function activateParserTab() {
			getParsers();
		}
		
		function activateNormalizerTab() {
			getNormalizers();
		}
		
		// initialize store
		var LoggerStore = new Ext.data.JsonStore({
			fields: ['interval', 'status', 'description', 'log_count', 'last_run', 'last_log', 'factory_full_name', 'full_name', 'namespace', 'name'],
			root: 'loggers'
		});
		
		var initLogger = { loggers: [] }
		LoggerStore.loadData(initLogger);
		
		var LoggerFactoryStore = new Ext.data.JsonStore({
			fields: ['full_name', 'namespace', 'name'],
			root: 'factories'
		});
		
		var initLoggerFactory = { factories: [] }
		LoggerFactoryStore.loadData(initLoggerFactory);
		
		var ParserStore = new Ext.data.ArrayStore({
			fields: [ 'name' ]
		});
		
		var initParser = [];
		ParserStore.loadData(initParser);
		
		var NormalizerStore = new Ext.data.ArrayStore({
			fields: [ 'name' ]
		});
		
		var initNormalizer = [];
		NormalizerStore.loadData(initNormalizer);
		
		// Plugins
		var sm = new Ext.ux.grid.SpinnerRowSelectionModel({
			id: 'smSpinner' + pid,
			header: 'Interval',
			dataIndex: 'interval',
			width: 80
		});
		
		var sm2 = new Ext.ux.grid.ButtonRowSelectionModel({
			id: 'smButton' + pid,
			header: ' ',
			dataIndex: 'status',
			width: 60,
			iconCls: 'ico-start',
			text: 'unknown',
			listeners: {
				afterrender: function(cmp, el, v, p, record) {
					if(v == 'stopped') {
						el.button.setIconClass('ico-start');
						el.button.setText('Start');
						el.button.on('click', function() { 
							startLogger(record.data.full_name, record.data.interval);
						});
					}
					else {
						el.button.setIconClass('ico-stop');
						el.button.setText('Stop');
						el.button.on('click', function() { 
							stopLogger(record.data.full_name);
						});
					}
				}
			}
		});
		
		// Layout
		var MyWindowUi = new Ext.Panel({
			layout: 'border',
			border: false,
			items: [{
				xtype: 'tabpanel',
				id: 'tab' + pid,
				//activeTab: 0,
				region: 'center',
				split: true,
				border: false,
				items: [
					{
						title: 'Logger',
						xtype: 'panel',
						layout: 'border',
						listeners: { 
							activate: activateLoggerTab,
						},
						items: [
							{
								xtype: 'grid',
								id: 'gridLoggers' + pid,
								region: 'center',
								border: false,
								split: true,
								store: LoggerStore,
								viewConfig: { forceFit: true },
								sm: sm,
								columns: [
									{
										xtype: 'gridcolumn',
										dataIndex: 'status',
										header: 'Status',
										sortable: true,
										renderer: function(v) {
											if(v == 'stopped') {
												return '<span style="color:#cc0000">' + v + '</span>';
											}
											else {
												return '<span style="color:green">' + v + '</span>';
											}
										}
									},
									sm2,
									sm,
									/*{
										xtype: 'gridcolumn',
										dataIndex: 'full_name',
										header: 'Name',
										sortable: true,
										renderer: function (v) {
											var n = v.replace(/(\w+)\s*\\\s*(\w+)/, '$1\\<b>$2</b>');
											return n;
										},
										width: 140
									},*/
									{
										xtype: 'gridcolumn',
										dataIndex: 'name',
										header: 'Name',
										sortable: true,
										renderer: function (v) {
											return '<b>' + v + '</b>';
										}
									},/*
									{
										xtype: 'gridcolumn',
										dataIndex: 'namespace',
										header: 'Namespace',
										sortable: true,
									},*/
									{
										xtype: 'gridcolumn',
										dataIndex: 'description',
										header: 'Description',
										sortable: true,
										width: 200
									},
									{
										xtype: 'gridcolumn',
										dataIndex: 'log_count',
										header: 'Count',
										sortable: true,
										width: 60
									},
									{
										xtype: 'gridcolumn',
										dataIndex: 'last_run',
										header: 'Last Run',
										sortable: true,
										width: 140
									},
									{
										xtype: 'gridcolumn',
										dataIndex: 'factory_full_name',
										header: 'Factory',
										sortable: true,
									}
								],
								tbar: {
									xtype: 'toolbar',
									items: [
										{
											xtype: 'button',
											text: 'Create Logger',
											handler: function() {
												getLoggerFactories(function() {
													openCreateLoggerWindow(window, LoggerFactoryStore, getLoggers);
												});
											},
											iconCls: 'ico-add'
										},
										{
											xtype: 'button',
											text: 'Remove',
											handler: removeLoggers,
											iconCls: 'ico-remove'
										},
										'-',
										{
											xtype: 'button',
											text: 'Refresh',
											iconCls: 'ico-refresh',
											handler: function() {
												activateLoggerTab();
											}
										}
									]
								}
							}
						]
					},
					{
						title: 'Logger Factory',
						xtype: 'panel',
						layout: 'border',
						listeners: { 
							activate: activateLoggerFactoryTab,
						},
						items: [
							{
								xtype: 'grid',
								id: 'gridLoggerFactories' + pid,
								region: 'center',
								split: true,
								border: false,
								store: LoggerFactoryStore,
								viewConfig: { forceFit: true },
								columns: [
									{
										xtype: 'gridcolumn',
										dataIndex: 'full_name',
										header: 'Full Name',
										sortable: true,
									},
									{
										xtype: 'gridcolumn',
										dataIndex: 'namespace',
										header: 'Namespace',
										sortable: true,
									},
									{
										xtype: 'gridcolumn',
										dataIndex: 'name',
										header: 'Name',
										sortable: true,
									}
								]
							}
						]
					},
					{
						title: 'Parser',
						xtype: 'panel',
						layout: 'border',
						listeners: {
							activate: activateParserTab
						},
						items: [
							{
								xtype: 'grid',
								id: 'gridParsers' + pid,
								region: 'center',
								split: true,
								border: false,
								store: ParserStore,
								viewConfig: { forceFit: true },
								columns: [
									{
										xtype: 'gridcolumn',
										dataIndex: 'name',
										header: 'Parser Name',
										sortable: true
									}
								]
							}
						]
					},
					{
						title: 'Normalizer',
						xtype: 'panel',
						layout: 'border',
						listeners: {
							activate: activateNormalizerTab
						},
						items: [
							{
								xtype: 'grid',
								id: 'gridNomalizers' + pid,
								region: 'center',
								border: false,
								split: true,
								store: NormalizerStore,
								viewConfig: { forceFit: true },
								columns: [
									{
										xtype: 'gridcolumn',
										dataIndex: 'name',
										header: 'Normalizer Name',
										sortable: true
									}
								]
							}
						]
					}
				]
			}]
		});
		
		Ext.QuickTips.init();
		
		var window = windowManager.createWindow(pid, this.name ,770,300, MyWindowUi);
		
		gridLogger = Ext.getCmp('gridLoggers' + pid);
		gridLoggerFactory = Ext.getCmp('gridLoggerFactories' + pid);
		gridParser = Ext.getCmp('gridParsers' + pid);
		gridNormalizer = Ext.getCmp('gridNormalizers' + pid);
		Ext.getCmp('tab' + pid).setActiveTab(0);
	}
	
	this.onstop = function() {
	}
}

processManager.launch(new Logger());