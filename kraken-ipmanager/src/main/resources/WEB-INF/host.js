Host = (function () {
    this.name = "Host";

    this.onstart = function (pid, args) {
		var that = this;
		
        var HostStore = new Ext.data.JsonStore({
            fields: ['id', 'first_seen', 'category', 'vendor', 'name', 'workgroup', 'last_seen', 'agent_id'],
            root: 'hosts'
        });

        var initHost = { hosts: [] };
        HostStore.loadData(initHost);
		
		function getHosts() {
			channel.send(1, 'org.krakenapps.ipmanager.msgbus.IpManagerPlugin.getHosts', {}, 
				function(resp) {
					HostStore.removeAll();
					HostStore.loadData(resp);
				}
			);
		}
		
		function simpleDateTime(v) {
			return v.split('+')[0];
		}
		
		var HostColumns = [
			{
				xtype: 'gridcolumn',
				dataIndex: 'id',
				header: 'ID',
				width: 35,
				sortable: true
			},
			{
				xtype: 'gridcolumn',
				dataIndex: 'name',
				header: 'Name',
				width: 150,
				sortable: true
			},
			{
				xtype: 'gridcolumn',
				dataIndex: 'vendor',
				header: 'Vendor',
				width: 200,
				sortable: true
			},
			{
				xtype: 'gridcolumn',
				dataIndex: 'category',
				header: 'Category',
				width: 250,
				sortable: true
			},
			{
				xtype: 'gridcolumn',
				dataIndex: 'workgroup',
				header: 'Workgroup',
				width: 120,
				sortable: true
			},
			{
				xtype: 'gridcolumn',
				dataIndex: 'first_seen',
				header: 'First Seen',
				width: 150,
				sortable: true,
				renderer: simpleDateTime
			},
			{
				xtype: 'gridcolumn',
				dataIndex: 'last_seen',
				header: 'Last Seen',
				width: 150,
				sortable: true,
				renderer: simpleDateTime
			}
		];
		
        var MainUI = new Ext.Panel({
            layout: 'border',
            border: false,
            defaults: {
                split: true
            },
            items: [
				{
					xtype: 'panel',
					layout: 'border',
					region: 'center',
					border: false,
					listeners: {
						beforerender: getHosts
					},
					items: [
						that.gridHost = new Ext.grid.GridPanel({
							viewConfig: { forceFit: true },
							region: 'center',
							store: HostStore,
							columns: HostColumns
						})
					]
				}
			]
        });
		
        var window = windowManager.createWindow(pid, that.name, 1000, 350, MainUI);
    }

	this.onstop = function() {
	}
});

processManager.launch(new Host());