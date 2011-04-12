Account = function() {
    this.name = "Account";

    this.onstart = function(pid, args) {
		programManager.loadJS('/core/account_create_user.js');
		programManager.loadJS('/core/account_ldap_profile.js');
		programManager.loadJS('/core/account_create_profile.js');
		
		var that = this;
		this.toolbar = {};
		
		var ou_rawdata;
		
		var TreeBlank = new Ext.tree.AsyncTreeNode({ text: '(domain)', expanded: true, children: [] });
		
		// init stores
		var UserStore = new Ext.data.JsonStore({
			fields: [ 'created_at', 'description', 'email', 'id', 'dc', 'login_name', 'name', 'ou_id', 'phone', 'updated_at', 'title' ],
			root: 'result'
		});
		
		var initUser = { result: [] };
		UserStore.loadData(initUser);
		
		function makeOUTree(resp) {
			var mainnode = [];
			
			function makeNode(ou) {
				return {
					leaf: false,
					children: [],
					text: ou.name,// + '(' + ou.id + ')',
					nid: ou.id,
					json: ou,
					expanded: true,
					listeners: {
						click: function() {
							getUsers(ou.id);
						}
					}
				}
			}
			
			function findParent(parent, id) {
				var found = { has: false };
				$.each(parent, function(idx, node) {
					if(node.nid == id) {
						found['node'] = node;
						found['has'] = true;
						return;
					}
					
					if(!found.has) {
						found = findParent(node.children, id);
						if(found.has) {
							return;
						}
					}
				});
				
				return found;
			}
			
			function makeTree(result) {
				var residue = [];
				$.each(result, function(idx, ou) {
					if(ou['parent_id'] == null) {
						var node = makeNode(ou);
						mainnode.push(node);
						
						return;
					}
					
					var found = findParent(mainnode, ou.parent_id);
					
					if(found.has) {
						var node = makeNode(ou);
						found.node.children.push(node);
						
						return;
					}
					else {
						residue.push(ou);
					}
				});
				
				return residue;
			}
			
			var r = makeTree(resp.result);
			while(r.length > 0) {
				r = makeTree(r);
				if(r.length == 0) 
					break;
			}
			
			return mainnode;
		}
		
		function getOrganizationUnits() {
			channel.send(1, 'org.krakenapps.dom.msgbus.OrganizationUnitPlugin.getOrganizationUnits', {},
				function(resp) {
					ou_rawdata = resp.result;

					/** start making tree **/
					var mainnode = makeOUTree(resp);
					/** end making tree **/
					
					var ou = {
						text: (mainnode.length > 0) ? mainnode[0].json.dc : '(no OU)',
						expanded: true,
						children: mainnode,
						listeners: {
							click: function() {
								getUsers();
							}
						}
					}
					
					var root = that.treeOrganization.setRootNode(new Ext.tree.AsyncTreeNode({
						text: '(domain)',
						expanded: true,
						children: (mainnode.length > 0) ? [ou] : [],
						listeners: {
							click: function() {
								getUsers();
							}
						}
					}));
					
					var sm = that.treeOrganization.getSelectionModel();
					sm.clearSelections(true);
					sm.select(root);
					
					try { 
						root.fireEvent('click', Ext.emptyFn);
					}
					catch (err) {}
				}
			);
		}
		
		function getCurrentOUID() {
			var attr = that.treeOrganization.getSelectionModel().selNode.attributes;
			if(attr.hasOwnProperty('nid')) {
				return attr.nid;
			}
			else {
				return null;
			}
		}
		
		function getOUName(id) {
			var name;
			$.each(ou_rawdata, function(idx, ou) {
				if(ou.id == id) {
					name = ou.name;
					return;
				}
			});
			return name;
		}
		
		function getUsers(ou_id) {
			channel.send(1, 'org.krakenapps.dom.msgbus.UserPlugin.getUsers',
				(ou_id != null) ? { 'ou_id': ou_id, 'inc_children': true } : {},
				function(resp) {
					UserStore.removeAll();
					UserStore.loadData(resp);
				}
			);
		}
		
		
		function removeUser(selected) {
			Ext.Msg.show({
				title:'Remove User',
				width: 250,
				msg: 'It will remove "' + selected.data.name + '"<br/>Are you sure?',
				buttons: Ext.Msg.OKCANCEL,
				fn: function(c,d,e) {
					
					if(c == "ok") {
						channel.send(1, 'org.krakenapps.dom.msgbus.UserPlugin.removeUser',
							{
								'id' : selected.id,
							}, 
							function(resp) {
								var pop = Ext.MessageBox.show({ title: 'Remove User', width: 200, msg: 'Removed successfully!', closable: false });
								setTimeout(function() { pop.hide(); }, 1000);
								
								getUsers(getCurrentOUID());
							},
							Ext.ErrorFn
						)
					}
					else {
					}
					
				},
				animEl: 'elId',
				icon: Ext.MessageBox.QUESTION
			});
		}
		
		function createOrganizationUnit(parent) {
			var tree = that.treeOrganization;
			
			var node = parent.appendChild(new Ext.tree.TreeNode({
			}));
			
			console.log(parent);
			
			parent.expand(true, false);
			tree.getSelectionModel().select(node);
			
			var ge = new Ext.tree.TreeEditor(tree, {}, {
				beforeNodeClick: Ext.emptyFn,
				blankText: 'A organization unit name is required',
				selectOnFocus:true,
				completeOnEnter: true,
				listeners: {
					cancelEdit: function(ed, val, startval) {
						parent.removeChild(node, false);
						ge.destroy();
						tree.getSelectionModel().select(tree.getRootNode());
					},
					complete: function(ed, val, startval) {
						if(ge.getValue() == "") {
							parent.removeChild(node, false);
							ge.destroy();
							createOrganizationUnit(parent);
							return;
						}
						
						channel.send(1, 'org.krakenapps.dom.msgbus.OrganizationUnitPlugin.createOrganizationUnit',
							{
								"name": val,
								"dc": null,
								"org_id": 1,
								"parent_id": parent.attributes.nid
							},
							function(resp) {
								var pop = Ext.MessageBox.show({ title: 'Create Organization Unit', msg: 'Organization Unit "' + val + '" Created!', width: 250, closable: false });
								setTimeout(function() { pop.hide(); }, 1000);
								
								var attr = node.attributes;
								attr.leaf = false;
								attr.children = [];
								attr.nid = resp.new_id;
								node.on('click', function() {
									getUsers(this.attributes.nid);
								});
								
							},
							function(resp) {
								Ext.Msg.alert('Create Organization Unit', 'An error occured!<br/>Please check browser console.', function() {
									parent.removeChild(node, false);
									ge.destroy();
								});
							}
						);
					}
				}
			});
			
			setTimeout(function() {
				ge.editNode = node;
				ge.startEdit(node.ui.textNode);
			}, 10);
		}
		
		function removeOrganizationUnit() {
			var name = getOUName(getCurrentOUID());
			Ext.Msg.show({
				title: 'Remove Organization Unit',
				msg: 'It will remove "' + name + '"<br/>Are you sure?',
				buttons: Ext.Msg.OKCANCEL,
				fn: function(result,d,box) {
					
					if(result == "ok") {
						channel.send(1, 'org.krakenapps.dom.msgbus.OrganizationUnitPlugin.removeOrganizationUnit', 
							{
								"id": getCurrentOUID()
							},
							function(resp) {
								var node = that.treeOrganization.getSelectionModel().selNode;
								var parent = node.parentNode;
								parent.removeChild(node, false);
								
								var pop = Ext.MessageBox.show({ title: 'Remove Organization Unit', width: 200, msg: 'Removed successfully!', closable: false });
								setTimeout(function() { pop.hide(); }, 1000);
							},
							function(resp) {
								console.log(resp);
								Ext.MessageBox.show({
									title: 'Remove Organization Unit',
									width: 280,
									msg: 'Cannot remove ' + name + '.<br/>This OU has one or more people.',
									icon: Ext.MessageBox.ERROR,
									buttons: Ext.MessageBox.OK
								});
								// 그룹에 User가 있거나
								// Sync되었거나 
								// 해서 안지워짐...
							}
						);
					}
				},
				animEl: 'elId',
				icon: Ext.MessageBox.QUESTION
			});
		}
		
		function getAdmins() {
			channel.send(1, 'org.krakenapps.dom.msgbus.AdminPlugin.getAdmins', {},
				function(resp) {
					console.log(resp);
				}
			);
		}
		
		var checkedProfile;
		function getFullProfile() {
			return '<b>'+ checkedProfile.name + '</b> ('+ checkedProfile.dc + ', ' + checkedProfile.account +')';
		}
		
		function onProfileCheck(item, checked) {
			if(checked) {
				checkedProfile = item.profile;
			}
		}
		
		function checkLdap() {
			channel.send(1, 'org.krakenapps.ldap.msgbus.LdapPlugin.getProfiles', {},
				function(resp) {
					//console.log('support LDAP');
					var len = resp.profiles.length;
					
					function openLdapProfile() {
						that.LdapProfile = new Account.LdapProfile({
							parent: window, 
							profiles: resp.profiles, 
							callbackSync: function(o) {
								// callback sync dom
								checkedProfile = o;
								syncDom();
								checkLdap();
							},
							callback: function() {
								checkLdap();
							}
						});
					}
					
					var arrProfiles = [];
					Ext.each(resp.profiles, function(p) {
						arrProfiles.push({
							text: '<b>' + p.name + '</b> (' + p.dc + ', ' + p.account + ')',
							checked: false,
							group: 'profile',
							profile: p,
							checkHandler: onProfileCheck
						});
					});
					
					arrProfiles.push('-');
					arrProfiles.push({
						text: 'Manage Profiles...',
						handler: openLdapProfile
					});
					
					if(len > 0) {
						arrProfiles[0]['checked'] = true;
						checkedProfile = arrProfiles[0].profile;
					}
					
					var tbar = that.gridUser.getTopToolbar();
					tbar.remove(that.toolbar.fill, true);
					tbar.remove(that.toolbar.text, true);
					tbar.remove(that.toolbar.sync, true);
					//tbar.remove(that.toolbar.manage, true);
					
					that.toolbar.fill = tbar.addFill();
					that.toolbar.text = tbar.addText({
						text: 'LDAP&nbsp;'
					});
					that.toolbar.sync = tbar.addItem({
						iconCls: 'ico-profilego',
						text: 'Sync&nbsp;',
						xtype: (len > 0) ? 'splitbutton' : 'button',
						menu: (len > 0) ? arrProfiles : null,
						handler: (len > 0) ? syncDom : createProfile
					});
					
					/*that.toolbar.manage = tbar.addButton({
						text: 'Manage Profiles',
						handler: openLdapProfile
					});*/
					
					tbar.doLayout();
				},
				function(resp) {
					//console.log('doesn\'t support LDAP');
				}
			);
		}
		
		function syncDom() {
			console.log('---- syncDom ----');
			console.log(checkedProfile);
			channel.send(1, 'org.krakenapps.ldap.msgbus.LdapPlugin.getDomainUserAccounts',
				{
					"profile_name": checkedProfile.name
				},
				function(resp) {
					checkLdap();
					// init stores
					var DomainUserStore = new Ext.data.JsonStore({
						fields: [ 'account_name', 'display_name', 'department', 'distinguished_name' ],
						root: 'users'
					});
					
					DomainUserStore.loadData(resp);
					
					var gridDomainUser = new Ext.grid.GridPanel({
						xtype: 'grid',
						store: DomainUserStore,
						region: 'center',
						columns: [
							{
								xtype: 'gridcolumn',
								dataIndex: 'account_name',
								header: 'Login Name',
								sortable: true,
								width: 80
							},
							{
								xtype: 'gridcolumn',
								dataIndex: 'display_name',
								header: 'Name',
								sortable: true,
								width: 80
							},
							{
								xtype: 'gridcolumn',
								header: 'OU',
								dataIndex: 'department',
								sortable: true,
								width: 70
							},
							{
								xtype: 'gridcolumn',
								header: 'Distinguished Name',
								dataIndex: 'distinguished_name',
								sortable: true,
								width: 300
							}
						]
					});
					
					var dlg = new Ext.Window({
						autoCreate : true,
						title:'Sync',
						resizable:true,
						constrain:true,
						constrainHeader:true,
						minimizable : false,
						maximizable : false,
						stateful: false,
						modal: true,
						shim:true,
						buttonAlign:"center",
						width:600,
						minWidth: 390,
						plain:true,
						footer:true,
						closable:true,
						autoHeight: true,
						icon: Ext.MessageBox.QUESTION,
						items: new Ext.Panel({
							layout: 'border',
							border: false,
							frame: false,
							bodyStyle: 'background: none',
							height: 180,
							items: [
								{
									xtype: 'panel',
									region: 'north',
									frame: false,
									border: false,
									style: 'padding: 10px; height: 35px',
									bodyStyle: 'background: none',
									html:'<div class="ext-mb-icon ext-mb-question"></div> It will sync all organization units and accounts of '+ getFullProfile() + '<br/>Are you sure?'
								},
								gridDomainUser
							]
						}),
						fbar: new Ext.Toolbar({
							items: [
								new Ext.Button({
									text: 'Sync All',
									handler: function() {
										dlg.destroy();
										var pop = Ext.MessageBox.show({
											width: 200, 
											title: 'Syncing...', 
											wait: true, 
											waitConfig: {interval: 200}, 
											animEl: 'mb7', 
											msg: 'Syncing with profile ' + checkedProfile.name + ', Please wait...', 
											closable: false
										});
										
										channel.send(1, 'org.krakenapps.ldap.msgbus.LdapPlugin.syncDom',
											{
												"profile_name": checkedProfile.name
											},
											function(resp2) {
												setTimeout(function() { pop.hide(); }, 3000);
												var pop2 = Ext.MessageBox.show({ title: 'Sync', width: 200, msg: 'Sync successfully!', closable: false });
												setTimeout(function() { pop2.hide(); }, 1000);
												
												getOrganizationUnits();
											}
										);
									}
								}),
								new Ext.Button({
									text: 'Cancel',
									handler: function() {
										dlg.destroy();
									}
								})
							],
							enableOverflow: false
						})
					});
					dlg.render(document.body);
					dlg.getEl().addClass('x-window-dlg');
					dlg.body.dom.style.setProperty('padding', '2px');
					dlg.show();
				},
				function(resp) {
					console.log(resp);
					Ext.MessageBox.show({
						title: 'Sync Error',
						width: 350,
						msg: 'Cannot sync with ' + getFullProfile() + '.<br/>A password of this profile may be incorrect.',
						icon: Ext.MessageBox.ERROR,
						buttons: Ext.MessageBox.OK
					});
				}
			);
		}
		
		function createProfile() {
			new Account.CreateProfile({
				parent: window,
				forceSync: true,
				callbackSync: function(o) {
					checkedProfile = o;
					syncDom();
				}
			});
		}
		
		var MainUI = new Ext.Panel({
			layout: 'border',
			border: false,
			defaults: {
				split: true
			},
			items: [
				that.treeOrganization = new Ext.tree.TreePanel({
					region: 'west',
					width: 150,
					root: TreeBlank,
					listeners: {
						beforerender: getOrganizationUnits
					},
					bbar: {
						xtype: 'toolbar',
						items: [
							{
								xtype: 'button',
								iconCls: 'ico-add',
								handler: function() {
									createOrganizationUnit(that.treeOrganization.getSelectionModel().selNode);
								}
							}/*,
							{
								xtype: 'button',
								iconCls: 'ico-edit'
							}*/,
							{
								xtype: 'button',
								iconCls: 'ico-remove',
								handler: removeOrganizationUnit
							}
						]
					}
				}),
				{
					xtype: 'panel',
					layout: 'border',
					region: 'center',
					border: false,
					items: [
						that.gridUser = new Ext.grid.GridPanel({
							region: 'center',
							store: UserStore,
							listeners: { 
								beforerender: checkLdap
							},
							tbar: {
								xtype: 'toolbar',
								items: [
									{
										xtype: 'button',
										iconCls: 'ico-profile',
										text: 'View'
									},
									'-',
									{
										xtype: 'button',
										iconCls: 'ico-adduser',
										text: 'New User',
										handler: function() {
											that.CreateUser = new Account.CreateUser(window, getCurrentOUID(), function() {
												getUsers(getCurrentOUID());
											});
										}
									},
									{
										xtype: 'button',
										iconCls: 'ico-removeuser',
										handler: function() {
											var sel = that.gridUser.getSelectionModel().getSelected();
											removeUser(sel);
										}
									},
									'-',
									{
										xtype: 'button',
										iconCls: 'ico-refresh',
										handler: function() {
											//getUsers(getCurrentOUID());
											getOrganizationUnits();
										}
									}
								]
							},
							columns: [
								{
									xtype: 'gridcolumn',
									dataIndex: 'id',
									header: '#',
									width: 25
								},
								{
									xtype: 'gridcolumn',
									dataIndex: 'login_name',
									header: 'Login Name',
									width: 95
								},
								{
									xtype: 'gridcolumn',
									dataIndex: 'name',
									header: 'Name',
									width: 95
								},
								{
									xtype: 'gridcolumn',
									dataIndex: 'title',
									header: 'Title',
									width: 160
								},
								{
									xtype: 'gridcolumn',
									dataIndex: 'ou_id',
									header: 'OU',
									width: 90,
									renderer: function(v) {
										return getOUName(v);
									}
								},
								{
									xtype: 'gridcolumn',
									dataIndex: 'dc',
									header: 'DC',
									width: 130
								},
								{
									xtype: 'gridcolumn',
									dataIndex: 'email',
									header: 'E-mail',
									width: 130
								},
								{
									xtype: 'gridcolumn',
									dataIndex: 'phone',
									header: 'Phone',
									width: 120
								},
								{
									xtype: 'gridcolumn',
									dataIndex: 'description',
									header: 'Description',
									width: 160
								}
								//'created_at', 'description', 'email', 'id', 'ldap_auth_profile', 'login_name', 'name', 'ou_id', 'phone', 'updated_at'
							]
						})
					]
				}
			]
		});

		var window = windowManager.createWindow(pid, this.name, 720, 350, MainUI);
		
		//getAdmins();
	}

	this.onstop = function() {
	}
}

processManager.launch(new Account()); 