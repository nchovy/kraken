function openInstallBundleWindow(parentWin, closefn) {

    var msg = function(title, msg){
        Ext.Msg.show({
            title: title,
            msg: msg,
            minWidth: 200,
            modal: true,
            icon: Ext.Msg.INFO,
            buttons: Ext.Msg.OK
        });
    };

    var fibasic = new Ext.ux.form.FileUploadField({
        width: '100%'
    });

    var fibtn = new Ext.Button({
        text: 'Get File Path',
        handler: function(){
            var v = fibasic.getValue();
            msg('Selected File', v && v != '' ? v : 'None');
        }
    });
	
	function InstallBundle(a,b) {
		
		var radioVal = $('input:radio[name=radioSrc' + parentWin.pid + ']:checked').val();	
		if(radioVal == undefined)
			return;
		
		if(radioVal == "local")
		{
			installbundlewin.destroy();
			closefn(function() {
				
			});
		}
		else if(radioVal = "maven")
		{
			var groupId = Ext.getCmp('mvnGroupId' + parentWin.pid).getValue();
			var artifactId = Ext.getCmp('mvnArtifactId' + parentWin.pid).getValue();
			var version = Ext.getCmp('mvnVersion' + parentWin.pid).getValue();
			
			channel.send(1, 'org.krakenapps.webconsole.plugins.BundlePlugin.installBundle',
				{
					'group_id' : groupId,
					'artifact_id' : artifactId,
					'version' : version
				}, 
				function(resp) {
					installbundlewin.destroy();
					closefn();
					
				}
			)
		}
	}
	
	function enableInstall() {
		Ext.getCmp('btnInstall' + parentWin.pid).enable();
	}
	
	MyWindowUi = Ext.extend(Ext.Panel, {
		layout: 'border',
		listeners: {
			resize: function() {
				fibasic.setWidth(this.getWidth() - 60);
			}
		},
		initComponent: function() {
			this.buttons = [
				{
					id: 'btnInstall' + parentWin.pid,
					xtype: 'button',
					text: 'Install',
					handler: InstallBundle,
					disabled: true
				},
				{
					xtype: 'button',
					text: 'Cancel',
					handler: function() { installbundlewin.destroy(); }
				}
			],
			this.items = [
				{
					xtype: 'panel',
					region: 'north',
					border: false,
					height: 100,
					padding: 15,
					items: [
						{
							xtype: 'radio',
							name: 'radioSrc' + parentWin.pid,
							height: 25,
							inputValue: 'local',
							boxLabel: 'Install from Local File System',
							handler: enableInstall
						},
						{
							xtype: 'panel',
							border: false,
							padding: 15,
							style: { "border": "1px solid #B5B8C8" },
							items: [
								fibasic,
								{
									xtype: 'label',
									text: ''
								}
							]
						}
					]
				},
				{
					xtype: 'panel',
					region: 'center',
					layout: 'border',
					border: false,
					items: [
						{
							xtype: 'panel',
							region: 'north',
							border: false,
							height: 180,
							padding: 15,
							labelAlign: 'top',
							items: [
								{
									xtype: 'radio',
									name: 'radioSrc' + parentWin.pid,
									height: 25,
									inputValue: 'maven',
									boxLabel: 'Install from Maven Repository',
									handler: enableInstall
								},
								{
									xtype: 'form',
									style: { "border": "1px solid #B5B8C8" },
									border: false,
									padding: 15,
									labelAlign: 'right',
									items: [
										{
											xtype: 'combo',
											fieldLabel: 'Select Repository',
											anchor: '100%'
										},
										{
											id: 'mvnGroupId' + parentWin.pid,
											xtype: 'textfield',
											fieldLabel: 'Group ID',
											anchor: '100%',
											value: 'org.krakenapps'
										},
										{
											id: 'mvnArtifactId' + parentWin.pid,
											xtype: 'textfield',
											fieldLabel: 'Artifact ID',
											anchor: '100%',
											value: 'kraken-cron'
										},
										{
											id: 'mvnVersion' + parentWin.pid,
											xtype: 'textfield',
											fieldLabel: 'Version',
											anchor: '100%',
											value: '1.0.0'
										}
										/*
										{
											xtype: 'container',
											layout: 'column',
											items: [
												{
													xtype: 'container',
													columnWidth: 0.5,
													layout: 'form',
													items: [
														{
															xtype: 'textfield',
															fieldLabel: 'Artifact ID',
															anchor: '100%'
														}
													]
												},
												{
													xtype: 'container',
													columnWidth: 0.5,
													layout: 'form',
													items: [
														{
															xtype: 'textfield',
															fieldLabel: 'Version',
															anchor: '100%'
														}
													]
												}
											]
										}*/
									]
								}
							]
						},
						{
							xtype: 'panel',
							region: 'center',
							padding: 15,
							border: false,
							//html: '<iframe id="sss" style="width:99.8%; height:100%; border:1px solid #B5B8C8" src="http://download.krakenapps.org"></iframe>'
						}
					]
				}
			];
			MyWindowUi.superclass.initComponent.call(this);
		}
	});


	MyWindow = Ext.extend(MyWindowUi, {
		initComponent: function() {
			MyWindow.superclass.initComponent.call(this);
		}
	});

	var cmp1 = new MyWindow({ });
		
	var installbundlewin = windowManager.createChildWindow({
		title: 'Install Bundle',
		width: 400,
		height: 400,
		items: cmp1,
		parent: parentWin,
		modal: true,
		maximizable: false,
		resizable: false
	});
	
	cmp1.show();

}