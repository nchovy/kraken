function getCreateLoggerWizard(LoggerFactoryStore, LoggerStore, SelectedLogger) {
	var that = this;
	
	function createLogger() {
		var params = {
			"factory": that.comboFactory.getValue(),
			"namespace": 'local',
			"name": that.txtName.getValue(),
			"description": that.txtDescription.getValue()
		}
		
		$.each(step_createOptions.items.items, function(i, opt) {
			if(i == 0) return; // not input form, just label 'Set Logger Options'
			params[opt.json.name] = opt.getValue();
		});
		
		console.log(params);
		
		channel.send(1, 'org.krakenapps.log.api.msgbus.LoggerPlugin.createLogger',
			params,
			function(resp) {
				SelectedLogger.setValue('local\\' + that.txtName.getValue());
			
				//var btnNext = step_creating.ownerCt.ownerCt.getBottomToolbar().items.items[2];
				var btnNext = step_creating.ownerCt.ownerCt.buttons[1];
				btnNext.fireEvent('click', btnNext);
			},
			Ext.ErrorFn
		);
		
		/*
		setTimeout(function() {
			SelectedLogger.setValue('local\\' + that.txtName.getValue());
			
			var btnNext = step_creating.ownerCt.ownerCt.getBottomToolbar().items.items[2];
			btnNext.fireEvent('click', btnNext);
		}, 2000);
		*/
	}
	
	var step_create = new WizardPanel({
		step: 0,
		layout: 'form',
		defaults: {
			width: 263
		},
		labelWidth: 70,
		items: [
			{
				xtype: 'label',
				text: 'Create Logger',
				height: 32,
				style: 'font-size: 1.3em; font-weight: bold; display: block'
			},
			that.txtName = new Ext.form.TextField({
				fieldLabel: 'Name',
				listeners: {
					change: function(t, n, o) {
						//var btnNext = step_create.ownerCt.ownerCt.getBottomToolbar().items.items[2];
						var btnNext = step_create.ownerCt.ownerCt.buttons[1];
						if(n != '' && that.comboFactory.getValue() != '') {
							btnNext.enable();
						}
						else {
							btnNext.disable();
						}
					}
				}
			}),
			that.comboFactory = new Ext.form.ComboBox({
				fieldLabel: 'Factory',
				editable: false,
				valueField: 'name',
				displayField: 'name',
				mode: 'local',
				store: LoggerFactoryStore,
				forceSelection: true,
				typeAhead: true,
				triggerAction: 'all',
				emptyText: 'Select A Logger Factory...',
				listeners : {
					select: function(c, rec) {
						getFactoryOptions(rec.data.name, step_createOptions, that.txtName);
					}
				}
			}),
			that.txtDescription = new Ext.form.TextArea({
				fieldLabel: 'Description'
			})
		],
		listeners: {
			activate: function() { 
				//var toolbar = step_create.ownerCt.ownerCt.getBottomToolbar();
				var toolbar = step_create.ownerCt.ownerCt.buttons;
				var btnPrev = toolbar[0];
				var btnNext = toolbar[1];
				
				if(LoggerStore.getCount() == 0) {
					btnPrev.disable();
				}
				
				if(that.txtName.getValue() != '' && that.comboFactory.getValue() != '') {
					btnNext.enable();
				}
				else {
					btnNext.disable();
				}
			}
		}
	});
	
	// Set Logger Options for Creating Logger
	var step_createOptions = new WizardPanel({
		step: 1,
		layout: 'form',
		labelWidth: 83,
		defaults: {
			width: 250
		},
		items: [ ]
	});
	
	var step_creating = new WizardPanel({
		step: 2,
		html: '<b style="font-size: 1.3em">Creating Logger...</b>',
		listeners: {
			activate: function() {
				createLogger();
			}
		}
	});
	
	return [step_create, step_createOptions, step_creating];
}

function getSelectParserWizard(SelectedLogger, ParserFactoryStore) {
	var that = this;
	function createManagedLogger() {
		var param = {}
		param['parser'] = that.comboParser.getValue();
		param['logger'] = SelectedLogger.value;
		
		console.log(param);
		/*
		channel.send(1, 'org.krakenapps.siem.msgbus.LoggerPlugin.createLogger',
			param,
			function(resp) {
			}
		);
		*/
		
		//var btnFinish = step_final.ownerCt.ownerCt.getBottomToolbar().items.items[2];
		var btnFinish = step_final.ownerCt.ownerCt.buttons[1];
		setTimeout(function() {
			btnFinish.enable();
		}, 1000);
	}
	
	var step_selectParser = new WizardPanel({
		step: 0,
		layout: 'form',
		defaults: {
			width: 263
		},
		labelWidth: 70,
		items: [
			{
				xtype: 'label',
				text: 'Select Parser',
				height: 32,
				style: 'font-size: 1.3em; font-weight: bold; display: block'
			},
			{
				xtype: 'label',
				text: 'Select Parser for binding with Managed Logger',
				height: 26,
				width: 300,
				style: 'display: block'
			},
			that.comboParser = new Ext.form.ComboBox({
				fieldLabel: 'Parser',
				editable: false,
				valueField: 'name',
				displayField: 'name',
				mode: 'local',
				store: ParserFactoryStore,
				forceSelection: true,
				typeAhead: true,
				triggerAction: 'all',
				emptyText: 'Select A Parser...',
				listeners : {
					select: function(c, rec, i) {
						var desclabel = this.ownerCt.items.items[3];
						if (rec == null) {
							return;
						}
						
						if (rec.data.options.length > 0) {
							desclabel.setText(rec.data.description);
							
							step_setParserOptions.removeAll();
							step_setParserOptions.add({
								xtype: 'label',
								text: 'Set Parser Options',
								height: 32,
								style: 'font-size: 1.3em; font-weight: bold; display: block'
							});
							
							$.each(rec.data.options, function(idx, o) {

								step_setParserOptions.add({
									xtype: 'textfield',
									fieldLabel: o.display_name,
									value: o.default_value
								});
								
							});
							
							step_setParserOptions.doLayout(true, true);
							
							step_setParserOptions.setStep(1);
							step_final.setStep(2);
						}
						else {
							step_setParserOptions.clearStep();
							step_final.setStep(1);
						}
						
						//console.log('setParserOptions: ', step_setParserOptions.getStep());
						//console.log('setFinal: ', step_final.getStep());
					}
				}
			}),
			{
				xtype: 'label',
				style: 'margin-left: 78px; display: block'
			}
		]
	});
	
	// Set Parser Options
	var step_setParserOptions = new WizardPanel({
		layout: 'form',
		defaults: {
			width: 263
		},
		labelWidth: 70,
		items: [ ]
	});
	
	// Final
	var step_final = new WizardPanel({
		html: '<b style="font-size: 1.3em">Creating Managed Logger...</b><br/>Binding Parser with Logger...',
		listeners: {
			activate: function() {
				createManagedLogger();
			}
		}
	});
	
	return [step_selectParser, step_setParserOptions, step_final];
}

function getFactoryOptions(val, panelCreateOptions, txtName) {
	channel.send(1, 'org.krakenapps.log.api.msgbus.LoggerPlugin.getFactoryOptions',
		{
			"factory": val,
			"locale" : programManager.getLocale()
		},
		function(resp) {
			panelCreateOptions.removeAll();
			panelCreateOptions.add({
				xtype: 'label',
				text: 'Set Logger Options',
				height: 32,
				style: 'font-size: 1.3em; font-weight: bold; display: block'
			});
			
			$.each(resp.options, function(idx, o) {
				
				panelCreateOptions.add({
					xtype: 'textfield',
					fieldLabel: o.display_name,
					value: o.default_value,
					json: o
				});
				
			});
			
			panelCreateOptions.doLayout(true, true);
			
			if(txtName.getValue() != '') {
				//var btnNext = panelCreateOptions.ownerCt.ownerCt.getBottomToolbar().items.items[2];
				var btnNext = panelCreateOptions.ownerCt.ownerCt.buttons[1];
				btnNext.enable();
			}
		}
	);
}

function openCreateManagedLoggerWizard(parent, LoggerFactoryStore, LoggerStore, ParserFactoryStore) {
	var that = this;
	var pid = parent.pid;
	var cannotUndo = false;
	var SelectedLogger = {
		value: '',
		setValue: function(val) { this.value = val; }
	};
	
	// Select UI
	var step_select_radioGroup = new Ext.form.RadioGroup({
		columns: 1,
		fieldLabel: ' ',
		items: [
			{
				boxLabel: 'New Logger',
				name: 'radioSelLogger',
				inputValue: 1,
				checked: true,
				handler: function() {
					step_select_comboLogger.enable();
				}
			},
			{
				boxLabel: 'Select Existing Logger',
				name: 'radioSelLogger',
				inputValue: 2,
				handler: function() {
					step_select_comboLogger.disable();
				}
			}
		]
	});
	
	var step_select_comboLogger = new Ext.form.ComboBox({
		editable: false,
		valueField: 'name',
		labelStyle: 'padding-left: 20px',
		displayField: 'name',
		mode: 'local',
		store: (LoggerStore.getCount() == 0) ? [ 'No Logger' ] : LoggerStore,
		disabled: true,
		forceSelection: true,
		listeners: {
			select: function() {
				
			},
			afterrender: function() {
				this.setValue(this.store.getAt(0).data.name);
				this.fireEvent('select');
			}
		}
	});
	
	var SelectUI = new Ext.Panel({
		layout: 'border',
		border: false,
		defaults: { border: false },
		items: [
			{
				region: 'west',
				width: 0,
				html: 'Select'
			},
			{
				region: 'center',
				bodyStyle: 'padding: 15px',
				defaults: { border: false },
				layout: 'form',
				layoutConfig: {
					labelSeparator: ' '
				},
				labelWidth: 20,
				items: [
					{
						xtype: 'label',
						text: 'Select Logger',
						height: 32,
						style: 'font-size: 1.3em; font-weight: bold; display: block'
					},
					step_select_radioGroup,
					step_select_comboLogger
				]
			}
		],
		buttons: [
			//'->',
			{
				text: '&laquo; Previous',
				disabled: true
			},
			{
				text: 'Next &raquo;',
				handler: function() {
					var groupVal = parseInt(step_select_radioGroup.items.items[0].getGroupValue());
					if(groupVal == 2) {
						SelectedLogger.setValue('local\\' + step_select_comboLogger.getValue());
						console.log('SelectedLogger: ', SelectedLogger.value);
					}
					wizlay.setActiveItem(groupVal);
				}
			}
		]
	});
	
	// Create UI
	var CreateUI = new Ext.Panel({
		layout: 'border',
		border: false,
		defaults: { border: false },
		items: [
			{
				region: 'west',
				width: 0,
				html: 'Create'
			},
			that.createWiz = new WizardContainer({
				items: getCreateLoggerWizard(LoggerFactoryStore, LoggerStore, SelectedLogger)
			})
		],
		buttons: [
			//'->',
			that.createBtnPrev = new Ext.Button({
				text: '&laquo; Previous',
				handler: function() {
					var step = that.createWiz.getCurrent().getStep();
					
					if(step == 0) {
						wizlay.setActiveItem(0);
					}
					else {
						that.createWiz.goStep(step - 1);
					}
				}
			}),
			{
				text: 'Next &raquo;',
				listeners: {
					click: function() {
						var step = that.createWiz.getCurrent().getStep();
						
						if(that.createWiz.getCurrent().isLast()) {
							wizlay.setActiveItem(2);
							console.log('SelectedLogger: ', SelectedLogger.value);
							
							cannotUndo = true;
						}
						else {
							that.createWiz.goStep(step + 1);
							that.createBtnPrev.enable();
							if(that.createWiz.getCurrent().isLast()) {
								this.disable();
								that.createBtnPrev.disable();
							}
						}
					}
				}
			}
		]
	});
	
	var ParserUI = new Ext.Panel({
		layout: 'border',
		border: false,
		defaults: { border: false },
		items: [
			{
				region: 'west',
				width: 0,
				html: 'Parser'
			},
			that.parserWiz = new WizardContainer({
				items: getSelectParserWizard(SelectedLogger, ParserFactoryStore)
			})
		],
		buttons: [
			//'->',
			that.parserBtnPrev = new Ext.Button({
				text: '&laquo; Previous',
				handler: function() {
					var step = that.parserWiz.getCurrent().getStep();
					
					if(step == 0) {
						if(!cannotUndo) {
							wizlay.setActiveItem(0);
						}
					}
					else if (step == 1) {
						that.parserWiz.goStep(step - 1);
						if(cannotUndo) {
							that.parserBtnPrev.disable()
						}
					}
					else {
						that.parserWiz.goStep(step - 1);
					}
				}
			}),
			{
				text: 'Next &raquo;',
				handler: function() {
					var step = that.parserWiz.getCurrent().getStep();
					
					if(that.parserWiz.getCurrent().isLast()) {
						wizard.close();
					}
					else {
						that.parserWiz.goStep(step + 1);
						
						if(that.parserWiz.getCurrent().isLast()) {
							this.setText('Finish');
							this.disable();
							that.parserBtnPrev.disable();
						}
						else {
							that.parserBtnPrev.enable();
						}
					}
				}
			}
		],
		listeners: {
			activate: function() {
				setTimeout(function() {
					if(cannotUndo) {
						that.parserBtnPrev.disable();
					}
				}, 10);
			}
		}
	});
	
	var wizard = windowManager.createChildWindow({
		title: 'Logger Wizard',
		width: 390,
		height: 270,
		items: {
			id: 'wizard' + pid,
			layout: 'card',
			border: false,
			defaults: { border: false },
			region: 'center',
			items: [SelectUI, CreateUI, ParserUI],
			activeItem: (LoggerStore.getCount() > 0) ? 0 : 1
		},
		parent: parent,
		modal: true,
		maximizable: false,
		resizable: false
	});
	
	var wizlay = Ext.getCmp('wizard' + pid).getLayout();
	
}