Response = function() {
    this.name = "Response Manager";

    this.onstart = function(pid, args) {
		programManager.loadJS('js/ext/ext-ButtonRowSelectionModel.js');
		programManager.loadJS('siem/response_create_action.js');
		var Stores = {};
		
		function getResponseManagers() { // init
			channel.send(1, 'org.krakenapps.siem.msgbus.ResponsePlugin.getResponseManagers', { },
				function(resp) {
					$.each(resp.managers, function(idx, manager) {
						MainUI.add({
							xtype: 'panel',
							title: manager,
							layout: 'border',
							items: [],
							tbar: {
								xtype: 'toolbar',
								items: [
									{
										xtype: 'button',
										text: 'Create Action',
										iconCls: 'ico-add',
										handler: function() {
											openCreateActionWindow(window, manager, refreshActions);
										}
									},
									{
										xtype: 'button',
										text: 'Remove',
										iconCls: 'ico-remove',
										handler: function() {
											removeAction(manager);
										}
									},
									'-',
									{
										xtype: 'button',
										text: 'Refresh',
										iconCls: 'ico-refresh',
										handler: function() {
											refreshActions(manager);
										}
									}
								]
							}
						});
						
						initStoreFields(manager, function() {
							if(resp.managers.length == Stores.length) {
								afterInitStoreFields();
							}
						}); // async function
					});
					
					function afterInitStoreFields() {
						console.log(Stores);
						
						$.each(resp.managers, function(idx, manager) {
							loadStore(manager, function() {
								setTimeout(function() {
									MainUI.setActiveTab(idx);
								}, 100);
							});
						});
						/*
						// 여기다 setActiveTab 하면 ButtonRowSelectionModel이 render가 미처 안되서 버튼이 두개씩 생김 ㅜㅜ
						if(MainUI.items.length > 0)
							MainUI.setActiveTab(0);
							*/
					}
					
				}
			);
		}
		
		function refreshActions(manager) {
			getResponseActions(manager, function(actions) {
				var grid = MainUI.tabs[manager].items.items[0];
				
				console.log(actions);
				grid.getStore().loadData({ 'actions': actions });
			});
		}
		
		function removeAction(manager) {
			var grid = MainUI.tabs[manager].items.items[0];
			var selected = grid.getSelectionModel().getSelected();
			if(selected == null) 
				return;
				
			Ext.Msg.confirm('Remove Action', 'It will remove action "' + selected.json.name + '"<br/>Are you sure?', function(btn) {
				if(btn == 'yes') {
					channel.send(1, 'org.krakenapps.siem.msgbus.ResponsePlugin.removeResponseAction',
						{
							"manager": manager, 
							"namespace": selected.json.namespace,
							"name": selected.json.name
						},
						function(resp) {
							refreshActions(manager);
							
							var pop = Ext.MessageBox.show({ title: 'Remove Action', width: 200, msg: 'Removed successfully!', closable: false });
							setTimeout(function() { pop.hide(); }, 1000);
						},
						Ext.FnError
					);					
				}
				else return;
			});
		}
		
		function loadStore(manager, callback) {
			// Plugins
			var sm2 = new Ext.ux.grid.ButtonRowSelectionModel({
				header: 'Mailer',
				dataIndex: 'mailer_name',
				iconCls: 'ico-mailgo',
				listeners: {
					afterrender: function(cmp, el, v, p, record) {
						el.button.setText(record.json.mailer_name);
						el.button.on('click', function() { 
							programManager.startProgram('Mailer', '/siem/dashboard_widget.js');
						});
					}
				}
			});
			
			var sm1 = new Ext.ux.grid.ButtonRowSelectionModel({
				header: 'Firewall',
				dataIndex: 'group_name',
				iconCls: 'ico-group',
				listeners: {
					afterrender: function(cmp, el, v, p, record) {
						el.button.setText(record.json.group_name);
						el.button.on('click', function() { 
							programManager.startProgram('Firewall', '/firewall/firewall.js');
						});
					}
				}
			});
			
			// initialize store
			var JsonStore = new Ext.data.JsonStore({
				fields: Stores[manager].fields,
				root: 'actions'
			});
			
			var initStoreFields = { actions: [] };
			JsonStore.loadData(initStoreFields);
			
			var gridColumns = [];
			
			$.each(Stores[manager].columns, function(jdx, column) {
				
				if(column.dataIndex == 'mailer_name') {
					column = null;
					column = sm2;
				}
				else if(column.dataIndex == 'group_name') {
					column = null;
					column = sm1;
				}
				
				column['xtype'] = 'gridcolumn';
				gridColumns.push(column);
			});
			
			// adding GridPanel
			var p = MainUI.tabs[manager];
			
			p.add({
				xtype: 'grid',
				region: 'center',
				border: false,
				store: JsonStore,
				viewConfig: { forceFit: true },
				columns: gridColumns
			});
			
			p.doLayout();
			
			// load data
			getResponseActions(manager, function(actions) {
				console.log(actions);
				JsonStore.loadData({ 'actions': actions });
				
				// callback
				if(callback != null && typeof callback == 'function') callback();
			});
		}
		
		function getResponseActionOptions(manager, callback) { // grid columns init
			channel.send(1, 'org.krakenapps.siem.msgbus.ResponsePlugin.getResponseActionOptions',
				{
					"manager": manager,
					"locale": programManager.getLocale()
				},
				function(resp) {
					Array.prototype.push.call(Stores, manager); // array like object : for using 'length' property
					var s = Stores[manager] = {};
					s['fields'] = ['name'];
					s['columns'] = [{
						dataIndex: 'name',
						header: 'Name'
					}];
					
					$.each(resp.options, function(idx, option) {
						s['fields'].push(option.name);
						s['columns'].push({
							dataIndex: option.name,
							header: option.display_name
						});
					});
					
					s['fields'].push('description');
					s['columns'].push({
						dataIndex: 'description',
						header: 'Description'
					});
					
					if(callback != null && typeof callback == 'function') callback();
				}
			);
		};
		var initStoreFields = getResponseActionOptions;
		
		function getResponseActions(manager, callback) { // tabs init
			channel.send(1, 'org.krakenapps.siem.msgbus.ResponsePlugin.getResponseActions', 
				{
					"manager": manager
				},
				function(resp) {
					if(callback != null && typeof callback == 'function') callback(resp.actions);
				}
			);
		}
		
		var MainUI = new Ext.TabPanel({
			listeners: {
				beforerender: getResponseManagers,
				add: function(tp, cmp, idx) {
					if (cmp.ownerCt.getXType() == "tabpanel") {
						tp.tabs[cmp.title] = cmp;
					}
				}
			},
			items: [ ],
			border: false,
			tabs: { }
		});
		
		var window = windowManager.createWindow(pid, this.name, 770, 300, MainUI);

	}

	this.onstop = function() {
	}
}


processManager.launch(new Response());