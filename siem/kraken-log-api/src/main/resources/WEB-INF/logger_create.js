function openCreateLoggerWindow(parentWin, factoryStore, getLoggers) {
	var fsOptions;
	
	var jsOptions = null;
	
	function getFactoryOptions(val) {
		channel.send(1, 'org.krakenapps.log.api.msgbus.LoggerPlugin.getFactoryOptions',
			{
				"factory": val,
				"locale" : programManager.getLocale()
			},
			function(resp) {
				
				jsOptions = resp.options;
				var optLength = resp.options.length;
				
				fsOptions.setTitle('Factory Option for <i>' + val + '</i>');
				fsOptions.removeAll();
				$.each(resp.options, function(idx, opt) {
					fsOptions.add({
						fieldLabel: opt.display_name
					});
				});
				
				fsOptions.doLayout();
				
				window.setHeight(optLength * 40 + 260);
			},
			Ext.ErrorFn
		);
	}
	
	function createLogger() {
		var params = {
			"factory": Ext.getCmp('comboFactory' + parentWin.pid).getValue(),
			"namespace": Ext.getCmp('txtNamespace' + parentWin.pid).getValue(),
			"name": Ext.getCmp('txtName' + parentWin.pid).getValue(),
			"description": Ext.getCmp('txtDescription' + parentWin.pid).getValue()
		}
		
		var fieldOptions = Ext.getCmp('fieldsetFactoryOptions' + parentWin.pid);
		
		$.each(jsOptions, function(idx, opt) {
			params[opt.name] = $('#' + fieldOptions.items.keys[idx]).val();
		});
		
		channel.send(1, 'org.krakenapps.log.api.msgbus.LoggerPlugin.createLogger',
			params,
			function(resp) {
				
				getLoggers();
				window.close();
			},
			Ext.ErrorFn
		);
		
	}
	
	var CreateLoggerWindowUi = new Ext.Panel({
		layout: 'border',
		plain: true,
		border: false,
		modal: true,
		closeAction: 'close',
		buttons: [
			{
				text: 'Create',
				handler: createLogger
			},
			{
				text: 'Cancel',
				handler: function() { window.close(); }
			}
		],
		items: [
			{
				xtype: 'form',
				region: 'center',
				frame: true,
				bodyStyle: { 'padding': '15px' },
				defaults: { anchor: '100%' },
				items: [
					{
						xtype: 'combo',
						id: 'comboFactory' + parentWin.pid,
						fieldLabel: 'Logger Factory',
						store: factoryStore,
						mode: 'local',
						valueField: 'name',
						displayField: 'full_name',
						forceSelection: true,
						listeners : {
							select: function() {
								getFactoryOptions(this.value);
								//window.setHeight(260 + );
							}
						}
					},
					{
						xtype: 'textfield',
						id: 'txtNamespace' + parentWin.pid,
						fieldLabel: 'Namespace',
						value: 'local'
					},
					{
						xtype: 'textfield',
						id: 'txtName' + parentWin.pid,
						fieldLabel: 'Name'
					},
					{
						xtype: 'textarea',
						id: 'txtDescription' + parentWin.pid,
						fieldLabel: 'Description'
					},
					{
						xtype: 'fieldset',
						id: 'fieldsetFactoryOptions' + parentWin.pid,
						title: 'Factory Option',
						autoHeight:true,
						defaults: { anchor: '100%' },
						style: 'margin-top: 15px',
						defaultType: 'textfield',
						items :[
							
						]
					}
				]
			}
		]
	});

	var window = windowManager.createChildWindow({
		title: 'Create Logger',
		width: 400,
		height: 265,
		items: CreateLoggerWindowUi,
		parent: parentWin,
		modal: true,
		maximizable: false,
		resizable: false
	});
	
	window.setSize(400, 265);
	
	fsOptions = Ext.getCmp('fieldsetFactoryOptions' + parentWin.pid);
	
	
}