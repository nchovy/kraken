Bundle = function () {
	
	var supr = this;
	
    this.name = "Bundle";

    this.onstart = function (pid, args) {
		programManager.loadCSS('/css/bundle.css');
		programManager.loadJS('/js/ext/Ext.DataView.DragSelector.js');
		programManager.loadJS('/js/ext/ext-ButtonRowSelectionModel.js');
		programManager.loadJS('/core/bundle_info.js');
		programManager.loadJS('/core/bundle_install.js');
		
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
			var tbar = p.getTopToolbar();
			
			channel.send(1, 'org.krakenapps.webconsole.plugins.PackagePlugin.getInstalledPackages', {},
				function(resp) {

					// storing
					var arr = [];
					arr.push(['', 'Install<br/> New Package...', '', '']);
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
						tpl: new Ext.XTemplate('<ul>', '<tpl for=".">', '<li class="appicon">', 
							'<tpl if="date != &quot;&quot;"><img width="64" height="64" src="img/pkg_64.png" /></tpl>',
							'<tpl if="date == &quot;&quot;"><img width="64" height="64" src="img/installer_64.png" /></tpl>',
							'<strong>{name}</strong>', '<span>{version}</span>', '</li>', '</tpl>', '</ul>'),
						itemSelector: 'li.appicon',
						overClass: 'appicon-hover',
						singleSelect: true,
						multiSelect: true,
						autoScroll: true,
						listeners: {
							click: function(dv, idx, node, e) {
								if(idx == 0) { // install new package
									that.winInstallPkg = new Bundle.PackageInstall({
										parent: window,
										callback: function() {
											console.log(supr.winInstallPkg);
										}
									});
								}
								
							}
						}
					});
					
					pnl.doLayout();
				}
			);
		}
		
		function makePackageToolbar(p) {
			var pnl = p.items.items[0];
			var tbar = p.getTopToolbar();
			
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
						
						//console.log('---------');
						store.filterBy(function(rec) {
							if(rec.get('name') == "Install<br/> New Package...") return true;
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
						tpl: new Ext.XTemplate('<ul>', '<tpl for=".">', '<li class="bdleicon">',
							'<tpl if="status == &quot;ACTIVE&quot;"><img width="48" height="48" src="img/bundle_64.png" /></tpl>',
							'<tpl if="status == &quot;RESOLVED&quot; || status == &quot;INSTALLED&quot;"><img width="48" height="48" src="img/bundle_64_disabled.png" /></tpl>',
							'<strong>{name}</strong>', '<span>{version}</span>' ,
							'<div class="rightbutton">',
							'<tpl if="status == &quot;ACTIVE&quot;"><span class="active">Stop</span></tpl>',
							'<tpl if="status == &quot;RESOLVED&quot; || status == &quot;INSTALLED&quot;"><span class="deactive">Start</span></tpl>',
							'</div>',
							'</li>', '</tpl>', '</ul>'),
						itemSelector: 'li.bdleicon',
						overClass: 'bdleicon-hover',
						selectedClass: 'bdleicon-selected',
						singleSelect: true,
						multiSelect: true,
						autoScroll: true,
						listeners: {
							selectionchange: function(d, sel) {
								//console.log(sel);
							},
							click: function(dv, idx, node, e) {
								//console.log(node);
								//console.log(e.target.getAttribute('class'));
								
								var rec = StoreBundle.getAt(idx);
								
								if(e.target.tagName.toLowerCase() == 'strong') {
									if(supr.winInfoBundle != null) {
										supr.winInfoBundle.onupdate(rec.data);
									}
									else {
										supr.winInfoBundle = new Bundle.Info({
											parent: window,
											bundle: StoreBundle.getAt(idx).data,
											callback: function() {
												console.log(supr.winInfoBundle);
												delete supr.winInfoBundle;
												
											}
										}).onstart();
									}
								}
								else if(e.target.tagName.toLowerCase() == 'span' && e.target.parentNode.getAttribute('class') == 'rightbutton') {
									if(e.target.getAttribute('class') == 'active') {
										stopBundle(rec.data);
									}
									else if(e.target.getAttribute('class') == 'deactive') {
										startBundle(rec.data);
									}
									else {
									}
								}
								else {
								}
							}
						},
						plugins: [
							new Ext.DataView.DragSelector()
						]
					});
					
					pnl.doLayout();
					
					// gridpanel
					//StoreBundle.loadData(resp);
					//BundleGridView.
				}
			);
		}
		
		function makeBundleToolbar(p) {
			var tbar = p.getTopToolbar();
			
			// making toolbar
			var arr = [];
			arr.push('<span style="padding-left: 5px">Filter by Name:</span>');
			var txtFilterName = new Ext.form.TextField({
				labelWidth: 0,
				emptyText: 'Type Program Name',
				fieldCls: 'AppsSearchTextfield',
				style: 'margin-right: 5px',
				enableKeyEvents: true,
				listeners: {
					keyup: function(c, e) {
						var value = c.getValue().toLowerCase();
						var store = StoreBundle;
						store.suspendEvents();
						store.clearFilter();
						store.resumeEvents();
						
						//console.log('---------');
						store.filterBy(function(rec) {
							return rec.get('name').toLowerCase().search(value) != -1;
						});
						
						store.sort('name', 'ASC');
					}
				}
			});
			arr.push(txtFilterName);
			
			arr.push('-');
			arr.push('View: ');
			arr.push({
				xtype: 'button',
				iconCls: 'ico-view-tile',
				enableToggle: true,
				toggleGroup: 'viewtype',
				handler: function() {
					that.BundleTab.layout.setActiveItem(0);
				}
			});
			
			arr.push({
				xtype: 'button',
				iconCls: 'ico-view-detail',
				enableToggle: true,
				toggleGroup: 'viewtype',
				pressed: true,
				handler: function() {
					console.log(that.BundleTab);
					that.BundleTab.layout.setActiveItem(1);
				}
			});
			
			tbar.removeAll();
			tbar.add(arr);
		}
		
		function renderStatus(val) {
			if (val == 'ACTIVE') {
				return '<span style="color:green;">' + val + '</span>';
			} else {
				return '<span style="color:red;">' + val + '</span>';
			}
			return val;
		}
		
		function startBundle(selected) {
			channel.send(1, 'org.krakenapps.webconsole.plugins.BundlePlugin.startBundle', { 'bundle_id' : selected.id }, function(resp) {
				var pop = Ext.MessageBox.show({ title: 'Start Bundle', width: 350, msg: 'Start "' + selected.name + '" successfully!', closable: false });
				setTimeout(function() { pop.hide(); }, 1000);
				getBundles(that.BundleTab);
			})
		}
		
		function refresh() { 
			channel.send(1, 'org.krakenapps.webconsole.plugins.BundlePlugin.refresh', {}, function(resp) {
			});
		}
		
		var prohibitedBundles = ["org.apache.felix.framework", "org.jboss.netty", "org.apache.felix.ipojo", "org.krakenapps.msgbus", "org.krakenapps.webconsole"];
		
		function stopBundle(selected) {
			var isProhibited = false;
			$.each(prohibitedBundles, function(idx, bdl) {
				if(selected.name == bdl) {
					var pop = Ext.MessageBox.show({ title: 'Stop Bundle', width: 350, msg: 'Access Denied!<br/>Cannot stop "' + selected.name + '"', closable: false });
					setTimeout(function() { pop.hide(); }, 1000);
					isProhibited = true;
				}
				
			});
			
			if(!isProhibited) {
				channel.send(1, 'org.krakenapps.webconsole.plugins.BundlePlugin.stopBundle', { 'bundle_id' : selected.id }, function(resp) {
					var pop = Ext.MessageBox.show({ title: 'Stop Bundle', width: 350, msg: 'Stop "' + selected.name + '" successfully!', closable: false });
					setTimeout(function() { pop.hide(); }, 1000);
					getBundles(that.BundleTab);
				});
			}

		}
		
        var MainUI = new Ext.TabPanel({
            border: false,
            tabPosition: 'bottom',
            activeTab: 0,
            items: [/*{
                title: 'Programs',
                layout: 'fit',
                items: [
					new Ext.Panel({
						layout: 'fit',
						items: dataview,
						bodyCls: 'AppsBox',
						tbar: makeFilter(resp.packages),
						listeners: {
							beforerender: function() {}
						}
					})
				]
            },*/ 
			{
                title: 'Packages',
                layout: 'fit',
				listeners: {
					activate: getInstalledPackages,
					afterrender: makePackageToolbar
				},
                items: [
					new Ext.Panel({
						layout: 'fit',
						items: [],
						bodyCls: 'AppsBox'
					})
				],
				tbar: []
            },
			that.BundleTab = new Ext.Panel({
                title: 'Bundles',
				layout: 'card',
				tbar: [],
				listeners: {
					activate: getBundles,
					afterrender: makeBundleToolbar
				},
                items: [
					that.BundleIconView = new Ext.Panel({
						layout: 'fit',
						items: [],
						bodyCls: 'AppsBox'
					}),
					that.BundleGridView = new Ext.grid.GridPanel({
						store: StoreBundle,
						viewConfig: { forceFit: true },
						listeners: {
							rowdblclick: function(g, idx, e) {
								console.log(supr.winInfoBundle);
								if(supr.winInfoBundle != null) {
									supr.winInfoBundle.onupdate(StoreBundle.getAt(idx).data);
								}
								else {
									supr.winInfoBundle = new Bundle.Info({
										parent: window,
										bundle: StoreBundle.getAt(idx).data,
										callback: function() {
											console.log(supr.winInfoBundle);
											delete supr.winInfoBundle;
											
										}
									}).onstart();
								}
							}
						},
						columns: [
							{
								xtype: 'gridcolumn',
								dataIndex: 'id',
								header: 'ID',
								sortable: true,
								resizable: true,
								width: 50,
								align: 'right'
							},
							{
								xtype: 'gridcolumn',
								dataIndex: 'name',
								header: 'Symbolic Name',
								sortable: true,
								width: 250
							},
							{
								xtype: 'gridcolumn',
								header: 'Version',
								sortable: true,
								width: 100,
								dataIndex: 'version'
							},
							{
								xtype: 'gridcolumn',
								header: 'Status',
								renderer: renderStatus,
								sortable: true,
								width: 100,
								dataIndex: 'status'
							},
							{
								xtype: 'actioncolumn',
								width: 50,
								items: [
									{
										getClass: function(v, meta, rec) {
											if(rec.data.status == 'ACTIVE') {
												this.items[0].tooltip = 'Stop';
												return 'ico-ac-stop';
											}
											else {
												this.items[0].tooltip = 'Start';
												return 'ico-ac-start';
											}
										},
										handler: function(grid, rowIndex, colIndex) {
											var rec = StoreBundle.getAt(rowIndex);
											if(rec.data.status == 'ACTIVE') {
												stopBundle(rec.data);
											}
											else {
												startBundle(rec.data);
											}
										}
										
									}
								]
							}
						]
					})
				],
				activeItem: 1
            })]
        });

        var window = windowManager.createWindow(pid, this.name, 720, 350, MainUI);
	}

    this.onstop = function () {
		if(supr.winInfoBundle != null) {
			supr.winInfoBundle.close();
		}
	}
}

processManager.launch(new Bundle());