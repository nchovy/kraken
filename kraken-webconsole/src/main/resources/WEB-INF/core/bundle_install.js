Bundle.PackageInstall = function(config) {
	Ext.apply(this, config); // parent, callback
	var pid = this.parent.pid;
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
	
	var BundleStore = new Ext.data.JsonStore({
		fields: ['id', 'last_modified', 'status', 'location', 'vendor', 'name', 'built_by', 'license', 'export_package', 'url', 'import_package', 'version']
	});
	
	var initBundle = [
		{
			name: 'org.krakenapps.ipojo',
			version: '1.0.0'
		},
		{
			name: 'org.krakenapps.rss',
			version: '1.1.0'
		}
	];
	BundleStore.loadData(initBundle);
	
	var step_select = new WizardPanel({
		step: 0,
		labelAlign: 'right',
		layout: 'absolute',
		style: 'padding: 15px',
		bodyStyle: 'background: none',
		border: false,
		items: [
			{
				xtype: 'label',
				text: 'Available Packages',
				height: 32,
				style: 'font-size: 1.3em; font-weight: bold; display: block'
			},
			{
				xtype: 'label',
				text: 'Select one of the available packages that you wish install.',
				width: 450,
				y: 20,
				height: 25,
				style: 'display: block; border-bottom: 1px dotted #ccc; padding-bottom: 7px'
			},
			{
				xtype: 'label',
				x: 0,
				y: 65,
				text: 'Work with:'
			},
			{
				xtype: 'combo',
				x: 60,
				y: 60,
				anchor: '100%'
			},
			{
				xtype: 'label',
				x: 0,
				y: 92,
				style: 'text-align: right',
				text: 'Find more software',
				anchor: '100%'
			},
			that.available = new Ext.grid.GridPanel({
				x: 0,
				y: 120,
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
				height: 160,
				listeners: {
					rowclick: function() {
						that.createBtnNext.enable();
					}
				}
			}),
			{
				xtype: 'fieldset',
				x: 0,
				y: 290,
				title: 'Details',
				items: [
					{
						xtype: 'label',
						text: 'detail here'
					}
				]
			}
		],
		listeners: {
			activate: function() {
				that.createBtnPrev.disable();
			}
		}
	});
	
	var step_check = new WizardPanel({
		step: 1,
		layout: 'form',
		defaults: {
			width: 450
		},
		labelWidth: 95,
		items: [
			{
				xtype: 'label',
				text: 'Install Details',
				height: 26,
				style: 'font-size: 1.3em; font-weight: bold; display: block'
			},
			{
				xtype: 'label',
				html: 'Package <b>kraken-rss 1.0.0</b> contains following bundles. Review the bundles to be installed.',
				height: 36,
				style: 'display: block'
			},
			that.willinstall = new Ext.grid.GridPanel({
				store: BundleStore,
				viewConfig: { forceFit: true },
				columns: [
					{
						xtype: 'gridcolumn',
						width: 250,
						header: 'Name',
						dataIndex: 'name'
					},
					{
						xtype: 'gridcolumn',
						width: 200,
						header: 'Version',
						dataIndex: 'version'
					}
				],
				height: 160
			}),
		],
		listeners: {
			activate: function() {
				that.createBtnPrev.enable();
				//getPackageInfo();
			}
		}
	});
	
	var step_install = new WizardPanel({
		step: 2,
		layout: 'form',
		defaults: {
			width: 450
		},
		labelWidth: 95,
		items: [
			{
				xtype: 'label',
				text: 'Installing Package...',
				height: 32,
				style: 'font-size: 1.3em; font-weight: bold; display: block'
			},
			{
				xtype: 'progress',
				animate: true
			}
		]
	});
	
	var step_done = new WizardPanel({
		step: 3,
		layout: 'form',
		defaults: {
			width: 450
		},
		labelWidth: 95,
		items: [
			{
				xtype: 'label',
				text: 'Installed Successfully!',
				height: 32,
				style: 'font-size: 1.3em; font-weight: bold; display: block'
			},
			{
				xtype: 'label',
				text: 'installed package',
				height: 36,
				style: 'display: block'
			}
		]
	});
	
	var PackageUI = new Ext.Panel({
		layout: 'border',
		border: false,
		defaults: { border: false },
		items: [
			{
				region: 'west',
				width: 0,
				html: ''
			},
			that.createWiz = new WizardContainer({
				items: [step_select, step_check, step_install, step_done]
			})
		],
		buttons: [
			that.createBtnPrev = new Ext.Button({
				text: '&nbsp;&laquo; Previous&nbsp;',
				disabled: true,
				handler: function() {
					var step = that.createWiz.getCurrent().getStep();
					that.createWiz.goStep(step - 1);
					//this.disable();
					
					that.createBtnNext.setText('&nbsp;Next &raquo;&nbsp;');
				}
			}),
			that.createBtnNext = new Ext.Button({
				text: '&nbsp;Next &raquo;&nbsp;',
				disabled: true,
				listeners: {
					click: function() {
						var step = that.createWiz.getCurrent().getStep();
						
						if(that.createWiz.getCurrent().isLast()) {
							wizard.close();
						}
						else {
							that.createWiz.goStep(step + 1);
							that.createBtnPrev.enable();
							
							if(that.createWiz.getCurrent().isLast()) {
								this.setText('&nbsp;Finish&nbsp;');
								//that.createBtnPrev.disable();
							}
						}
					}
				}
			})
		]
	});
	
	var wizard = windowManager.createChildWindow({
		title: 'Install New Package',
		width: 500,
		height: 450,
		items: {
			id: 'wizard' + pid,
			layout: 'card',
			border: false,
			defaults: { border: false },
			region: 'center',
			items: [PackageUI],
			activeItem: 0
		},
		parent: that.parent,
		modal: true,
		maximizable: false,
		resizable: false,
		listeners: {
			beforeclose: function() {
				if(that.callback != null) 
					that.callback();
			}
		}
	});
	
	var wizlay = Ext.getCmp('wizard' + pid).getLayout();
}