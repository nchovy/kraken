Account.LdapProfile = function(config) {
	Ext.apply(this, config); // parent, profiles, callbackSync
	var that = this;
	
	// init stores
	var ProfileStore = new Ext.data.JsonStore({
		fields: [ 'name', 'dc', 'account' ],
	});
	
	ProfileStore.loadData(that.profiles);
	
	function refresh() {
		channel.send(1, 'org.krakenapps.ldap.msgbus.LdapPlugin.getProfiles', {},
			function(resp) {
				console.log(resp);
				ProfileStore.removeAll();
				ProfileStore.loadData(resp.profiles);
				
				that.profiles = null;
				that.profiles = resp.profiles;
			}
		);
	}
	
	function removeProfile() {
		var selected = panel.items.items[0].getSelectionModel().getSelected();
		Ext.Msg.show({
			title: 'Remove Profile',
			msg: 'It will remove "' + selected.data.name + '"<br/>Are you sure?',
			buttons: Ext.Msg.OKCANCEL,
			fn: function(result,d,box) {
				
				if(result == "ok") {
					channel.send(1, 'org.krakenapps.ldap.msgbus.LdapPlugin.removeProfile', 
						{
							"profile_name": selected.data.name
						},
						function(resp) {
							var pop = Ext.MessageBox.show({ title: 'Remove Profile', width: 200, msg: 'Removed successfully!', closable: false });
							setTimeout(function() { pop.hide(); }, 1000);
							
							refresh();
						}
					);
				}
			},
			animEl: 'elId',
			icon: Ext.MessageBox.QUESTION
		});
	}

	var panel = new Ext.Panel({
		layout: 'border',
		border: false,
		defaults: {
			split: true
		},
		items: [
			new Ext.grid.GridPanel({
				region: 'center',
				store: ProfileStore,
				viewConfig: { forceFit: true },
				tbar: {
					xtype: 'toolbar',
					items: [
						{
							xtype: 'button',
							iconCls: 'ico-add',
							text: 'Add',
							handler: function() {
								new Account.CreateProfile({
									parent: windowNewUser,
									forceSync: false,
									profiles: that.profiles,
									callbackSync: function(o) {
										that.callbackSync(o);
										windowNewUser.close();
									},
									callback: function() {
										refresh();
									}
								});
							}
						},
						{
							xtype: 'button',
							iconCls: 'ico-remove',
							handler: removeProfile
						},
						{
							xtype: 'button',
							iconCls: 'ico-refresh',
							handler: refresh
						}
					]
				},
				columns: [
					{
						xtype: 'gridcolumn',
						dataIndex: 'name',
						header: 'Name'
					},
					{
						xtype: 'gridcolumn',
						dataIndex: 'dc',
						header: 'DC'
					},
					{
						xtype: 'gridcolumn',
						dataIndex: 'account',
						header: 'Account'
					}
				]
			})
		]
	});

	var windowNewUser = windowManager.createChildWindow({
		title: 'Manage Profile',
		width: 450,
		height: 200,
		items: panel,
		parent: that.parent,
		modal: true,
		maximizable: false,
		resizable: true,
		listeners: {
			beforeclose: function() {
				if(that.callback != null) 
					that.callback();
			}
		}
	});
}