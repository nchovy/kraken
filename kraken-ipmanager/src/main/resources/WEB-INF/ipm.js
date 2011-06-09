IPManager = (function () {
    this.name = "IP Manager";

    this.onstart = function (pid, args) {
		//programManager.loadJS('/core/area_tree.js');
		
        var that = this;
		
		var TreeBlank = new Ext.tree.AsyncTreeNode({ text: 'Agent', expanded: true, children: [] });
		
		/*
		"id": 9,
            "mac_vendor": {
                "address": "15, Shibata Hondori 4-chome Minami-ku,Nagoya Aichi Pref. 457-8520",
                "name": "Buffalo Inc.",
                "country": "JAPAN"
            },
            "first_seen": "2011-06-09 19:14:31+0900",
            "is_block": false,
            "is_protected": false,
            "mac": "00:24:A5:93:20:0A",
            "agent_id": 1,
            "last_seen": "2011-06-09 20:27:46+0900",
            "ip": "172.20.0.12"*/
        var UserStore = new Ext.data.JsonStore({
            fields: ['id', 'ip', 'mac', 'mac_vendor', 'is_block', 'is_protected', 'first_seen', 'last_seen', 'agent_id'],
            root: 'ip_entries'
        });

        var initUser = { ip_entries: [] };
        UserStore.loadData(initUser);
		
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
					UserStore.removeAll();
					UserStore.loadData(resp);
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
						    store: UserStore,
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
        });
		
        var window = windowManager.createWindow(pid, this.name, 800, 600, MainUI);
    }

	this.onstop = function() {
	}
});

processManager.launch(new IPManager());