function openCreateActionWindow(parentWin, manager, close_callback) {
	var props = {};
	var raw;
	
	function getResponseActionOptions(callback) {
		channel.send(1, 'org.krakenapps.siem.msgbus.ResponsePlugin.getResponseActionOptions',
			{
				"manager": manager,
				"locale": programManager.getLocale()
			},
			function(resp) {
				$.each(resp.options, function(idx, option) {
					// if(option.type == "integer") 타입별로 코딩
					props[option.display_name] = "";
					raw = resp.options;
				});
				
				Ext.getCmp('gridNewResponseActionProperties' + parentWin.pid).setSource(props);
				
				if(callback != null) callback();
			},
			Ext.FnError
		);
	};
	
	function createResponseAction() {
		var params = {
			"manager": manager,
			"namespace": 'local',
			"name": Ext.getCmp('txtName' + parentWin.pid).getValue()
		};
		
		function findName(dispName) {
			var name = null;
			$.each(raw, function(idx, option) {
				if(option.display_name == dispName) {
					name = option.name;
					return;
				}
			});
			
			return name;
		}
		
		for (p in props) {
			if(findName(p) != null) {
				params[findName(p)] = props[p];
			}
		}

		channel.send(1, 'org.krakenapps.siem.msgbus.ResponsePlugin.createResponseAction', params,
			function(resp) {
				window.close();
				close_callback(manager);
			},
			Ext.FnError
		);
	}
	
	var window;
	
	var CreateActionWindowUi = new Ext.Panel({
		layout: 'border',
		plain: true,
		border: false,
		modal: true,
		closeAction: 'close',
		listeners: {
			
		},
		buttons: [
			{
				text: 'Create',
				handler: createResponseAction
			},
			{
				text: 'Cancel',
				handler: function() {
					window.close();
				}
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
						xtype: 'textfield',
						fieldLabel: 'Manager',
						value: manager,
						disabled: true
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
						xtype: 'propertygrid',
						id: 'gridNewResponseActionProperties' + parentWin.pid,
						autoHeight: true,
						style: 'border: 1px solid #ccc',
						source: props,
						listeners: {
							'afterrender': function(e) {
								getResponseActionOptions(function() {
									var s = e.getHeight();
									setTimeout(function() {
										window.setHeight(s + 250);
									}, 10);
								});
							}
						}
					}
				]
			}
		]
	});
	
	window = windowManager.createChildWindow({
		title: 'Create Response Action',
		width: 400,
		height: 330,
		items: CreateActionWindowUi,
		parent: parentWin,
		modal: true,
		maximizable: false,
		resizable: false
	});
}