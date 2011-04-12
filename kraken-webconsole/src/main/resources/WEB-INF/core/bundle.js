Bundle = function () {
    this.name = "Bundle";

    this.onstart = function (pid, args) {
        var that = this;
		
		var StorePackage = new Ext.data.ArrayStore({
            proxy: new Ext.data.MemoryProxy(),
            fields: ['description', 'name', 'date', 'version'],
            sortInfo: {
                field: 'name',
                direction: 'ASC'
            }
        });
		
		function getInstalledPackages(p) {
			var pnl = p.items.items[0];
			var tbar = pnl.getTopToolbar();
			channel.send(1, 'org.krakenapps.webconsole.plugins.PackagePlugin.getInstalledPackages', {},
				function(resp) {
					resp = {
    "packages": [
        {
            "description": null,
            "name": "kraken-siem",
            "date": "2011-01-17 19:43:00+0900",
            "version": "1.0.0"
        },
		{
            "description": null,
            "name": "kraken-zxcv",
            "date": "2011-01-17 19:43:00+0900",
            "version": "1.0.0"
        },
		{
            "description": null,
            "name": "kraken-qwer",
            "date": "2011-01-17 19:43:00+0900",
            "version": "1.0.0"
        },{
            "description": null,
            "name": "kraken-hhh",
            "date": "2011-01-17 19:43:00+0900",
            "version": "1.0.0"
        },{
            "description": null,
            "name": "kraken-qqq",
            "date": "2011-01-17 19:43:00+0900",
            "version": "1.0.0"
        },{
            "description": null,
            "name": "zxcv-siem",
            "date": "2011-01-17 19:43:00+0900",
            "version": "1.0.0"
        },{
            "description": null,
            "name": "qwer-siem",
            "date": "2011-01-17 19:43:00+0900",
            "version": "1.0.0"
        },{
            "description": null,
            "name": "hhh-siem",
            "date": "2011-01-17 19:43:00+0900",
            "version": "1.0.0"
        },{
            "description": null,
            "name": "zz-siem",
            "date": "2011-01-17 19:43:00+0900",
            "version": "1.0.0"
        },{
            "description": null,
            "name": "x-siem",
            "date": "2011-01-17 19:43:00+0900",
            "version": "1.0.0"
        }
		
    ]
}
					// storing
					var arr = [];
					Ext.each(resp.packages, function (obj, i) {
						var p = [];
						for (prop in obj)
							p.push(obj[prop]);
							
						arr.push(p);
					});
					StorePackage.loadData(arr);
					
					// drawing dataview
					pnl.add({
						xtype: 'dataview',
						store: StorePackage,
						tpl: new Ext.XTemplate('<ul>', '<tpl for=".">', '<li class="appicon">', '<img width="64" height="64" src="img/pkg_64.png" />', '<strong>{name}</strong>', '<span>{version}</span>', '</li>', '</tpl>', '</ul>'),
						itemSelector: 'li.appicon',
						overClass: 'appicon-hover',
						singleSelect: true,
						multiSelect: true,
						autoScroll: true
					});
					
					pnl.doLayout();
					
					// making toolbar
					var arr = [];
					arr.push('<span style="padding-left: 5px">Filter by Name:</span>');
					var txtFilterName = new Ext.form.TextField({
						labelWidth: 0,
						emptyText: 'Type Program Name',
						fieldCls: 'AppsSearchTextfield',
						enableKeyEvents: true,
						listeners: {
							keyup: function(c, e) {
								var value = c.getValue().toLowerCase();
								var store = StorePackage;
								store.suspendEvents();
								store.clearFilter();
								store.resumeEvents();
								
								console.log('---------');
								store.filterBy(function(rec) {
									return rec.get('name').toLowerCase().search(value) != -1;
								});
								
								store.sort('name', 'ASC');
							}
						}
					});
					arr.push(txtFilterName);
					
					tbar.removeAll();
					tbar.add(arr);
				}
			);
		}
		
		
        var MainUI = new Ext.TabPanel({
            border: false,
            tabPosition: 'bottom',
            activeTab: 1,
            items: [{
                title: 'Programs',
                layout: 'fit',
                items: [
                /*new Ext.Panel({
                    layout: 'fit',
                    items: dataview,
                    bodyCls: 'AppsBox',
                    tbar: makeFilter(resp.packages),
					listeners: {
						beforerender: function() {}
					}
                })*/]
            }, 
			{
                title: 'Packages',
                layout: 'fit',
				listeners: {
					activate: getInstalledPackages
				},
                items: [
                new Ext.Panel({
                    layout: 'fit',
                    items: [],
                    bodyCls: 'AppsBox',
                    tbar: []
                })]
            },
			{
                title: 'Bundles'
            }]
        });

        var window = windowManager.createWindow(pid, this.name, 720, 350, MainUI);
	}

    this.onstop = function () {}
}

processManager.launch(new Bundle());