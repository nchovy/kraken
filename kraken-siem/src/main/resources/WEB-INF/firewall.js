Firewall = function() {
	this.name = "Firewall Control";
	
	this.onstart = function(pid, args) {
		Processed = {
			'Groups': [],
			'Instances': [],
			'GroupsWithInstancesForIPTab': [],
			'GroupsWithInstancesForGroupsTab': [],
			'Managers': [],
		};
		
		// Instances Tab
		function createInstance() {
			var cmpManagername = Ext.getCmp('treeManagers' + pid);
			var cmpInstancename = Ext.getCmp('txtInstanceName' + pid);
			
			var sm = cmpManagername.getSelectionModel();
			var type = sm.selNode.attributes.iconCls;
			var managername = sm.selNode.text;
			
			var hasInstances = sm.selNode.attributes.instances;
			
			if(cmpInstancename.getValue() == "") { 
				cmpInstancename.markInvalid("This field is required");
				return;
			}
			
			if(type == 'ico-manager') {
				channel.send(1, 'org.krakenapps.firewall.api.msgbus.FirewallPlugin.createInstance', 
					{
						"manager_name": managername,
						"instance_name": cmpInstancename.getValue()
					},
					function(resp) {
						var pop = Ext.MessageBox.show({ title: 'Add Instance', msg: 'Added successfully!', closable: false });
						setTimeout(function() { 
							pop.hide();
							
							hasInstances.push([cmpInstancename.getValue()]);
							InstanceStore.loadData(hasInstances);
							
							cmpInstancename.setValue('');
							cmpInstancename.focus(false, true);
						}, 1000);
					},
					function(resp) {
						cmpInstancename.markInvalid("This instance name is inappropriate");
					}
				);
			}
			else {
				return;
			}
		}
		
		function removeInstance() {
			var cmpManagers = Ext.getCmp('treeManagers' + pid);
			var smTree = cmpManagers.getSelectionModel();
			var type = smTree.selNode.attributes.iconCls;
			var managerName = smTree.selNode.text;
			
			var smGrid = Ext.getCmp('gridInstances' + pid).getSelectionModel();
			// 일단 하나씩만 지울 수 있음
			var record = smGrid.getSelected();
			
			channel.send(1, 'org.krakenapps.firewall.api.msgbus.FirewallPlugin.removeInstance', 
				{
					"manager_name": managerName,
					"instance_name": record.data.name
				},
				function(resp) {
					// hasInstances의 변화를 확인하고 싶다면 이하 내용을 setTimeout 함수 안에 넣어서 확인하라. 안그러면 hasInstances값은 전과 후가 똑같이 나온다.
					
					var hasInstances = smTree.selNode.attributes.instances;
					
					var ii = null;
					$.each(hasInstances, function(idx, instance) {
						if(record.data.name == instance[0]) {
							ii = idx;
						}
					});
					
					if(ii != null) {
						hasInstances.splice(ii, 1);
					}
					
					InstanceStore.loadData(hasInstances);
					
					var pop = Ext.MessageBox.show({ title: 'Remove Instance', width: 200, msg: 'Removed successfully!', closable: false });
					setTimeout(function() { pop.hide(); }, 1000);
				},
				Ext.ErrorFn
			);
			
		}
		
		// IP Tab Methods
		function block() {
			var cmpGroupname = Ext.getCmp('treeGroupsForIPTab' + pid);
			var cmpHost = Ext.getCmp('txtSourceIp' + pid);
			var cmpMin = Ext.getCmp('spinnerInterval' + pid);
		
			var sm = cmpGroupname.getSelectionModel();
			var type = sm.selNode.attributes.iconCls;
			var groupname = sm.selNode.text;
			
			if(type == 'ico-group') {
				channel.send(1, 'org.krakenapps.firewall.api.msgbus.FirewallPlugin.block', 
					{
						"group_name": groupname,
						"host": cmpHost.getValue(),
						"minutes": cmpMin.getValue()
					},
					function(resp) {
						getGroupRules(groupname, function() {
							var pop = Ext.MessageBox.show({ title: 'Add Blocked IP', msg: 'Added successfully!', closable: false });
							setTimeout(function() { pop.hide(); }, 1000);
							
							cmpHost.setValue('');
							cmpMin.setValue('');
							cmpHost.focus();
						});
					},
					function(resp) {
						
						if(cmpHost.getValue() == "") { cmpHost.markInvalid("This field is required"); }
						else { cmpHost.validate(); }
						
						if(cmpMin.getValue() == "") { cmpMin.markInvalid("This field is required"); }
						else { cmpMin.validate(); }
					}
				);
			}
			else { 
				return;
			}
		}
		
		function unblock() {
			var cmpGroupname = Ext.getCmp('treeGroupsForIPTab' + pid);
			var smTree = cmpGroupname.getSelectionModel();
			var type = smTree.selNode.attributes.iconCls;
			var groupname = smTree.selNode.text;
			
			var smGrid = Ext.getCmp('gridBlockedIp' + pid).getSelectionModel();
			// 일단 하나씩만 지울 수 있음
			var record = smGrid.getSelected();
			
			channel.send(1, 'org.krakenapps.firewall.api.msgbus.FirewallPlugin.unblock', 
				{
					"group_name": groupname,
					"host": record.data.ip
				},
				function(resp) {
					RuleStore.remove(record);
					
					var pop = Ext.MessageBox.show({ title: 'Remove Blocked IP', msg: 'Removed successfully!', closable: false });
					setTimeout(function() { pop.hide(); }, 1000);
				},
				Ext.ErrorFn
			);	
		}
		
		// Groups Tab Methods
		function createGroup() {
			var tree = Ext.getCmp('treeGroupsWithInstances' + pid);
			var root = tree.getRootNode();
			var node = root.appendChild(new Ext.tree.TreeNode({
				iconCls: 'ico-group'
			}));
			tree.getSelectionModel().select(node);
			
			var ge = new Ext.tree.TreeEditor(tree, { }, {
				beforeNodeClick: Ext.emptyFn, // 앞에 아이콘 누르면 편집 화면 뜨는거 방지
				blankText: 'A group name is required',
				selectOnFocus:true,
				completeOnEnter: true,
				listeners: {
					cancelEdit: function(ed, val, startval) {
						root.removeChild(node, false);
						ge.destroy();
					},
					complete: function(ed, val, startval) {
						if(ge.getValue() == "") {
							root.removeChild(node, false);
							ge.destroy();
							createGroup();
							return;
						}
						
						channel.send(1, 'org.krakenapps.firewall.api.msgbus.FirewallPlugin.createGroup',
							{ 
								'name': val
							},
							function(resp) {
								var pop = Ext.MessageBox.show({ title: 'New Group', msg: 'Group "' + val + '" Added!', width: 150, closable: false });
								setTimeout(function() { pop.hide(); }, 1000);
							},
							function(resp) {
								Ext.Msg.alert('New Group', 'An error occured!<br/>Please check browser console.', function() {
									root.removeChild(node, false);
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
		
		function removeGroup(node) {
			Ext.Msg.show({
				title: 'Remove Group',
				msg: 'It will remove "' + node.text + '"<br/>Are you sure?',
				buttons: Ext.Msg.OKCANCEL,
				fn: function(result,d,box) {
					
					if(result == "ok") {
						channel.send(1, 'org.krakenapps.firewall.api.msgbus.FirewallPlugin.removeGroup',
							{
								"name": node.text
							},
							function(resp) {
								var tree = Ext.getCmp('treeGroupsWithInstances' + pid);
								var root = tree.getRootNode();
								root.removeChild(node, false);
							},
							Ext.ErrorFn
						);
					}
				},
				animEl: 'elId',
				icon: Ext.MessageBox.QUESTION
			});
		}
		
		function leaveGroup(node) {
			var parent = node.parentNode;
			
			channel.send(1, 'org.krakenapps.firewall.api.msgbus.FirewallPlugin.leaveGroup',
				{
					"group_name": parent.text,
					"instance_name": node.text
				},
				function(resp) {
					parent.removeChild(node, false);
					
					var pop = Ext.MessageBox.show({ title: 'Leave', width: 400, msg: 'Instance "<b>' + node.text + '</b>" left from Group "<b>' + parent.text + '</b>" successfully!', closable: false });
					setTimeout(function() { pop.hide(); }, 1000);
				},
				Ext.ErrorFn
			);
		}
		
		function joinGroup(instance, parent) {
			channel.send(1, 'org.krakenapps.firewall.api.msgbus.FirewallPlugin.joinGroup',
				{
					"group_name": parent,
					"instance_name": instance
				},
				function(resp) {
					var pop = Ext.MessageBox.show({ title: 'Join', width: 400, msg: 'Instance "<b>' + instance + '</b>" joined with Group "<b>' + parent + '</b>" successfully!', closable: false });
					setTimeout(function() { pop.hide(); }, 1000);
				},
				Ext.ErrorFn
			);
				
		}
		
		// get methods, treeNode 선택에 대한 handler도 여기 다 있음
		function getGroupRules(groupname, callback) {
			channel.send(1, 'org.krakenapps.firewall.api.msgbus.FirewallPlugin.getGroupRules',
				{
					"group_name" : groupname.toString()
				},
				function(resp) {
					Ext.getCmp('gridBlockedIp' + pid).setTitle('Blocked IP in <i>' + groupname + '</i>');
					
					RuleStore.loadData(resp);
					
					if(callback != null) callback();
				}
			);
		}
		
		function getInstanceRules(instancename) {
			channel.send(1, 'org.krakenapps.firewall.api.msgbus.FirewallPlugin.getInstanceRules', 
				{
					"instance_name" : instancename.toString()
				},
				function(resp) {
					Ext.getCmp('gridBlockedIp' + pid).setTitle('Blocked IP in <i>' + instancename + '</i>');
					
					var rules = [];
					$.each(resp.rules, function(idx, rulename) {
						var rule = { "ip": null, "expire": null };
						rule.ip = rulename;
						rules.push(rule);
					});
					
					var rulesObj = { "rules": rules };
					RuleStore.loadData(rulesObj);
				}
			);
		}
		
		function getGroups(callback) {
			channel.send(1, 'org.krakenapps.firewall.api.msgbus.FirewallPlugin.getGroups', {},
				function(resp) {
					
					Processed.GroupsWithInstancesForIPTab = [];
					Processed.GroupsWithInstancesForGroupsTab = [];
					Processed.Groups = [];
					
					$.each(resp.groups, function(idx, group) {
						var members = [];
						var membersForIPTab = [];
						
						
						$.each(group.members, function(jdx, member) {
							members.push({
								text: member,
								leaf: true,
								iconCls: 'ico-instance'
							});
							
							membersForIPTab.push({
								text: member,
								leaf: true,
								iconCls: 'ico-instance',
								listeners: { 
									'click': function() {
										getInstanceRules(this.text);
									}
								}
							});
						});
						
						Processed.Groups.push({
							text: group.name,
							leaf: true,
							iconCls: 'ico-group',
							instancesArray: members,
							listeners: {
								'click' : function() {
									
								}
							}
						});
						
						Processed.GroupsWithInstancesForGroupsTab.push({
							text: group.name,
							leaf: false,
							iconCls: 'ico-group',
							expanded: true,
							children: members
						});
						
						Processed.GroupsWithInstancesForIPTab.push({
							text: group.name,
							leaf: false,
							iconCls: 'ico-group',
							expanded: true,
							children: membersForIPTab,
							listeners: {
								'click' : function() {
									getGroupRules(this.text);
									
								}
							}
						});
					});
					
					if(callback != null) callback();
				}
			);
		}
		
		function getInstanceManagers(callback) {
			channel.send(1, 'org.krakenapps.firewall.api.msgbus.FirewallPlugin.getInstanceManagers', {},
				function(resp) {
					Processed.Managers = [];
					
					$.each(resp.managers, function(idx, manager) {					
						Processed.Managers.push({
							text: manager.name,
							leaf: true,
							iconCls: 'ico-manager',
							instances: manager.instances.wrap(),
							listeners: {
								'click': function() {
									Ext.getCmp('gridInstances' + pid).setTitle('Instances in <i>' + this.text + '</i>');
									
									InstanceStore.loadData(this.attributes.instances);
								}
							}
						});
						
					});
					
					if(callback != null) callback();
				}
			);
		}
		
		function findGroupById(id) {
			var found;
			$.each(Processed.Groups, function(idx, group) {
				if(group.id == id) {
					found = group;
					return;
				}
			});
			return found;
		}
		
		function getInstances(callback) {
			channel.send(1, 'org.krakenapps.firewall.api.msgbus.FirewallPlugin.getInstances', {},
				function(resp) {
					Processed.Instances = [];
					
					$.each(resp.instances, function(idx, instance) {
						Processed.Instances.push({
							text: instance.name,
							leaf: true,
							iconCls: 'ico-instance'
						});
					});
					
					
					if(callback != null) callback();
				}
			);
		}
		
		// methods about tab & tree
		var TreeNodeGroupsOnly = new Ext.tree.AsyncTreeNode({ text: 'Groups', expanded: true, children: [] })
		var TreeNodeGroupsWithInstances = new Ext.tree.AsyncTreeNode({ text: 'Groups', expanded: true, children: [] });
		var TreeNodeInstances =  new Ext.tree.AsyncTreeNode({ text: 'Instances', expanded: true, children: [] });
		var TreeNodeManagers = new Ext.tree.AsyncTreeNode({ text: 'Managers', expanded: true, children: [] });
		
		function activateIPTab() {
			getGroups(function() {
				var treeGroupsForIPTab = Ext.getCmp('treeGroupsForIPTab' + pid);
				
				treeGroupsForIPTab.setRootNode(new Ext.tree.AsyncTreeNode({
					text: 'Groups',
					expanded: true,
					children: Processed.GroupsWithInstancesForIPTab
				}));
				
				
				// 첫번째 자식노드 선택
				var firstnode = treeGroupsForIPTab.getRootNode().firstChild;
				var sm = treeGroupsForIPTab.getSelectionModel();
				sm.clearSelections(true);
				sm.select(firstnode);
				
				try {
					firstnode.fireEvent('click', Ext.emptyFn); //여기에 뭔가 에러가 나는데 작동은 됨
				}
				catch (err) {}
			});
		}
		
		function activateGroupsTab() {
			getInstances(function() {
				var treeInstances = Ext.getCmp('treeInstances' + pid);
				var treeCombined = Ext.getCmp('treeGroupsWithInstances' + pid);
				treeCombined.setRootNode(new Ext.tree.AsyncTreeNode({
					text: 'Groups',
					expanded: true,
					isParent: true,
					children: Processed.GroupsWithInstancesForGroupsTab
				}));
				treeInstances.setRootNode(new Ext.tree.AsyncTreeNode({
					text: 'Instances',
					expanded: true,
					children: Processed.Instances
				}));
			});
		}
		
		function activateInstancesTab() {
			//getInstances();
			getInstanceManagers(function() {
				var treeManagers = Ext.getCmp('treeManagers' + pid);
				treeManagers.setRootNode(new Ext.tree.AsyncTreeNode({
					id: 'rootManager' + pid,
					text: 'Managers',
					expanded: true,
					children: Processed.Managers
				}));
				
				// 첫번째 자식노드 선택
				var firstnode = treeManagers.getRootNode().firstChild;
				var sm = treeManagers.getSelectionModel();
				sm.clearSelections(true);
				sm.select(firstnode);
				
				try {
					firstnode.fireEvent('click', Ext.emptyFn); //여기에 뭔가 에러가 나는데 작동은 됨
				}
				catch (err) {}
			});
		}
		
		// initialize store
		var RuleStore = new Ext.data.JsonStore({
			fields: ['ip', 'expire'],
			root: 'rules'
		});
		
		var sampleRule = {
			rules: [ 
				{ 'ip': '123.123.123.123', 'expire': "2011-01-29 04:33:05+0900" },
				{ 'ip': '123.3.123.123', 'expire': "2011-01-30 04:33:05+0900" },
				{ 'ip': '123.54.123.123', 'expire': "2011-01-29 08:33:05+0900" },
				{ 'ip': '123.13.123.123', 'expire': "2011-01-26 06:33:05+0900" }
			]
		};
		var initRule = {
			rules: []
		};
		
		RuleStore.loadData(initRule);
		
		
		var InstanceStore = new Ext.data.ArrayStore({
			fields: [ 'name' ]
		});
		
		var sampleInstance = [['asdf'], ['zxcv'], ['qwer']];
		var initInstance = [];
		
		InstanceStore.loadData(initInstance);
		
		// Layout
		MyWindowUi = Ext.extend(Ext.Panel, {
			layout: 'border',
			initComponent: function() {
				this.items = [
					{
						xtype: 'tabpanel',
						id: 'tab' + pid,
						//activeTab: 0,
						region: 'center',
						split: true,
						border: false,
						items: [
							{
								title: 'IP',
								xtype: 'panel',
								layout: 'border',
								listeners: { 
									//afterrender: activateIPTab,
									//render: activateIPTab,
									activate: activateIPTab,
								},
								items: [
									{
										xtype: 'treepanel',
										id: 'treeGroupsForIPTab' + pid,
										region: 'west',
										title: 'Groups',
										rootVisible: false,
										width: 200,
										split: true,
										root: TreeNodeGroupsOnly
									},
									{
										xtype: 'grid',
										id: 'gridBlockedIp' + pid,
										region: 'center',
										split: true,
										title: 'Blocked IP',
										store: RuleStore,
										viewConfig: { forceFit: true },
										columns: [
											{
												xtype: 'gridcolumn',
												dataIndex: 'ip',
												header: 'IP',
												sortable: true,
											},
											{
												xtype: 'gridcolumn',
												dataIndex: 'expire',
												header: 'Expire',
												sortable: true,
											}
										],
										tbar: {
											xtype: 'toolbar',
											items: [
												{
													xtype: 'buttongroup',
													title: 'Add IP',
													columns: 5,
													items: [
														
														{
															xtype: 'tbtext',
															text: '&nbsp; Source &nbsp;'
														},
														{
															xtype: 'textfield',
															id: 'txtSourceIp' + pid,
															width: 150,
															vtype: 'IPAddress',
															//allowBlank: false
														},
														{
															xtype: 'tbtext',
															text: '&nbsp; Interval &nbsp;'
														},
														{
															xtype: 'spinnerfield',
															width: 50,
															id: 'spinnerInterval' + pid,
															minValue: 0,
															maxValue: 10000,
															allowDecimals: true,
															decimalPrecision: 1,
															incrementValue: 1,
															//alternateIncrementValue: 2.1,
															accelerate: true,
															//allowBlank: false
														},
														{
															xtype: 'button',
															text: 'Add',
															iconCls: 'ico-add',
															style: {
																'margin-left' : '5px',
																'margin-right': '5px'
															},
															handler: block
														}
													]
												},
												{
													xtype: 'buttongroup',
													title: 'Remove IP',
													columns: 1,
													items: [
														{
															xtype: 'button',
															text: 'Remove',
															iconCls: 'ico-remove',
															rowspan: 2,
															handler: unblock,
															style: {
																'margin-left' : '5px',
																'margin-right': '5px'
															}
														}
													]
												}
											]
										}
									}
								]
							},
							{
								title: 'Groups',
								xtype: 'panel',
								layout: 'hbox',
								listeners: {
									//afterrender: activateGroupsTab,
									//render: test,
									activate: activateGroupsTab
								},
								layoutConfig: {
									align: 'stretch',
									pack: 'start',
								},
								items: [
									{
										xtype: 'treepanel',
										region: 'west',
										title: 'Groups',
										rootVisible: false,
										id: 'treeGroupsWithInstances' + pid,
										bbar: {
											items: [
												{
													xtype: 'button',
													text: 'New Group',
													iconCls: 'ico-add',
													handler: createGroup
												}
											]
										},
										flex: 1,
										split: true,
										fn: {
											isExistAlready: function(nodename, parentNode) {
												var returnVal = false;
												$.each(parentNode.childNodes, function(idx, childNode) {
													if(childNode.attributes.text == nodename) {
														returnVal = true;
													}
												});
												
												return returnVal;
											}
										},
										listeners: {
											beforenodedrop: function(e) {
												function notAllowed(error) {
													console.log('not allowed!! : ' + error); // do not allowed
													return false;
												}
											
												if(e.target.parentNode != null) {
													if(e.point == "above" && e.target.attributes.iconCls == "ico-group") {
														return notAllowed('target is not group');
													}
													
													if(e.target.attributes.iconCls == "ico-group") {
														if(this.fn.isExistAlready(e.dropNode.attributes.text, e.target)) {
															return notAllowed('existance err');
														}
														
														e.dropNode = new Ext.tree.TreeNode(e.dropNode.attributes); // not move, drop to copy instance
														joinGroup(e.dropNode.attributes.text, e.target.attributes.text);
														
														return true;	
													}
													
													if(e.target.attributes.iconCls == "ico-instance") {
														if(this.fn.isExistAlready(e.dropNode.attributes.text, e.target.parentNode)) {
															return notAllowed('existance err');
														}
														
														e.dropNode = new Ext.tree.TreeNode(e.dropNode.attributes); // not move, drop to copy instance
														joinGroup(e.dropNode.attributes.text, e.target.parentNode.attributes.text);
													
														return true;
													}
													
												}
												else {
													return notAllowed('target is not group');
												}
												
											}
										},
										root: TreeNodeGroupsWithInstances,
										enableDD: true,
										dragConfig: {
											ddGroup: 'asdf',
											onDrag: function(e) {

											},
											onDragOver: function(e, overzone) {
												$('#'+overzone).css('border', '2px solid red');
											},
											onDragOut: function(e, outzone) {
												$('#'+outzone).css('border', '2px solid lightgray');
											},
											onDragDrop: function(e, dropzone) { // Drop시 호출
												var node = this.dragData.node;
												
												this.hideProxy(); // StatusProxy (ghost node)가 안사라지기 때문에 호출
												this.initFrame(); // DragDrop을 다시 시작하기 위해 호출
												
												$('#'+dropzone).css('border', '2px solid lightgray');
												
												if(node.attributes.iconCls == "ico-group") {
													removeGroup(node);
												}
												else if (node.attributes.iconCls == "ico-instance") {
													leaveGroup(node);
												}
												
											},
										},
									},
									{
										xtype: 'panel',
										region: 'center',
										width: 100,
										id: 'dropZone' + pid,
										listeners: {
											afterrender: function(e) { // Drop 가능하게 만들어줌
												dd = new Ext.dd.DragDrop("ddTrash" + pid, "asdf");
												dd.onDragDrop = function(e, id) {
													
												};
												
											}
										},
										html: '<div style="padding: 10px">drag instance to group<br/><br/><br/>drag instance here to remove from group<br/><br/>drag group here to remove<br/><br/><div id="ddTrash' + pid + '" style="border:2px solid lightgray; padding: 20px; text-align: center"><img src="../img/trash_closed.png" /></div></div>'
									},
									{
										xtype: 'treepanel',
										region: 'east',
										title: 'Instances',
										rootVisible: false,
										id: 'treeInstances' + pid,
										flex: 1,
										split: true,
										enableDrag: true,
										enableDrop: false,
										ddAppendOnly: false,
										root: TreeNodeInstances
									},
								]
							},
							{
								title: 'Instances',
								xtype: 'panel',
								layout: 'border',
								listeners: {
									//afterrender: activateInstancesTab,
									activate: activateInstancesTab
								},
								items: [
									{
										xtype: 'treepanel',
										region: 'west',
										id: 'treeManagers' + pid,
										title: 'Instances Managers',
										rootVisible: false,
										width: 200,
										split: true,
										root: TreeNodeManagers
									},
									{
										xtype: 'grid',
										id: 'gridInstances' + pid,
										region: 'center',
										split: true,
										title: 'Instances',
										store: InstanceStore,
										viewConfig: { forceFit: true },
										columns: [
											{
												xtype: 'gridcolumn',
												dataIndex: 'name',
												header: 'Instances',
												sortable: true,
												renderer: function(val) {
													return '<img src="img/16-image.png" style="margin-bottom:-3px"/> ' + val;
												}
											}
										],
										tbar: {
											xtype: 'toolbar',
											items: [
												{
													xtype: 'buttongroup',
													title: 'Add Instance',
													columns: 3,
													items: [
														
														{
															xtype: 'tbtext',
															text: '&nbsp; Instance Name &nbsp;'
														},
														{
															xtype: 'textfield',
															id: 'txtInstanceName' + pid,
															width: 150
														},
														{
															xtype: 'button',
															text: 'Add',
															iconCls: 'ico-add',
															style: {
																'margin-left' : '10px',
																'margin-right': '10px'
															},
															handler: createInstance
														}
													]
												},
												//'->',
												{
													xtype: 'buttongroup',
													title: 'Remove Instance',
													columns: 1,
													items: [
														{
															xtype: 'button',
															text: 'Remove',
															iconCls: 'ico-remove',
															style: {
																'margin-left' : '15px',
																'margin-right': '15px'
															},
															handler: removeInstance
														}
													]
												}
											]
										}
									}
								]
							}
						]
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
		
		var window = windowManager.createWindow(pid, this.name, 700, 400, cmp1);
		cmp1.show();
		
		
		Ext.getCmp('tab' + pid).setActiveTab(0);
	}
	
	this.onstop = function() {
	}
}

processManager.launch(new Firewall());