CAManager = function() {
	this.name = "Certificate Authority";
	
	this.onstart = function(pid, args) {
		var TreeRootCert = new Ext.tree.AsyncTreeNode({ text: 'Root Certificates', expanded: true, children: [] })
		
		function getRootCertificates(callback) {
			channel.send(1, 'org.krakenapps.ca.msgbus.CaPlugin.getRootCertificates', {},
				function(resp) {
					var certlist = [];
					
					$.each(resp.root_certs, function(idx, cert) {
						certlist.push({
							text: cert.subject,
							leaf: false,
							detail: cert,
							type: 'root_cert',
							iconCls: 'ico-rootcert'
						});
					});
					
					var tree = Ext.getCmp('treeRootCert' + pid);
					tree.setRootNode(new Ext.tree.AsyncTreeNode({
						text: 'Root Certificates',
						expanded: true,
						children: certlist
					}));
					
					if(callback != null && typeof callback == 'function') callback();
					else tree.getSelectionModel().select(tree.getRootNode().firstChild);
				}
			);
			
		}
		
		function getCertificates(node, callback) {
			var cn = node.attributes.text.match(/CN=\w+/)[0].replace('CN=', '');
			console.log(cn);
			
			channel.send(1, 'org.krakenapps.ca.msgbus.CaPlugin.getCertificates', 
				{
					"ca_common_name": cn
				},
				function(resp) {
					var certs = [];
					
					$.each(resp.certs, function(idx, cert) {
						certs.push({
							text: cert,
							leaf: true,
							type: 'cert',
							iconCls: 'ico-cert',
							cn: cn
						});
					});
					
					if(callback != null) callback(certs);
				}
			);
		}
		
		function getCertificate(cn, alias, password, callback, error_callback) {
			channel.send(1, 'org.krakenapps.ca.msgbus.CaPlugin.getCertificate', 
				{
					"ca_common_name": cn,
					"key_alias": alias,
					"key_password": password
				},
				function(resp) {
					if(!resp.hasOwnProperty('cert')) {
						if(error_callback != null) error_callback();
					}
					else {
						if(callback != null) callback(resp.cert);
					}
				}
			);
		}
		
		function getPfxFile(cn, alias, password) {
			channel.send(1, 'org.krakenapps.ca.msgbus.CaPlugin.getPfxFile',
				{
					"ca_common_name": cn,
					"key_alias": alias,
					"key_password": password 
				},
				function(resp) {
					console.log(resp.pfx);
					console.error('download does not working on webkit');
				}
			);
		}
		
		function issueRootCertificate() {
			var root = tree.getRootNode();
			var node = root.appendChild(new Ext.tree.TreeNode({
				iconCls: 'ico-rootcert',
				text: '(new Root Certificate)'
			}));
			tree.getSelectionModel().select(node);
			
			contents.layout.setActiveItem(2);
			tree.disable();
			createtitle.setText('Create Root Certificate');
			CreatePropertyGrid.setSource({
				"days": 0,
				"common_name": "",
				"org_unit": "", 
				"org": "", 
				"city":"",
				"state":"",
				"country": "KR", 
				"signature_algorithm": "SHA512withRSA", 
				"password":""
			});
			
			
			btnSave.handler = function() {
				channel.send(1, 'org.krakenapps.ca.msgbus.CaPlugin.issueRootCertificate',
					CreatePropertyGrid.getSource(),
					function(resp) {
						tree.enable();
						getRootCertificates();
					}
				);
			}
			
		}
		
		function issueCertificate() {
			var selected = tree.getSelectionModel().getSelectedNode();
			var parent, node;
			
			if(selected == null) return;
			if(selected.attributes.type == 'root_cert') {
				parent = selected;
			}
			else if(selected.attributes.type == 'cert') {
				parent = selected.parentNode;
			}
			else { return; }
			
			parent.expand();
			node = parent.appendChild(new Ext.tree.TreeNode({
				iconCls: 'ico-cert',
				text: '(new Certificate)'
			}));
			
			function select() {
				try {
					tree.getSelectionModel().select(node)
				}
				catch (e) {
					setTimeout(select, 5);
				}
			}
			select();
			
			var cn = parent.attributes.text.match(/CN=\w+/)[0].replace('CN=', '');
			
			contents.layout.setActiveItem(2);
			tree.disable();
			createtitle.setText('Create Certificate');
			CreatePropertyGrid.setSource({
				"ca_common_name": cn,
				"ca_password": "",
				"key_alias": "",
				"common_name": "",
				"org_unit": "",
				"org": "",
				"city": "",
				"state": "", 
				"country": "KR",
				"signature_algorithm": "SHA512withRSA",
				"days": 0,
				"password": ""
			});
			
			btnSave.handler = function() {
				channel.send(1, 'org.krakenapps.ca.msgbus.CaPlugin.issueCertificate',
					CreatePropertyGrid.getSource(),
					function(resp) {
						tree.enable();
						getRootCertificates();
					}
				);
			}
		}
		
		function showCert() {
			var sel = Ext.getCmp('treeRootCert' + pid).getSelectionModel().selNode.attributes;
			var pwd = Ext.getCmp('password' + pid);
			var alias = sel.text.split('.')[0];
			
			getCertificate(sel.cn, alias, pwd.getValue(), function(resp) {
				contents.layout.setActiveItem(0);
				proptitle.setText(sel.text);
				PropertyGrid.setSource(resp);
				btnGetPFX.enable();
				btnGetPFX.profileForGetBinary = {
					'cn' : sel.cn,
					'alias': alias,
					'pwd' : pwd.getValue()
				}
			},
			function() {
				pwd.markInvalid('Password does not matched.')
			});
		}
		
		// define layout
		var PropertyGrid = new Ext.grid.PropertyGrid({
			id: 'gridCertificates' + pid,
			tbar: {
				xtype: 'toolbar',
				items: [
					{
						xtype: 'tbtext',
						id: 'tbtitle' + pid,
						text: '&nbsp;',
						style: {
							'font-weight': 'bold'
						}
					},
					'->',
					{
						xtype: 'button',
						id: 'btnGetPFX' + pid,
						text: 'Get PFX',
						iconCls: 'ico-keyreport',
						disabled: true,
						handler: function(c) {
							var p = c.profileForGetBinary;
							getPfxFile(p.cn, p.alias, p.pwd);
						}
					}
				]
			}
		});
		
		var comboSignatureAlgorithm = new Ext.form.ComboBox({
			fieldLabel: 'signature algorithm',
			name: 'signature_algorithm',
			allowBlank: false,
			store: ['SHA512withRSA', 'SHA512'],
			typeAhead: true,
			mode: 'local',
			triggerAction: 'all',
			emptyText: 'Select signature algorithm',
			selectOnFocus: true
		});
		
		var comboCountry = new Ext.form.ComboBox({
			fieldLabel: 'Country',
			name: 'country',
			allowBlank: false,
			store: ['KR', 'US', 'CN'],
			typeAhead: true,
			mode: 'local',
			triggerAction: 'all',
			emptyText: 'Select Country',
			selectOnFocus: true
		});
		
		var txtPassword = new Ext.form.TextField({
			inputType: 'password',
			allowBlank: false
		});
		
		var txtCaPassword = new Ext.form.TextField({
			inputType: 'password',
			allowBlank: false
		});
		
		var txtReadonly = new Ext.form.TextField({
			readOnly: true
		});
		
		function passwordRenderer(v) {
			var ret = ''
			for(var i=0; i < v.length; i++) {
				ret += '*';
			}
			return ret;
		}
		
		function exampleRenderer(v, a, b) {
			function e(r) {
				return '<span style="color: #999">' + r + '</span>';
			}
			
			var ret = "";
			
			if(v == "") {
				switch(b.id) {
					case "key_alias": 
						ret =  e("xeraph"); break;
					case "common_name":
						ret =  e("xeraph"); break;
					case "org_unit": 
						ret =  e("TSG"); break;
					case "org": 
						ret =  e("FutureSystems"); break;
					case "city":
						ret =  e("Guro"); break;
					case "state": 
						ret =  e("Seoul"); break;
					case "common_name":
						ret =  e("Frodo"); break;
				}
			}			
			else {
				ret = v;
			}
			
			return ret;
		}
		
		var CreatePropertyGrid = new Ext.grid.PropertyGrid({
			id: 'gridCreateCertificates' + pid,
			customEditors: {
				'ca_common_name': new Ext.grid.GridEditor(txtReadonly),
				'signature_algorithm': new Ext.grid.GridEditor(comboSignatureAlgorithm),
				'password': new Ext.grid.GridEditor(txtPassword),
				'ca_password': new Ext.grid.GridEditor(txtCaPassword),
				'country': new Ext.grid.GridEditor(comboCountry)
			},
			customRenderers: {
				'ca_password': passwordRenderer,
				'password': passwordRenderer,
				'ca_common_name': exampleRenderer,
				'key_alias': exampleRenderer,
				'common_name': exampleRenderer,
				'org_unit': exampleRenderer,
				'org': exampleRenderer,
				'city': exampleRenderer,
				'state': exampleRenderer,
			},
			tbar: {
				xtype: 'toolbar',
				items: [
					{
						xtype: 'tbtext',
						id: 'tbcreatetitle' + pid,
						text: 'Create',
						style: {
							'font-weight': 'bold'
						}
					}
				]
			},
			buttons: [
				{
					xtype: 'button',
					id: 'btnSave' + pid,
					text: 'Save'
				},
				{
					xtype: 'button',
					id: 'btnCancel' + pid,
					text: 'Cancel',
					handler: function() {
						contents.layout.setActiveItem(0);
						tree.enable();
						var node = tree.getSelectionModel().getSelectedNode();
						node.destroy();
					}
				}
			]
		});


		var Panel = new Ext.FormPanel({
			labelAlign: 'top',
			frame: false,
			items: [{
				layout: 'column',
				border: false,
				defaults: {
					border: false,
				},
				items: [{
					columnWidth: .3,
					layout: 'form',
					items: [
						{
							xtype: 'panel',
							html: '&nbsp;',
							border: false,
						}
					]
				},
				{
					columnWidth: .4,
					layout: 'form',
					items: [{
						id: 'password' + pid,
						xtype: 'textfield',
						fieldLabel: '<br/>If you want to show this certificate, Please enter password below',
						name: 'last',
						anchor: '95%',
						inputType: 'password',
						listeners: {
							specialkey: function(field, e) {
								if(e.getKey() == e.ENTER) {
									showCert();
								}
							}
						}
					}]
				},
				{
					columnWidth: .3,
					layout: 'form',
					items: [
						{
							xtype: 'panel',
							html: '&nbsp;',
							border: false,
						}
					]
				}]
			},],
			tbar: {
				xtype: 'toolbar',
				items: [
					{
						xtype: 'tbtext',
						id: 'pnltitle' + pid,
						text: '&nbsp;',
						style: { 'font-weight' : 'bold' }
					}
				]
			},
			buttons: [{
				id: 'btnShowCert' + pid,
				text: 'Show Certificate',
				handler: showCert
			}]
		});
		
		// Layout
		MyWindowUi = Ext.extend(Ext.Panel, {
			layout: 'border',
			border: false,
			frame: false,
			listeners: {
				render: getRootCertificates
			},
			initComponent: function() {
				this.items = [
					{
						xtype: 'treepanel',
						id: 'treeRootCert' + pid,
						region: 'north',
						height: 120,
						split: true,
						root: TreeRootCert,
						tbar: {
							xtype: 'toolbar',
							items: [
								{
									xtype: 'button',
									iconCls: 'ico-addbook',
									text: 'Issue Root Certificate',
									handler: issueRootCertificate
								},
								{
									xtype: 'button',
									iconCls: 'ico-addpage',
									text: 'Issue Certificate',
									handler: issueCertificate
								}
							]
						},
						loader: new Ext.tree.TreeLoader({
							directFn: function(nodeid, callback, node) {
								console.log(node) // 이게 바로 kraken.js에서 TreeLoader를 override 해서 뽑아낸 부분
								getCertificates(node, function(resp) {
									callback(resp, {status: true});
								});
							},
						}),
						listeners: {
							render: function(tp) {
								tp.getSelectionModel().on('selectionchange', function(tree, node) {
									btnGetPFX.disable();
									if(node == null) {
										tp.getSelectionModel().select(tp.getRootNode().firstChild);
										return true;
									}
									
									var a = node.attributes;
									if(a.hasOwnProperty('type')) {
										var t = a.type;
										if(t == 'root_cert') {
											contents.layout.setActiveItem(0);
											proptitle.setText(a.text);
											PropertyGrid.setSource(a.detail);
										}
										else if(t == 'cert') {
											contents.layout.setActiveItem(1);
											
											Ext.getCmp('password' + pid).setValue('');
											pnltitle.setText(a.text);
											//PropertyGrid.setSource(a.detail);
										}
									}
								});
							}
						}
					},
					{
						id: 'panelContent' + pid,
						region: 'center',
						layout: 'card',
						activeItem: 0,
						border: false,
						items: [PropertyGrid, Panel, CreatePropertyGrid]
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
		
		var caWindow = windowManager.createWindow(pid, this.name ,800,400, cmp1);
		cmp1.show();
		
		var tree = Ext.getCmp('treeRootCert' + pid);
		var contents = Ext.getCmp('panelContent' + pid);
		var pnltitle = Ext.getCmp('pnltitle' + pid);
		var proptitle = Ext.getCmp('tbtitle' + pid);
		var createtitle = Ext.getCmp('tbcreatetitle' + pid);
		var btnGetPFX = Ext.getCmp('btnGetPFX' + pid);
		var btnSave = Ext.getCmp('btnSave' + pid);
	}
	
	this.onstop = function() {
		
	}
}

processManager.launch(new CAManager()); 