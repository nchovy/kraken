Bundle = function () {
    this.name = "Bundle";

    this.onstart = function (pid, args) {
		programManager.loadJS('/js/ext/Ext.DataView.DragSelector.js');
        var that = this;
		
		var StorePackage = new Ext.data.ArrayStore({
            proxy: new Ext.data.MemoryProxy(),
            fields: ['description', 'name', 'date', 'version'],
            sortInfo: {
                field: 'name',
                direction: 'ASC'
            }
        });
		
		var StoreBundle = new Ext.data.ArrayStore({
			proxy: new Ext.data.MemoryProxy(),
			fields: ['id', 'last_modified', 'status', 'location', 'vendor', 'name', 'built_by', 'license', 'export_package', 'url', 'import_package', 'version'],
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
					/*resp = {
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
					}*/
					
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

		function getBundles(p) {
			var pnl = p.items.items[0];
			channel.send(1, 'org.krakenapps.webconsole.plugins.BundlePlugin.getBundles', {},
				function(resp) {
					
					// storing
					var arr = [];
					Ext.each(resp.bundles, function (obj, i) {
						var p = [];
						for (prop in obj)
							p.push(obj[prop]);
							
						arr.push(p);
					});
					StoreBundle.loadData(arr);
					
					// drawing dataview
					pnl.add({
						xtype: 'dataview',
						store: StoreBundle,
						tpl: new Ext.XTemplate('<ul>', '<tpl for=".">', '<li class="bdleicon">', '<img width="48" height="48" src="img/bundle_64.png" />',
							'<strong>{name}</strong>', '<span>{version}</span>' ,
							'<div class="rightbutton"><span style="border: 1px solid #aaa; padding: 3px; -webkit-border-radius: 3px; background-image: -webkit-gradient(linear,left bottom,left top,color-stop(0.02, rgb(224,224,224)),color-stop(0.52, rgb(255,255,255)));">Stop</span></div>',
							'</li>', '</tpl>', '</ul>'),
						itemSelector: 'li.bdleicon',
						overClass: 'bdleicon-hover',
						selectedClass: 'bdleicon-selected',
						singleSelect: true,
						multiSelect: true,
						autoScroll: true,
						listeners: {
							selectionchange: function(d, sel) {
								console.log(sel);
							}
						},
						plugins: [
							new Ext.DataView.DragSelector()
						]
					});
					
					pnl.doLayout();
				}
			);
		}
		
		function makeBundleToolbar(p) {
			var pnl = p.items.items[0];
			var tbar = pnl.getTopToolbar();
			
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
						var store = StoreBundle;
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
                title: 'Bundles',
				layout: 'fit',
				listeners: {
					activate: getBundles,
					afterrender: makeBundleToolbar
				},
                items: [
                new Ext.Panel({
                    layout: 'fit',
                    items: [],
                    bodyCls: 'AppsBox',
                    tbar: []
                })]
            }]
        });

        var window = windowManager.createWindow(pid, this.name, 720, 350, MainUI);
	}

    this.onstop = function () {}
}

processManager.launch(new Bundle());