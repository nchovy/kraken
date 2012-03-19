Account.CreateUser = function(parentWin, ouid, callback) {
	var that = this;
	
	Ext.QuickTips.init(); // for invalid text tooltip
	
	var params = new HashTable(
		{
			'key': 'org_unit_id',
			'value': ouid,
			'readonly': true
		},
		'name', 'login_name', 'description', 'title', 'email', 'phone'
	);
	
	var params_bind = new Binding(params);
	/*
	params.observer.addListener('setValue', function(d) {
		console.log('setValue occured!!');
		console.log(d);
	});
	params.setValue('name', 'newbie');
	*/
	
	params.observer.addListener('setValue', function(d) {
		var login_name = params.getValue('login_name');
		var name = params.getValue('name');
		
		if(login_name == "" || login_name == null || name == "" || name == null) {
			btnCreate.disable();
		}
		else {
			btnCreate.enable();
		}
	});
	
	var btnCreate;
	
	function createUser() {
		channel.send(1, 'org.krakenapps.dom.msgbus.UserPlugin.createUser',
			params.get(),
			function(resp) {
				//resp.new_id
				windowNewUser.destroy();
				
				if(callback != null && typeof callback == 'function') {
					callback();
				}
			}
		);
	}

	var panel = new Ext.Panel({
		frame: true,
		border: false,
		bodyStyle: 'padding: 8px',
		defaults: {
			border: false,
			bodyStyle: 'padding: 7px',
		},
		items: [
		{
			xtype: 'form',
			labelWidth: 80,
			style: 'border-bottom: 1px dotted #ccc; padding-bottom: 7px',
			items: [
				params_bind.login_name = new Ext.form.TextField({
					fieldLabel: 'Login Name',
					name: 'login_name',
					anchor: '85%',
					allowBlank: false
				}),
				params_bind.name = new Ext.form.TextField({
					fieldLabel: 'Name',
					name: 'name',
					anchor: '85%',
					allowBlank: false
				})
			]
		},
		{
			xtype: 'form',
			labelWidth: 80,
			style: 'border-top: 1px dotted #fff; padding-top: 10px',
			items: [
				params_bind.title = new Ext.form.TextField({
					fieldLabel: 'Title',
					name: 'title',
					anchor: '99%'
				}),
				params_bind.email = new Ext.form.TextField({
					fieldLabel: 'E-mail',
					name: 'email',
					anchor: '99%',
					vtype: 'email'
				}),
				params_bind.phone = new Ext.form.TextField({
					fieldLabel: 'Phone',
					name: 'phone',
					anchor: '99%'
				})
			]
		},
		{
			xtype: 'form',
			labelAlign: 'top',
			items: [
				params_bind.description = new Ext.form.TextArea({
					xtype: 'textarea',
					fieldLabel: 'Description',
					anchor: '99%'
				})
			]
		}
		],
		buttons: [
			btnCreate = new Ext.Button({
				text: 'Create',
				disabled: true,
				handler: function() { 
					createUser();
				}
			}),
			{
				xtype: 'button',
				text: 'Cancel',
				handler: function() { windowNewUser.destroy(); },
				style: 'margin-right: 10px'
			}
		]
	});

	var windowNewUser = windowManager.createChildWindow({
		title: 'New User',
		width: 380,
		height: 365,
		items: panel,
		parent: parentWin,
		modal: true,
		maximizable: false,
		resizable: false
	});
	
	params_bind.bind();
}