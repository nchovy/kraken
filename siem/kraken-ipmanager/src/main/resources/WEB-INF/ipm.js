IPManagement = (function () {
    this.name = "IP Management";

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
            fields: ['date', 'agent_id', 'ip1', 'ip2', 'mac1', 'mac2', 'type', 'msg'],
            root: 'ipmlog'
        });

        var initIpmLog = { ipmlog: [] };
        IpmLogStore.loadData(initIpmLog);
		
		channel.registerTrap('kraken-ipm-log', function(msg) {
			switch(msg.type) {
				case 1: msg.msg = String.format('New IP [{0} ({1})] detected', msg.ip1, msg.mac1); break;
				case 2: msg.msg = String.format('New MAC [{0} ({1})] detected', msg.ip1, msg.mac1); break;
				case 3: msg.msg = String.format('Host [{0}]\'s IP {1} is changed to IP {2}', msg.mac1, msg.ip1, msg.ip2); break;
				case 4: msg.msg = String.format('Host [{0}]\'s MAC {1} is changed to MAC {2}', msg.ip1, msg.mac1, msg.mac2); break;
				case 5: msg.msg = String.format('Host [{0} ({1})] conflicts with Host [{2}]', msg.ip1, msg.mac1, msg.mac2); break;
			}
			
			if(IpmLogStore.getCount() == 30) {
				IpmLogStore.removeAt(29);
			}
			
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
					IpEntryStore.removeAll();
					IpEntryStore.loadData(resp);
				}
			);
		}
		
		var IpmLogColumns = [
			{
				xtype: 'gridcolumn',
				dataIndex: 'date',
				header: 'Time',
				width: 30,
				renderer: simpleDateTime
			},
			{
				xtype: 'gridcolumn',
				dataIndex: 'msg',
				header: 'Message',
				width: 150
			}
		];
		
		function simpleDateTime(v) {
			return v.split('+')[0];
		}
		
		function imgTrueFalse(v) {
			if(v == true) {
				return '<span class="ico-check" style="position:absolute"></span>'
			}
			else {
				return '<span class="ico-cross" style="position:absolute"></span>' //<img src="/img/badge-circle-cross-16-ns.png" width="16" height="16"/>'
			}
		}
		
        var MainUI = new Ext.Panel({
            layout: 'border',
            border: false,
            defaults: {
                split: true
            },
            items: [
				that.IpmLogTab = new Ext.TabPanel({
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
							columns: IpmLogColumns,
							listeners: {
								activate: function() {
									IpmLogStore.clearFilter();
								}
							}
						},
						{
							title: 'New IP Detected',
							xtype: 'grid',
							store: IpmLogStore,
							viewConfig: { forceFit: true },
							columns: IpmLogColumns,
							listeners: {
								activate: function() {
									IpmLogStore.filter([
										{
											property: 'type',
											value: 1
										}
									]);
								}
							}
						},
						{
							title: 'New MAC Detected',
							xtype: 'grid',
							store: IpmLogStore,
							viewConfig: { forceFit: true },
							columns: IpmLogColumns,
							listeners: {
								activate: function() {
									IpmLogStore.filter([
										{
											property: 'type',
											value: 2
										}
									]);
								}
							}
						},
						{
							title: 'IP Changed',
							xtype: 'grid',
							store: IpmLogStore,
							viewConfig: { forceFit: true },
							columns: IpmLogColumns,
							listeners: {
								activate: function() {
									IpmLogStore.filter([
										{
											property: 'type',
											value: 3
										}
									]);
								}
							}
						},
						{
							title: 'MAC Changed',
							xtype: 'grid',
							store: IpmLogStore,
							viewConfig: { forceFit: true },
							columns: IpmLogColumns,
							listeners: {
								activate: function() {
									IpmLogStore.filter([
										{
											property: 'type',
											value: 4
										}
									]);
								}
							}
						},
						{
							title: 'IP Conflict',
							xtype: 'grid',
							store: IpmLogStore,
							viewConfig: { forceFit: true },
							columns: IpmLogColumns,
							listeners: {
								activate: function() {
									IpmLogStore.filter([
										{
											property: 'type',
											value: 5
										},
										{
											fn: function(r) {
												return true;
											}
										}
									]);
								}
							}
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
							},
							append: function(a, b, c) {
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
												iconCls: 'ico-refresh',
												text: 'Refresh',
												handler: function() {
													getIpEntries();
												}
											}
										]
									},
									columns: [
										{
											xtype: 'gridcolumn',
											dataIndex: 'id',
											header: '#',
											width: 35,
											sortable: true
										},
										{
											xtype: 'gridcolumn',
											dataIndex: 'ip',
											header: 'IP Address',
											width: 100,
											sortable: true
										},
										{
											xtype: 'gridcolumn',
											dataIndex: 'mac',
											header: 'MAC',
											width: 140,
											sortable: true
										},
										{
											xtype: 'gridcolumn',
											dataIndex: 'mac_vendor',
											header: 'MAC Vendor',
											width: 160,
											sortable: true,
											renderer: function(v) {
												if(v != null)
													return '<span>' + v.name + '</span>'
												else
													return '';
											}
										},
										{
											xtype: 'gridcolumn',
											dataIndex: 'is_block',
											header: 'Block?',
											width: 70,
											sortable: true,
											renderer: imgTrueFalse
										},
										{
											xtype: 'gridcolumn',
											dataIndex: 'is_protected',
											header: 'Protected?',
											width: 70,
											sortable: true,
											renderer: imgTrueFalse
										},
										{
											xtype: 'gridcolumn',
											dataIndex: 'first_seen',
											header: 'First Seen',
											width: 130,
											sortable: true,
											renderer: simpleDateTime 
										},
										{
											xtype: 'gridcolumn',
											dataIndex: 'last_seen',
											header: 'Last Seen',
											width: 120,
											sortable: true,
											renderer: simpleDateTime
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

processManager.launch(new IPManagement());