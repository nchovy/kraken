Account.CreateProfile = function(config) {
	Ext.apply(this, config);
	var pid = this.parent.pid;
	var that = this;
	
	var info_connect = new HashTable('dc', 'account', 'password');
	var info_connect_bind = new Binding(info_connect);
	
	info_connect.observer.addListener('setValue', function() {
		var filled = true;
		var btn = that.createBtnNext;
		var obj = info_connect.get();
		for(prop in obj) {
			if(obj[prop] == null || obj[prop] == '') {
				filled = false;
			}
		}
		
		if(filled) 
			btn.enable();
		else 
			btn.disable();
	});
	
	function testConnection() {
		setTimeout(function() { 
			that.createBtnPrev.disable();
		}, 10);
		that.lblConnecting.setText('<img src="img/ajax-loader.gif" style="margin-bottom: -2px"> Connecting...', false);
		
		var btn = that.createBtnNext;
		btn.disable();
		
		channel.send(1, 'org.krakenapps.ldap.msgbus.LdapPlugin.testConnection',
			info_connect.get(),
			function(resp) {
				that.lblConnecting.setText('<img src="img/badge-circle-check-16-ns.png" style="margin-bottom: -2px"> Connected!', false);
				that.lblConnectionInfo.setText('');
				
				setTimeout(function() {
					btn.fireEvent('click', btn);
					that.profile_name.setValue(namingProfile(info_connect.getValue('dc')));
				}, 500);
			},
			function(resp) {
				console.log(resp);
				that.lblConnecting.setText('<img src="img/badge-circle-cross-16-ns.png" style="margin-bottom: -2px"> Connection Failed!', false);
				that.lblConnectionInfo.setText('Cannot connect to <b>' + info_connect.getValue('dc') + '</b>. Click <span style="border: 1px solid #aaa; padding: 3px; -webkit-border-radius: 3px; background-image: -webkit-gradient(linear,left bottom,left top,color-stop(0.02, rgb(224,224,224)),color-stop(0.52, rgb(255,255,255)));">&nbsp;&laquo; Previous&nbsp;</span> and check connection informations you entered.', false);
				
				function oneStepBack() {
					var step = that.createWiz.getCurrent().getStep();
					that.createWiz.goStep(step - 1);
					this.disable();
					
					that.createBtnPrev.un('click', oneStepBack);
					that.createBtnNext.enable();
				}
				that.createBtnPrev.on('click', oneStepBack);
				setTimeout(function() { 
					that.createBtnPrev.enable();
				}, 20);
			}
		);
	}
	
	function namingProfile(name) {
		var isExist = false;
		var ret = name;
		
		if(that.profiles != null) {
			Ext.each(that.profiles, function(p) {
				if(p.name == name) {
					isExist = true;
					ret = name.toString().addNum();
					return;
				}
			});
		}
		
		if(!isExist) return ret;
		
		return namingProfile(ret);
	}
	
	var step_connect = new WizardPanel({
		step: 0,
		layout: 'form',
		defaults: {
			width: 240
		},
		labelWidth: 95,
		items: [
			{
				xtype: 'label',
				text: 'Connect to Active Directory',
				height: 32,
				style: 'font-size: 1.3em; font-weight: bold; display: block'
			},
			{
				xtype: 'label',
				text: 'Enter connection informations of Active Directory to which you want to connect.',
				width: 350,
				height: 32,
				style: 'display: block'
			},
			info_connect_bind.dc = new Ext.form.TextField({
				fieldLabel: 'DC (Connect to)',
				emptyText: 'sub.domain.com'
			}),
			info_connect_bind.account = new Ext.form.TextField({
				fieldLabel: 'Account'
			}),
			info_connect_bind.password = new Ext.form.TextField({
				fieldLabel: 'Password',
				inputType: 'password'
			})
		]
	});
	
	// Set Logger Options for Creating Logger
	var step_verify = new WizardPanel({
		step: 1,
		layout: 'form',
		items: [
			that.lblConnecting = new Ext.form.Label({
				html: '&nbsp;',
				height: 32,
				style: 'font-size: 1.3em; font-weight: bold; display: block'
			}),
			that.lblConnectionInfo = new Ext.form.Label({
				html: ' ',
				height: 32,
				width: 350,
				style: 'display: block'
			})
		],
		listeners: {
			activate: testConnection
		}
	});
	
	var step_saveprofile = new WizardPanel({
		step: 2,
		layout: 'form',
		items: [
			{
				xtype: 'label',
				text: 'Save this connection information to Profile',
				height: 32,
				style: 'font-size: 1.3em; font-weight: bold; display: block'
			},
			that.profile_name = new Ext.form.TextField({
				fieldLabel: 'Profile Name',
				listeners: {
					change: function(t) {
						if(t.getValue() == '') 
							that.createBtnNext.disable();
						else
							that.createBtnNext.enable();
					}
				}
			})
		],
		listeners: {
			activate: function() { 
				setTimeout(function() {
					that.profile_name.selectText();
				}, 10);
				that.createBtnNext.enable();
			}
		}
	});
	
	function syncThisProfile() {
		var o = {
			name: that.profile_name.getValue(),
			dc: info_connect.getValue('dc'),
			account: info_connect.getValue('account')
		}
		
		that.callbackSync(o);
		wizard.close();
	}
	
	function createProfile(callback) {
		var obj = info_connect.get();
		obj['profile_name'] = that.profile_name.getValue();
		channel.send(1, 'org.krakenapps.ldap.msgbus.LdapPlugin.createProfile', obj,
			function(resp) {
				console.log(resp);
				that.btnSync.enable();
				
				if(callback != null) callback();
			},
			function(resp) {
				console.log(resp);
				Ext.MessageBox.show({
					title: 'Create Profile',
					width: 280,
					msg: 'An error occured! It cannot create profile.<br/>Check browser console.',
					icon: Ext.MessageBox.ERROR,
					buttons: Ext.MessageBox.OK,
					fn: function() {
						wizard.close();
					}
				});
			}
		);
	}
	
	var step_creating = new WizardPanel({
		step: 3,
		layout: 'form',
		items: [
			{
				xtype: 'label',
				html: '<img src="img/badge-circle-check-16-ns.png" style="margin-bottom: -2px"> Profile Saved!',
				height: 32,
				style: 'font-size: 1.3em; font-weight: bold; display: block'
			},
			that.btnSync = new Ext.Button({
				text: 'Sync this Profile',
				handler: syncThisProfile,
				disabled: true,
				width: 160,
				iconCls: 'ico-profilego',
				style: 'margin-left: 100px' 
			})
		],
		listeners: {
			activate: function() {
				createProfile(function() {
					if(that.forceSync) {
						syncThisProfile();
					}
				});
			}
		}
	});

	
	// CreateProfile UI
	var CreateProfileUI = new Ext.Panel({
		layout: 'border',
		border: false,
		defaults: { border: false },
		items: [
			{
				region: 'west',
				width: 0,
				html: 'Create'
			},
			that.createWiz = new WizardContainer({
				items: [step_connect, step_verify, step_saveprofile, step_creating]
			})
		],
		bbar: [
			'->',
			that.createBtnPrev = new Ext.Button({
				text: '&nbsp;&laquo; Previous&nbsp;',
				disabled: true,
				handler: function() {
					var step = that.createWiz.getCurrent().getStep();
					that.createWiz.goStep(step - 2);
					this.disable();
				}
			}),
			that.createBtnNext = new Ext.Button({
				text: '&nbsp;Next &raquo;&nbsp;',
				disabled: true,
				listeners: {
					click: function() {
						var step = that.createWiz.getCurrent().getStep();
						
						if(that.createWiz.getCurrent().isLast()) {
							wizard.close();
						}
						else {
							that.createWiz.goStep(step + 1);
							that.createBtnPrev.enable();
							
							if(that.createWiz.getCurrent().isLast()) {
								this.setText('&nbsp;Finish&nbsp;');
								that.createBtnPrev.disable();
							}
						}
					}
				}
			})
		]
	});
	
	var wizard = windowManager.createChildWindow({
		title: 'Profile Wizard',
		width: 390,
		height: 270,
		items: {
			id: 'wizard' + pid,
			layout: 'card',
			border: false,
			defaults: { border: false },
			region: 'center',
			items: [CreateProfileUI],
			activeItem: 0
		},
		parent: that.parent,
		modal: true,
		maximizable: false,
		resizable: false,
		listeners: {
			beforeclose: function() {
				if(that.callback != null) 
					that.callback();
			}
		}
	});
	
	var wizlay = Ext.getCmp('wizard' + pid).getLayout();
	
	info_connect_bind.bind();
}