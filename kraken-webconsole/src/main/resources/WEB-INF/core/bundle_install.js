Bundle.PackageInstall = function(config) {
	Ext.apply(this, config); // parent, callback
	var that = this;
	
	var sm = new Ext.grid.CheckboxSelectionModel();
	
	var form = new Ext.form.FormPanel({
		labelAlign: 'right',
		layout: 'absolute',
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
				store: ['asd', 'asd'],
				sm: sm,
				columns: [
					sm,
					{
						header: 'Name'
					},
					{
						header: 'Version'
					}
				],
				height: 200
			})
		],
		region: 'center',
		height: 60
	});
	
	var panel = new Ext.Panel({
		style: 'padding: 20px',
		items: form,
		border: false,
		layout: 'border'
	});
	
	var windowInstallPkg = windowManager.createChildWindow({
		title: 'Install New Package',
		width: 450,
		height: 200,
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