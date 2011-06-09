IPManager = (function () {
    this.name = "IP Manager";

    this.onstart = function (pid, args) {
		var that = this;
		
		channel.send(1, 'org.krakenapps.dom.msgbus.PushPlugin.subscribe', { "callback" : "kraken-ipm-log" },
	/** subscribe **/
	function() {
        var IpEntryStore = new Ext.data.JsonStore({
            fields: ['id', 'ip', 'mac', 'mac_vendor', 'is_block', 'is_protected', 'first_seen', 'last_seen', 'agent_id'],
            root: 'ip_entries'
        });

        var initIpEntry = { ip_entries: [] };
        IpEntryStore.loadData(initIpEntry);
		
		var IpmLogStore = new Ext.data.JsonStore({
            fields: ['date', 'agent_id', 'id', 'ip1', 'ip2', 'mac1', 'mac2', 'type'],
            root: 'ipmlog'
        });

        var initIpmLog = { ipmlog: [] };
        IpmLogStore.loadData(initIpmLog);
		
		channel.registerTrap('kraken-ipm-log', function(msg) {
			console.log(msg);
			IpmLogStore.insert(0, new IpmLogStore.recordType(msg));
		});
		
		function createTree() {
			var root = that.treeAgent.setRootNode(new Ext.tree.AsyncTreeNode({
				text: 'root',
				expanded: true,
				children: [],
				listeners: {
					click: function() {
						getIpEntries();
					}
				}
			}));
			
			var sm = that.treeAgent.getSelectionModel();
			sm.clearSelections(true);
			sm.select(root);
			
			try { 
				root.fireEvent('click', Ext.emptyFn);
			}
			catch (err) {}
		}
		
		function getIpEntries() {
			channel.send(1, 'org.krakenapps.ipmanager.msgbus.IpManagerPlugin.getIpEntries', {}, 
				function(resp) {
					console.log(resp);
					IpEntryStore.removeAll();
					IpEntryStore.loadData(resp);
				}
			);
		}
		
        var MainUI = new Ext.Panel({
            layout: 'border',
            border: false,
            defaults: {
                split: true
            },
            items: [
				new Ext.TabPanel({
					region: 'south',
					activeTab: 0,
					height: 200,
					border: false,
					items: [
						{
							xtype: 'grid',
							title: 'All',
							store: IpmLogStore,
							viewConfig: { forceFit: true },
							columns: [
								{
									xtype: 'gridcolumn',
									dataIndex: 'agent_id',
									header: 'Agent',
									width: 50,
									sortable: true
								},
								{
									xtype: 'gridcolumn',
									dataIndex: 'id',
									header: 'ID',
									width: 30,
									sortable: true
								},
								{
									xtype: 'gridcolumn',
									dataIndex: 'date',
									header: 'Time',
									width: 95,
									sortable: true
								},
								{
									xtype: 'gridcolumn',
									dataIndex: 'ip1',
									header: 'IP1',
									width: 95,
									sortable: true
								},
								{
									xtype: 'gridcolumn',
									dataIndex: 'ip2',
									header: 'IP2',
									width: 95,
									sortable: true
								},
								{
									xtype: 'gridcolumn',
									dataIndex: 'mac1',
									header: 'MAC1',
									width: 95,
									sortable: true
								},
								{
									xtype: 'gridcolumn',
									dataIndex: 'mac2',
									header: 'MAC2',
									width: 95,
									sortable: true
								},
								{
									xtype: 'gridcolumn',
									dataIndex: 'type',
									header: 'Type',
									width: 150,
									sortable: true
								}
							]
						},
						{
							title: 'Detect',
							xtype: 'panel',
							html: 'hello'
						}
					]
				}),
				{
					xtype: 'panel',
					layout: 'border',
					region: 'center',
					border: false,
					items: [
						that.treeAgent = new Kraken.ext.AreaTreePanel({
							region: 'west',
							width: 150
						},
						{
							click: function() {
								console.log(this);
							},
							append: function(a, b, c) {
								console.log(a);/*
								this.appendChild(new Ext.tree.TreeNode({
									text: 'hello'
								}));
								a.un('append');*/
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
									store: IpEntryStore,
									tbar: {
										xtype: 'toolbar',
										listeners: {
											beforerender: createTree
										},
										items: [
											{
												xtype: 'button',
												iconCls: 'ico-profile',
												text: 'View'
											}
										]
									},
									columns: [
										{
											xtype: 'gridcolumn',
											dataIndex: 'id',
											header: '#',
											width: 25,
											sortable: true
										},
										{
											xtype: 'gridcolumn',
											dataIndex: 'ip',
											header: 'IP Address',
											width: 95,
											sortable: true
										},
										{
											xtype: 'gridcolumn',
											dataIndex: 'mac',
											header: 'MAC',
											width: 95,
											sortable: true
										},
										{
											xtype: 'gridcolumn',
											dataIndex: 'mac_vendor',
											header: 'MAC Vendor',
											width: 160,
											sortable: true
										},
										{
											xtype: 'gridcolumn',
											dataIndex: 'is_block',
											header: 'Block?',
											width: 90,
											sortable: true
										},
										{
											xtype: 'gridcolumn',
											dataIndex: 'is_protected',
											header: 'Protected?',
											width: 130,
											sortable: true
										},
										{
											xtype: 'gridcolumn',
											dataIndex: 'first_seen',
											header: 'First Seen',
											width: 130,
											sortable: true
										},
										{
											xtype: 'gridcolumn',
											dataIndex: 'last_seen',
											header: 'Last Seen',
											width: 120,
											sortable: true
										},
										{
											xtype: 'gridcolumn',
											dataIndex: 'agent_id',
											header: 'Agent',
											width: 160,
											sortable: true
										}
									]
								})
							]
						}
					]
				}
			]
        });
		
        var window = windowManager.createWindow(pid, that.name, 800, 600, MainUI);
	}); /** subscribe **/
    }

	this.onstop = function() {
		channel.send(1, 'org.krakenapps.dom.msgbus.PushPlugin.unsubscribe', { "callback" : "kraken-ipm-log" }, function() { });
	}
});

processManager.launch(new IPManager());