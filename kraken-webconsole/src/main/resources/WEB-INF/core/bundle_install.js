Bundle.PackageInstall = function(config) {
	Ext.apply(this, config); // parent, callback
	var that = this;
	
	var sm = new Ext.grid.CheckboxSelectionModel();
	
	var PackageStore = new Ext.data.JsonStore({
		fields: [ 'name', 'version' ]
	});
	
	var initPackage = [
		{
			name: 'kraken-rss',
			version: '1.0.0'
		},
		{
			name: 'kraken-geoip',
			version: '1.2.0'
		}
	];
	PackageStore.loadData(initPackage);
	
	var form = new Ext.form.FormPanel({
		labelAlign: 'right',
		layout: 'absolute',
		style: 'padding: 15px',
		bodyStyle: 'background: none',
		border: false,
		items: [
			{
				xtype: 'label',
				x: 0,
				y: 5,
				text: 'Work with:',
			},
			{
				xtype: 'combo',
				x: 60,
				y: 0,
				anchor: '100%'
			},
			{
				xtype: 'label',
				x: 0,
				y: 32,
				style: 'text-align: right',
				text: 'Find more software',
				anchor: '100%'
			},
			that.grid = new Ext.grid.GridPanel({
				x: 0,
				y: 60,
				store: PackageStore,
				sm: sm,
				viewConfig: { forceFit: true },
				columns: [
					sm,
					{
						header: 'Name',
						dataIndex: 'name'
					},
					{
						header: 'Version',
						dataIndex: 'version'
					}
				],
				height: 200
			})
		],
		region: 'center',
		height: 60,
		buttons: [
			{
				xtype: 'button',
				text: 'Install'
			},
			{
				xtype: 'button',
				text: 'Cancel'
			}
		]
	});
	
	var panel = new Ext.Panel({
		items: form,
		border: false,
		layout: 'border'
	});
	
	var windowInstallPkg = windowManager.createChildWindow({
		title: 'Install New Package',
		width: 500,
		height: 450,
		items: panel,
		parent: that.parent,
		modal: true,
		maximizable: false,
		resizable: true,
		listeners: {
			beforeclose: function() {
				if(that.callback != null) 
					that.callback();
			}
		}
	});
	
	return windowInstallPkg;
	
}