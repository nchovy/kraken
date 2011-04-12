Logger = function() {
	this.name = "Logger Manager";
	
	this.onstart = function(pid, args) {
		programManager.loadJS('siem/logger_create_wizard.js');
		
		// Store for logapi
		var LoggerFactoryStore = new Ext.data.JsonStore({
			fields: ['full_name', 'namespace', 'name'],
			root: 'factories'
		});
			
		var initLoggerFactory = { factories: [ ] }
		LoggerFactoryStore.loadData(initLoggerFactory);
		
		var LoggerStore = new Ext.data.JsonStore({
			fields: ['interval', 'status', 'description', 'log_count', 'last_run', 'last_log', 'factory_full_name', 'full_name', 'namespace', 'name'],
			root: 'loggers'
		});
		
		var initLogger = { loggers: [] }
		LoggerStore.loadData(initLogger);
		
		var ParserFactoryStore = new Ext.data.JsonStore({
			fields: ['description', 'display_name', 'name', 'options'],
			root: 'factories'
		});
		
		var initParserFactory = { factories: [] }
		ParserFactoryStore.loadData(initParserFactory);
		
		// getters
		function getLoggers(callback) {
			channel.send(1, 'org.krakenapps.log.api.msgbus.LoggerPlugin.getLoggers', {},
				function(resp) {
					LoggerStore.removeAll(false);
					LoggerStore.loadData(resp);
					
					if(callback != null) callback();
				}
			);
		}
		
		function getLoggerFactories(callback) {
			channel.send(1, 'org.krakenapps.log.api.msgbus.LoggerPlugin.getLoggerFactories',
				{
					'locale': programManager.getLocale()
				},
				function(resp) {
					LoggerFactoryStore.removeAll(false);
					LoggerFactoryStore.loadData(resp);
					
					if(callback != null) callback();
				}
			);
		}
		
		function getParserFactories(callback) {
			channel.send(1, 'org.krakenapps.log.api.msgbus.LoggerPlugin.getParserFactories', {},
				function(resp) {
					console.log(resp);
					ParserFactoryStore.removeAll(false);
					ParserFactoryStore.loadData(resp);
					
					if(callback != null) callback();
				}
			);
		}
		
		// Store for siem 
		var ManageLoggerStore = new Ext.data.JsonStore({ 
			fields: [ 'enabled', 'parser', 'logger' ],
			root: 'loggers'
		});
		
		var initManageLogger = { loggers: [
			{ 'enabled': true, 'parser': "httpd", 'logger': "gotoweb" },
			{ 'enabled': false, 'parser': "httpd", 'logger': "test" },
		] };
		ManageLoggerStore.loadData(initManageLogger);
		
		// Grid
		var gridLogger = new Ext.grid.GridPanel({
			tbar: [
				{
					xtype: 'button',
					text: 'Add Logger',
					iconCls: 'ico-add',
					handler: function() {
						getLoggerFactories(function() {
							getLoggers(function() {
								getParserFactories(function() {
									openCreateManagedLoggerWizard(window, LoggerFactoryStore, LoggerStore, ParserFactoryStore);
								});
							});
						});
					}
				},
				{
					xtype: 'button',
					text: 'Remove',
					iconCls: 'ico-remove'
				},
			],
			region: 'center',
			border: false,
			store: ManageLoggerStore,
			viewConfig: { forceFit: true },
			columns: [
				{
					xtype: 'gridcolumn',
					dataIndex: 'enabled',
					header: '&nbsp;'
				},
				{
					xtype: 'gridcolumn',
					dataIndex: 'logger',
					header: 'Logger'
				},
				{
					xtype: 'gridcolumn',
					dataIndex: 'parser',
					header: 'Parser'
				}
			]
		});
		
		// Layout
		var MainUI = new Ext.Panel({
			layout: 'border',
			border: false,
			items: [gridLogger]
		});
		
		Ext.QuickTips.init();
		var window = windowManager.createWindow(pid, this.name ,700, 300, MainUI);
	}
	
	this.onstop = function() {
	}
}

processManager.launch(new Logger());