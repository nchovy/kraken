SleepProxy = function() {
	this.name = "Sleep Proxy";

	var count = 10;
	var nodes = [];
	var refresh_group_id = 0;
	
	//Today
	var now_time = new Date();

	//from(getTimeStamp(now_time) + getTimeStamp(new Date(now_time.getTime()-86400)));
	var to_date = getTimeStamp(now_time);
	var from_date = getTimeStamp(new Date(now_time.getTime()-(1000*60*60*1*1))); //default

	//*************** Item Deleter ****************
	Ext.ns('Extensive.grid');

	Extensive.grid.ItemDeleter = Ext.extend(Ext.grid.RowSelectionModel, {
		width			: 30,
		sortable		: false,
		dataIndex		: 0, // this is needed, otherwise there will be an error
		
		menuDisabled	: true,
		fixed			: true,
		id				: 'deleter',
		
		initEvents		: function(){
			Extensive.grid.ItemDeleter.superclass.initEvents.call(this);
			this.grid.on('cellclick', function(grid, rowIndex, columnIndex, e){
				if(columnIndex==grid.getColumnModel().getIndexById('deleter')) {
					var record = grid.getStore().getAt(rowIndex);
					//console.log(record.json);
					if(record.json[0]=='policy')
						channel.send(1, 'org.krakenapps.sleepproxy.SleepProxyPlugin.removeSleepPolicy', { policy_id: record.json[1]}, reloadPolicies);
					else if(record.json[0]=='agent'){
						channel.send(1, 'org.krakenapps.sleepproxy.SleepProxyPlugin.removeAgent', { agent_id: record.json[1]}, function (){});
						grid.getStore().remove(record);
						grid.getView().refresh();
					}
				}
			});
		},
		
		renderer: function(v, p, record, rowIndex){
			return '<div class="extensive-remove" style="width: 15px; height: 16px;"><img src="../images/default/qtip/close.gif"></div>';
		}
	});

	var itemDeleter = new Extensive.grid.ItemDeleter();

	//***************전체적인 패널 ********************
	function createPanelUi() {
		
		MyPanelUi = Ext.extend(Ext.TabPanel, {
			title: 'TabPanel1',
			activeTab: 1,
			initComponent: function() {
				this.items = [
					{
						xtype		: 'panel',
						title		: 'Dashboard',
						layout		: 'border',
						height		: 500,
						defaults	: {
							split: true
							//collapsible: true,
							//bodyStyle: 'padding:15px'
						},
						items		: [
							dashboard_tree,
							power_chart
							/*new Ext.Panel({
								title: 'Chart',
								height: 500,
								region: 'center',
								listeners : {
									render : function(){
											dashboard_tree.expandAll();
										}
									}
							})*/
							]
					},
					{
						xtype: 'panel',
						title: 'Assets',
						layout: 'border',
						height: 440,
						defaults: {
							//collapsible: true,
							split: true,
							//bodyStyle: 'padding:15px'
						},
						items: [{
							xtype: 'panel',
							layout:'border',
							title: '',
							region:'west',
							//margins: '5 0 0 0',
							//cmargins: '5 5 0 0',
							width: 212,
							minSize: 100,
							maxSize: 250,
							defaults: {
								//collapsible: true,
								split: true,
								//bodyStyle: 'padding:15px'
							},
							items: [
									tree,
									panel_assets
								]
						},
						new Ext.Panel({
							text: '',
							region: 'center',
							items: [grid, agentlist_refresh_button]
						})]
						
					},
					{
						xtype		: 'panel',
						title		: 'Policy',
						layout		: 'border',
						height		: 500,
						defaults	: {	split: true	},
						items		: [ 
							new Ext.Panel({
								text	: '',
								region	: 'west',
								width	: 212,
								items: [insert_policy_panel]
							}),
							policy_grid ]
					},
					{
						xtype	: 'panel',
						title	: 'Log',
						layout	: 'border',
						height	: 500,
						defaults: {split:true },
						items	: [
							log_tree,
							log_grid,
							log_date_panel
							]
					}
				];
				MyPanelUi.superclass.initComponent.call(this);
			}
		});

		MyPanel = Ext.extend(MyPanelUi, {
			initComponent: function() {
				MyPanel.superclass.initComponent.call(this);
			}
		});
	}
	
	//***************** Dashboard 트리 *********************
	function createDashboardTreePanel() {
		//console.log(nodes);
		dashboard_tree = new Ext.tree.TreePanel({
			title: 'Agents Groups',
			width: 210,
			height: 300,
			region: 'west',
			root: {
				text: 'Agents',
				children : nodes
			},
			loader: {},
			listeners: {
				click: function(n){
					if(n.isRoot!=true){
						sendGetChartData(n.id);
						//console.log(dashboard_tree.getLoader().load());
						//dashboard_tree.getLoader().load(root); 
					}
				}
			}
		});
	}
	function parseChartData(chartdata){
		var retdata=[];
		var j;
		var tmp,tmp2,tmp3;
		for (j=0;j<chartdata.length ; j++)
		{			
			//chartdata[j].date
			tmp=chartdata[j].date.split(' ');
			tmp2=tmp[1].split('+');
			tmp=tmp2[0].split(':');
			tmp2=tmp[0]+':'+tmp[1];
			retdata.push({time : tmp2, used:chartdata[j].used, saved:chartdata[j].saved, can_saved:chartdata[j].can_saved});
		}
		return retdata;
	}
	function sendGetChartData(group_id){
		//Date
		var group_graph_load_date = new Date();
		var to_load_date= getTimeStamp(group_graph_load_date);
		var from_load_date = getTimeStamp(new Date(group_graph_load_date.getTime()-(1000*60*60*1*1))); //default
		channel.send(1, 'org.krakenapps.sleepproxy.SleepProxyPlugin.getGroupPowerGraph', { group_id : group_id, from: from_load_date, to: to_load_date }, function(resp){
			chart_store.loadData(parseChartData(resp['graph']));
			power_chart.findById('innerchart').bindStore(chart_store);
			//console.log(chart_store);
		});
	}

	function getTimeStamp(d) {
		var s =
		leadingZeros(d.getFullYear(), 4) + '-' +
		leadingZeros(d.getMonth() + 1, 2) + '-' +
		leadingZeros(d.getDate(), 2) + ' ' +

		leadingZeros(d.getHours(), 2) + ':' +
		leadingZeros(d.getMinutes(), 2) + ':' +
		leadingZeros(d.getSeconds(), 2) + '+0900';

	  return s;
	}

	function leadingZeros(n, digits) {
		var zero = '';
		n = n.toString();

		if (n.length < digits) {
			for (i = 0; i < digits - n.length; i++)
				zero += '0';
		 }
		return zero + n;
	}

	function loadChartDataStore(){
		chart_store = new Ext.data.JsonStore({
			fields:['time', 'used', 'saved', 'can_saved'],
			//data : [{time: '1', used:1, saved:1, can_saved:1},{time:'2', used:2,saved:2,can_saved:2}]
		});
		//console.log(chartdata_list);

		chart_store.loadData(chartdata_list);
	}
	//****************** Chart ***************************
	function createChart(){
		loadChartDataStore();
		power_chart = new Ext.Panel({
			iconCls:'chart',
			title: 'Chart',
			region: 'center',
			frame:true,
			//renderTo: 'container',
			//width:500,
			height:400,
			layout:'',
			listeners : {
				render : function(){
					//agentlist.insertBefore(new Ext.tree.AsyncTreeNode({text: '11'}));
					//dashboard_tree.setRootNode(grouplist);
					dashboard_tree.expandAll();
				}
			},
			items: [{
				xtype:'linechart',
				id : 'innerchart',
				store: chart_store,
				//url:'../../resources/charts.swf',
				height: 350,
				xField: 'time',
				yAxis: new Ext.chart.NumericAxis({
					displayName: 'Time',
					labelRenderer : Ext.util.Format.numberRenderer('0,0')
				}),
			/*	tipRenderer : function(chart, record, index, series){
					if(series.yField == 'visits'){
						return Ext.util.Format.number(record.data.visits, '0,0') + ' visits in ' + record.data.name;
					}else{
						return Ext.util.Format.number(record.data.views, '0,0') + ' page views in ' + record.data.name;
					}
				},*/
				chartStyle: {
					padding: 10,
					//height: 300,
					animationEnabled: true,
					font: {
						name: 'Tahoma',
						color: 0x444444,
						size: 15
					},
					dataTip: {
						padding: 5,
						border: {
							color: 0x99bbe8,
							size:1
						},
						background: {
							color: 0xDAE7F6,
							alpha: .9
						},
						font: {
							name: 'Tahoma',
							color: 0x15428B,
							size: 10,
							bold: true
						}
					},
					xAxis: {
						color: 0x69aBc8,
						majorTicks: {color: 0x69aBc8, length: 4},
						minorTicks: {color: 0x69aBc8, length: 2},
						majorGridLines: {size: 1, color: 0xeeeeee},
						labelRotation : 45
					},
					yAxis: {
						color: 0x69aBc8,
						majorTicks: {color: 0x69aBc8, length: 4},
						minorTicks: {color: 0x69aBc8, length: 2},
						majorGridLines: {size: 1, color: 0xdfe8f6}
					}
				},
				series: [{
					type: 'line',
					displayName: 'Used',
					yField: 'used',
					style: {
						//image:'bar.gif',
						//mode: 'stretch',
						color:0x99BBE8
					}
				},{
					type:'line',
					displayName: 'Saved',
					yField: 'saved',
					//height: 300,
					style: {
						color: 0x15428B
					}
				},{
					type:'line',
					displayName: 'Can Saved',
					yField: 'can_saved',
					style: {
						color: 0x22F28B
					}
				}]
			}]		
		});

	};
	//***************** Assets 트리 *********************
	function createTreePanel() {
	//	test_nodes = new Ext.tree.AsyncTreeNode({
	//		childNodes:nodes
	//	}
		tree = new Ext.tree.TreePanel({
			title: 'Agents Groups',
			width: 200,
			height: 250,
			region: 'center',
			root: {
				text: 'Agents',
				children : nodes
				//children	: test_nodes
			},
			loader: {},
			listeners: {
				click: function(n){
					//console.log(n);
					if(n.isRoot!=true){
						sendGetAgentsRequest(n.id);
						refresh_group_id=n.id;
						group_id_textfield.setValue(n.id);
					}
				}
			},
			buttons: [{
				text: 'Add Group',
				handler: function(n,m){
					groupaddwindow.show();
				}
			}]
		});

	}

	function createGroupAddWindow(){
		parent_id_combobox = new Ext.form.ComboBox({
			fieldLabel		: 'Parent ID',
			anchor			: '100%',
			title			: 'Parent ID',
			typeAhead		: true,
			triggerAction	: 'all',
			lazyRender		: true,
			mode			: 'local',
			store			: group_combobox_list,
			valueField		: 'id',
			displayField	: 'text',
			//hidden			: true,
			/*listeners		: {
				select	: function(cbox, cnt){
					policy_id_textfield.setValue(cnt.json[0]);
					channel.send(3, 'org.krakenapps.sleepproxy.SleepProxyPlugin.getSleepPolicy', { policy_id: cnt.json[0] }, function(resp2){
						policy_combobox.setValue( resp2['policy'].name);
						away_textfield.setValue(resp2['policy'].away_criteria);
						if(resp2['policy'].force_hibernate == 0){
							hibernate_radiogroup.setValue('no', true);
						}
						else
							hibernate_radiogroup.setValue('allow', true);

					});
				}
			}*/
		});
		add_name_textfield = new Ext.form.TextField({
			fieldLabel		: 'Group Name',
			anchor			: '100%'
		});
		add_desc_textfield = new Ext.form.TextField({
			fieldLabel		: 'Description',
			anchor			: '100%'
		});
		add_policy_combobox = new Ext.form.ComboBox({
			fieldLabel		: 'Policy',
			anchor			: '100%',
			title			: 'Policy',
			typeAhead		: true,
			triggerAction	: 'all',
			lazyRender		: true,
			mode			: 'local',
			store			: policy_combobox_list,
			valueField		: 'PolicyID',
			displayField	: 'PolicyName',
			listeners		: {
				select	: function(cbox, cnt){
					//policy_id_textfield.setValue(cnt.json[0]);
					channel.send(3, 'org.krakenapps.sleepproxy.SleepProxyPlugin.getSleepPolicy', { policy_id: cnt.json[0] }, function(resp2){
						add_policy_id_textfield.setValue( resp2['policy'].id);
						add_policy_combobox.setValue( resp2['policy'].name);
						add_away_textfield.setValue(resp2['policy'].away_criteria);
						if(resp2['policy'].force_hibernate == 0){
							add_hibernate_textfield.setValue('No');
						}
						else
							add_hibernate_textfield.setValue('Allow');

					});
				}
			}
		});
		add_away_textfield = new Ext.form.TextField({
			fieldLabel		: 'Away Mode',
			anchor			: '100%',
			value			: '0',
			readOnly		: true

		});
		add_hibernate_textfield = new Ext.form.TextField({
			fieldLabel		: 'Force Hibernate',
			anchor			: '100%',
			readOnly		: true
		});
		add_policy_id_textfield = new Ext.form.TextField({
			fieldLabel		: 'policyid',
			anchor			: '100%',
			hidden			: true
		});

		groupaddwindow =  new Ext.Window({
			title:'Create New Group',
			width:310,
			height:250,
			closeAction:'hide',
			items:[ new Ext.form.FormPanel({
				//region		: 'west',
				id			: 'addgroupformpanel',
				title		: '',
				width		: 300,
				height		: 220,
				footer		: true,
				items: [
					parent_id_combobox,
					add_name_textfield,
					add_desc_textfield,
					add_policy_combobox,
					add_away_textfield,
					add_hibernate_textfield,
					add_policy_id_textfield
					],
				buttons : [
				{
					text : "Add",
					handler : function(){
						channel.send(3, 'org.krakenapps.sleepproxy.SleepProxyPlugin.createAgentGroup', { parent_id:parseInt(parent_id_combobox.getValue()),name:add_name_textfield.getValue(),description:add_desc_textfield.getValue(),policy_id:parseInt(add_policy_id_textfield.getValue()) }, function(){});
						console.log({ parent_id:parseInt(parent_id_combobox.getValue()),name:add_name_textfield.getValue(),description:add_desc_textfield.getValue(),policy_id:parseInt(add_policy_id_textfield.getValue()) });
						groupaddwindow.hide();
					}
				},{
					text: "Close",
					handler : function(){
						groupaddwindow.hide();
					}
				}
				]
				
				})
			]
		});
	}
	
	function sendGetPolicyRequest(resp) {
		group_name_textfield.setValue(resp['agent_group'].name);
		group_desc_textfield.setValue(resp['agent_group'].description);
		channel.send(3, 'org.krakenapps.sleepproxy.SleepProxyPlugin.getSleepPolicy', { policy_id: resp['agent_group'].policy_id }, function(resp2){
			policy_combobox.setValue( resp2['policy'].name);
			away_textfield.setValue(resp2['policy'].away_criteria);
			if(resp2['policy'].force_hibernate == 0){
				hibernate_radiogroup.setValue('no', true);
			}
			else
				hibernate_radiogroup.setValue('allow', true);

		});
	}

	function sendGetAgentsRequest(group_id) {
		channel.send(1, 'org.krakenapps.sleepproxy.SleepProxyPlugin.getAgents', { group_id: group_id }, function(resp) {
			agentlist = parseAgents(resp['agents']);
			loadDataStore();
			grid.reconfigure(agent_store, agent_col_model);
			channel.send(2, 'org.krakenapps.sleepproxy.SleepProxyPlugin.getAgentGroup', { group_id: group_id }, sendGetPolicyRequest);
		});
	}
	//******************assets 하단 설정 패널 ************
	function createPolicyCombobox() {
		group_id_textfield = new Ext.form.TextField({
			fieldLabel		: 'Group ID',
			anchor			: '100%',
			readOnly		: true
		});
		policy_id_textfield = new Ext.form.TextField({
			fieldLabel		: 'Policy ID',
			anchor			: '100%',
			hidden			: true
		});
		group_name_textfield = new Ext.form.TextField({
			fieldLabel		: 'group name',
			anchor			: '100%',
			hidden			: true
		});
		group_desc_textfield = new Ext.form.TextField({
			fieldLabel		: 'Group desc',
			anchor			: '100%',
			hidden			: true
		});
		policy_combobox = new Ext.form.ComboBox({
			fieldLabel		: 'Policy',
			anchor			: '100%',
			title			: 'Policy',
			typeAhead		: true,
			triggerAction	: 'all',
			lazyRender		: true,
			mode			: 'local',
			store			: policy_combobox_list,
			valueField		: 'PolicyID',
			displayField	: 'PolicyName',
			listeners		: {
				select	: function(cbox, cnt){
					policy_id_textfield.setValue(cnt.json[0]);
					channel.send(3, 'org.krakenapps.sleepproxy.SleepProxyPlugin.getSleepPolicy', { policy_id: cnt.json[0] }, function(resp2){
						policy_combobox.setValue( resp2['policy'].name);
						away_textfield.setValue(resp2['policy'].away_criteria);
						if(resp2['policy'].force_hibernate == 0){
							hibernate_radiogroup.setValue('no', true);
						}
						else
							hibernate_radiogroup.setValue('allow', true);

					});
				}
			}
		});
		away_textfield = new Ext.form.TextField({
			fieldLabel		: 'Away Mode',
			anchor			: '100%',
			value			: '0',
			//disabled		: true,
			readOnly		: true

		});
		hibernate_radiogroup = new Ext.form.RadioGroup({
			fieldLabel		: 'Force Hibernate',
			vertical		: false,
			id				: "hibernate",
			readOnly		: true,
			items: [
				{boxLabel: 'Allow', name: 'id-1', id: 'allow', inputValue: 'A', checked:true},
				{boxLabel: 'No', name: 'id-1', id: 'no', inputValue: 'N'}
				]
		});
	}

	function resetAssetPanel() {
		group_id_textfield.setValue('');
		group_name_textfield.setValue('');
		group_desc_textfield.setValue('');
		policy_combobox.setValue('');
		away_textfield.setValue('');
		hibernate_radiogroup.setValue('no',true);
		policy_id_textfield.setValue('');
	}

	function createAssetPanel() {
		panel_assets = new Ext.form.FormPanel({
			title: '',
			width: 210,
			height: 150,
			region: 'south',
			layout: 'form',
			items: [
				group_id_textfield,
				group_name_textfield,
				group_desc_textfield,
				policy_combobox,
				away_textfield,
				hibernate_radiogroup,
				policy_id_textfield
			],
			buttons : [{
				text	: 'Update',
				handler : function(){
					//console.log({ group_id: parseInt(group_id_textfield.getValue()), name:group_name_textfield.getValue(), description:group_desc_textfield.getValue(), policy_id: parseInt(policy_id_textfield.getValue())});
					channel.send(2, 'org.krakenapps.sleepproxy.SleepProxyPlugin.updateAgentGroup', { group_id: parseInt(group_id_textfield.getValue()), name:group_name_textfield.getValue(), description:group_desc_textfield.getValue(), policy_id: parseInt(policy_id_textfield.getValue())}, function(){});
				}
			},
			{
				text	: 'Remove',
				handler : function(){
					//console.log({ group_id: parseInt(group_id_textfield.getValue()), name:group_name_textfield.getValue(), description:group_desc_textfield.getValue(), policy_id: parseInt(policy_id_textfield.getValue())});
					channel.send(2, 'org.krakenapps.sleepproxy.SleepProxyPlugin.removeAgentGroup', { group_id: parseInt(group_id_textfield.getValue())}, function(){});
					resetAssetPanel();
				}
			}
			],
			listeners: {
				render : function(){
					tree.expandAll();
					//nodes.push({text:'111', children:[]});
				}
			}
		});
	}

	//****************Agent List ***********************************
	function parseAgents(agents) {
		var i_count= agents.length;
		var j;

		agentlist = [];
		for(j=0;j<i_count;j++){
			agentlist.push([ 
				'agent',
				agents[j].id,
				agents[j].group_name,
				agents[j].hostname,
				agents[j].domain,
				agents[j].username,
				agents[j].power_consumption,
				agents[j].created_at,
				agents[j].updated_at
				//'sleep'
			]);
			
		}

		return agentlist;
	}

	var agent_itemDeleter = new Extensive.grid.ItemDeleter();

	var agent_wakeup_column = new Ext.grid.ActionColumn({
		width : 40,
		header : 'Wake',
		align: 'center',
		items : [
		{
			icon   : '../images/default/shared/warning.gif',
			tooltip: 'Wake Up',
			handler: function(grid, rowIndex, colIndex) {
				var rowinfo = agent_store.getAt(rowIndex).json;
				channel.send(1, 'org.krakenapps.sleepproxy.SleepProxyPlugin.wake', {agent_id : rowinfo[1]}, function(resp){
					Ext.MessageBox.alert('Wake Up', "Send 'wake up messages' to " + rowinfo[3]);
				});
			}
		}]
	});
	
	function createAgentGraphWindow(){
		agent_chart_store = new Ext.data.JsonStore({
			fields:['time', 'used', 'saved', 'can_saved'],
			data: [{time : '111', used : 1, saved:2, can_saved :4}]
		});

		agent_power_chart = new Ext.Panel({
			iconCls:'chart',
			title: 'Agent Chart',
			region: 'center',
			frame:true,
			//renderTo: 'container',
			//width:500,
			height:400,
			layout:'',
			items: [{
				xtype:'linechart',
				id : 'agentinnerchart',
				store: agent_chart_store,
				//url:'../../resources/charts.swf',
				height: 350,
				xField: 'time',
				yAxis: new Ext.chart.NumericAxis({
					displayName: 'Time',
					labelRenderer : Ext.util.Format.numberRenderer('0,0')
				}),
			/*	tipRenderer : function(chart, record, index, series){
					if(series.yField == 'visits'){
						return Ext.util.Format.number(record.data.visits, '0,0') + ' visits in ' + record.data.name;
					}else{
						return Ext.util.Format.number(record.data.views, '0,0') + ' page views in ' + record.data.name;
					}
				},*/
				chartStyle: {
					padding: 10,
					//height: 300,
					animationEnabled: true,
					font: {
						name: 'Tahoma',
						color: 0x444444,
						size: 15
					},
					dataTip: {
						padding: 5,
						border: {
							color: 0x99bbe8,
							size:1
						},
						background: {
							color: 0xDAE7F6,
							alpha: .9
						},
						font: {
							name: 'Tahoma',
							color: 0x15428B,
							size: 10,
							bold: true
						}
					},
					xAxis: {
						color: 0x69aBc8,
						majorTicks: {color: 0x69aBc8, length: 4},
						minorTicks: {color: 0x69aBc8, length: 2},
						majorGridLines: {size: 1, color: 0xeeeeee},
						labelRotation : 45
					},
					yAxis: {
						color: 0x69aBc8,
						majorTicks: {color: 0x69aBc8, length: 4},
						minorTicks: {color: 0x69aBc8, length: 2},
						majorGridLines: {size: 1, color: 0xdfe8f6}
					}
				},
				series: [{
					type: 'line',
					displayName: 'Used',
					yField: 'used',
					style: {
						//image:'bar.gif',
						//mode: 'stretch',
						color:0x99BBE8
					}
				},{
					type:'line',
					displayName: 'Saved',
					yField: 'saved',
					//height: 300,
					style: {
						color: 0x15428B
					}
				},{
					type:'line',
					displayName: 'Can Saved',
					yField: 'can_saved',
					style: {
						color: 0x22F28B
					}
				}]
			}]		
		});

		graph_window = new Ext.Window({
			title:'Graph Window',
			width:600,
			height:450,
			closeAction:'hide',
			items:[agent_power_chart]
		});

	}


	var agent_graph_column = new Ext.grid.ActionColumn({
		width : 40,
		header : 'Graph',
		align: 'center',
		items : [
		{
			icon   : '../images/default/icon_graph.gif',
			tooltip: 'Show Graph',
			handler: function(grid, rowIndex, colIndex) {
				var rowinfo = agent_store.getAt(rowIndex).json;
				//Date
				var agent_graph_load_date = new Date();
				var to_load_date= getTimeStamp(agent_graph_load_date);
				var from_load_date = getTimeStamp(new Date(agent_graph_load_date.getTime()-(1000*60*60*1*1))); //default
				channel.send(1, 'org.krakenapps.sleepproxy.SleepProxyPlugin.getAgentPowerGraph', { agent_id : rowinfo[1], from: from_load_date, to: to_load_date }, function(resp){
					agent_chart_store = new Ext.data.JsonStore({
						fields:['time', 'used', 'saved', 'can_saved']
					});

					agent_chart_store.loadData(parseChartData(resp['graph']));
					//console.log(agent_power_chart.findById('agentinnerchart'));
					graph_window.show();

					agent_power_chart.findById('agentinnerchart').bindStore(agent_chart_store);
				
					
				});
			}
		}]
	});

	var agent_column = new Array(
		{	id       :'g_type',		header   : 'g_type',		hidden	 : true,					dataIndex: 'g_type' },
		{	id       :'agent_id',	header   : 'agent_id',		hidden	 : true,					dataIndex: 'agent_id' },
		{	id       :'groupname',	header   : 'Group',			width    : 60,	sortable : true,	dataIndex: 'groupname' },
		{	id       :'hostname',	header   : 'Host',			width    : 60, 	sortable : true,	dataIndex: 'hostname'	},
		{	id       :'domainname',	header   : 'Domain',		width    : 60, 	sortable : true,	dataIndex: 'domainname'},
		{	id       :'username',	header   : 'User', 			width    : 60, 	sortable : true,	dataIndex: 'username'	},
		{	id       :'power',		header   : 'Power', 		width    : 50, 	sortable : true, align: 'center',	 dataIndex: 'power'		},
		{	id       :'created',	header   : 'Created', 		width    : 70, 	sortable : true,	dataIndex: 'created'	},
		{	id       :'updated',	header   : 'Updated', 		width    : 70, 	sortable : true,	dataIndex: 'updated'	},
		//{	id       :'status',		header   : 'Status', 		width    : 50, 	sortable : true,	dataIndex: 'status'	},
		agent_wakeup_column,
		agent_graph_column,
		agent_itemDeleter
	);
	var agent_col_model = new Ext.grid.ColumnModel(agent_column);

	

	function loadDataStore() {
		// create the data store
		agent_store = new Ext.data.ArrayStore({
			fields: [
				{name: 'g_type'},
				{name: 'agent_id'},
				{name: 'groupname'},
				{name: 'hostname'},
				{name: 'domainname'},
				{name: 'username'},
				{name: 'power', type: 'int'},
				{name: 'created'},
				{name: 'updated'}
				//{name: 'status'}
			]
		});

		// manually load local data
		//store.loadData(myData);
		//alert("load data store: " + agentlist.length);
		agent_store.loadData(agentlist);
	}

	function createAgentGrid() {
		// create the Grid
		grid = new Ext.grid.GridPanel({
			id			: 'agentgrid',
			store		: agent_store,
			region		: 'center',
			//columns: agent_column,
			cm			: agent_col_model,
			stripeRows	: true,
			autoExpandColumn: 'domainname',
			//autoExpandColumn: 'groupname',
			height		: 350,
			width		: 570,
			title		: 'Agent List',
			// config options for stateful behavior
			stateful	: true,
			stateId		: 'grid',
			selModel	: agent_itemDeleter,
			autoHeight	: true
		}); //grid

		agentlist_refresh_button = new Ext.Button({
			text	: 'Refresh',
			handler	: function(){
				//console.log(refresh_group_id);
				reloadAgents()
			}
		});
	}

	function reloadAgents(){
		var reloadgroup;
		if(refresh_group_id==0)
			reloadgroup={};
		else
			reloadgroup={group_id : refresh_group_id};
		channel.send(1, 'org.krakenapps.sleepproxy.SleepProxyPlugin.getAgents', reloadgroup, function(resp){
			agentlist = parseAgents(resp['agents']);
			loadDataStore();
			grid.reconfigure(agent_store, agent_col_model);
		});
	}

	//****************Policy Panel ********************************
	var policy_itemDeleter = new Extensive.grid.ItemDeleter();
	var policy_column = new Array(
		{	id :'g_type',				header : 'g_type', 			hidden: true,						dataIndex: 'g_type'	},
		{	id :'policyid',				header : 'ID',				width : 25,		sortable : true,	align: 'center',	dataIndex: 'policyid' },
		{	id :'policyname',			header : 'Name',			width : 70, 	sortable : true,	dataIndex: 'policyname'	},
		{	id :'policydescriptjion',	header : 'Description',		width : 90, 	sortable : true,	dataIndex: 'policydescription'},
		{	id :'policyaway',			header : 'Away',			width : 40,		sortable : true, align: 'center',	dataIndex: 'policyaway'},
		{	id :'policyhibernate',		header : 'Hibernate', width : 60, 	sortable : true, align: 'center',	dataIndex: 'policyhibernate'	},
		{	id :'policycreated',		header : 'Created', 	width : 120, 	sortable : true,	dataIndex: 'policycreated'	},
		{	id :'policyupdated',		header : 'Updated', 	width : 120, 	sortable : true,	dataIndex: 'policyupdated'	},
		policy_itemDeleter
	);
	var policy_col_model = new Ext.grid.ColumnModel(policy_column);
	//var policy_itemDeleter = new Extensive.grid.ItemDeleter();

	function parsePolicies(policies) {
		var i_count= policies.length;
		var j;

		policy_list = [];
		policy_combobox_list = [];
		for(j=0;j<i_count;j++){
			policy_list.push([ 'policy',
				policies[j].id,
				policies[j].name,
				policies[j].description,
				policies[j].away_criteria,
				policies[j].force_hibernate,
				policies[j].created_at,
				policies[j].updated_at
			]);
			policy_combobox_list.push([ policies[j].id,	policies[j].name]);
		}
		return policy_list;
	}

	function loadPolicyDataStore(){
		// create the data store
		policy_store = new Ext.data.ArrayStore({
			fields: [
				{name: 'g_type'},
				{name: 'policyid'},
				{name: 'policyname'},
				{name: 'policydescription'},
				{name: 'policyaway'},
				{name: 'policyhibernate'},
				{name: 'policycreated'},
				{name: 'policyupdated'}
			]
		});
		
		policy_combobox_store = new Ext.data.ArrayStore({
				fields: [ 'PolicyID', 'PolicyName' ]
		});

		policy_store.loadData(policylist);
		policy_combobox_store.loadData(policy_combobox_list);
	}

	function reloadPolicies(){
		channel.send(2, 'org.krakenapps.sleepproxy.SleepProxyPlugin.getSleepPolicies', { }, function(resp){
			policylist = parsePolicies(resp['policies']);
			loadPolicyDataStore();
			policy_grid.reconfigure(policy_store, policy_col_model);
			resetPolicyFormPanel();
		});
	}

	function resetPolicyFormPanel(){
		policy_formpanel_id.setValue('');
		policy_formpanel_id.setVisible(false);
		policy_formpanel_name.setValue('');
		policy_formpanel_description.setValue('');
		policy_formpanel_away.setValue('');
		policy_formpanel_hibernate.setValue('no2',true);
		policy_formpanel_updatebutton.setVisible(false);
		policy_formpanel_cancelbutton.setVisible(false);
		policy_formpanel_createbutton.setVisible(true);
		
		insert_policy_panel.setTitle('Create Policy');
	}

	function createPolicyFormPanel() {
		policy_formpanel_id = new Ext.form.TextField({
			fieldLabel	: 'Id',
			name		: 'id',
			readOnly	: true,
			hidden		: true,
			anchor		: '100%'
		});
		policy_formpanel_name = new Ext.form.TextField({
			fieldLabel	: 'Name',
			anchor		: '100%',
			name		: 'name',
			width		: 150
		});
		policy_formpanel_description = new Ext.form.TextField({
			anchor		: '100%',
			name		: 'description',
			fieldLabel	: 'Description'
		});
		policy_formpanel_away = new Ext.form.TextField({
			anchor		: '100%',
			name		: 'away',
			fieldLabel	: 'Away Criteria',
			inputType	: 'int'
		});
		policy_formpanel_hibernate = new Ext.form.RadioGroup({
			anchor		: '100%',
			name		: 'forcehibernate',
			fieldLabel	: 'Force Hibernate',
			items		: [ { boxLabel: 'Allow', name : 'hibernate', id: 'allow2', inputValue: 1},
				{boxLabel: 'No', name: 'hibernate', id: 'no2', inputValue: 0, checked:true}
			]
		});
		
		policy_formpanel_updatebutton = new Ext.Button({
			text : 'Update',
			hidden : true,
			handler: function() {
				var inputdata = insert_policy_panel.getForm().getValues();
				var fh;
				if(inputdata.hibernate=='0')
					fh = false;
				else
					fh = true;
				channel.send(1, 'org.krakenapps.sleepproxy.SleepProxyPlugin.updateSleepPolicy', { policy_id : parseInt(inputdata.id), name : inputdata.name, description: inputdata.description, away: parseInt(inputdata.away), force_hibernate: fh}, reloadPolicies);
			}
		});
		
		policy_formpanel_cancelbutton = new Ext.Button({
			text : 'Cancel',
			hidden : true,
			handler: resetPolicyFormPanel
		});

		policy_formpanel_createbutton = new Ext.Button({
			text : 'Create',
			handler: function() {
				var inputdata = insert_policy_panel.getForm().getValues();
				var fh = true;
				if(inputdata.hibernate=='0')
					fh = false;
				channel.send(1, 'org.krakenapps.sleepproxy.SleepProxyPlugin.createSleepPolicy', { name : inputdata.name, description: inputdata.description, away: parseInt(inputdata.away), force_hibernate: fh}, reloadPolicies);
			}
		});

		insert_policy_panel = new Ext.form.FormPanel({
			region		: 'west',
			id			: 'insertpolicy',
			title		: 'Create Policy',
			width		: 210,
			height		: 190,
			footer		: true,
			items: [
				policy_formpanel_id,
				policy_formpanel_name,
				policy_formpanel_description,
				policy_formpanel_away,
				policy_formpanel_hibernate
				],
			buttons : [
				policy_formpanel_createbutton,
				policy_formpanel_updatebutton,
				policy_formpanel_cancelbutton
			]
			
		});
	}
	function createPolicyGrid() {
		// create panel
		createPolicyFormPanel();

		// create the Grid
		policy_grid = new Ext.grid.GridPanel({
			id			: 'policygrid',
			store		: policy_store,
			region		: 'center',
			//columns		: policy_column,
			cm			: policy_col_model,
			stripeRows	: true,
			//autoExpandColumn: 'groupname',
			height		: 350,
			//width		: 400,
			title		: 'Policy List',
			// config options for stateful behavior
			stateful	: true,
			stateId		: 'policygrid',
			selModel	: policy_itemDeleter,
			autoHeight	: true
		}); //grid

		policy_grid.on('rowclick', function(g, i, e){
			//console.log(g.getStore().getAt(i));
			rowinfo = g.getStore().getAt(i).json;
			policy_formpanel_id.setValue(rowinfo[1]);
			policy_formpanel_id.setVisible(true);
			//policy_formpanel_type.setValue('update');
			policy_formpanel_name.setValue(rowinfo[2]);
			policy_formpanel_description.setValue(rowinfo[3]);
			policy_formpanel_away.setValue(rowinfo[4]);
			if(rowinfo[5])
				policy_formpanel_hibernate.setValue('allow2',true);
			else
				policy_formpanel_hibernate.setValue('no2',true);
			policy_formpanel_createbutton.setVisible(false);
			policy_formpanel_updatebutton.setVisible(true);
			policy_formpanel_cancelbutton.setVisible(true);

			insert_policy_panel.setTitle('Update Policy');

		});
	
	}

	//*********************** Log Panel ******************************
	function createLogTreePanel() {
		log_tree = new Ext.tree.TreePanel({
			title: 'Agents Groups',
			width: 210,
			height: 350,
			region: 'west',
			root: {
				text: 'Agents',
				children : nodes
			},
			loader: {},
			listeners: {
				click: function(n){				
					if(n.isRoot!=true)
						sendGetLogsRequest(n.id);
				}
				
			}
		});
	}
	function sendGetLogsRequest(group_id){
		//console.log(log_date_panel.getForm().getValues());
		var tmp;
		var tmp2;
		var start_date;
		var end_date;
		var new_date = new Date();

		if(log_date_panel.getForm().getValues().enddt != '' && log_date_panel.getForm().getValues().startdt !=''){
			tmp2 = log_date_panel.getForm().getValues().enddt.split('/');
			end_date = tmp2[2]+'-'+tmp2[0]+'-'+tmp2[1]+' 23:59:59+0900';
			tmp = log_date_panel.getForm().getValues().startdt.split('/');
			start_date = tmp[2]+'-'+tmp[0]+'-'+tmp[1]+' 00:00:00+0900';
		}else{
			end_date = getTimeStamp(new_date);
			start_date = getTimeStamp(new Date(new_date.getTime()-(1000*60*60*1*1)));
		}

		channel.send(1, 'org.krakenapps.sleepproxy.SleepProxyPlugin.getLogs', { group_id : group_id, from: start_date, to: end_date }, function(resp){
			log_store.loadData(parseLogs(resp['logs']));
			log_grid.reconfigure(log_store, log_col_model);
		});

	}

	var log_column = new Array(
		{	id       :'g_type',		header   : 'G',			hidden: true,	dataIndex: 'g_type'		},
		{	id       :'log_id',		header   : 'ID',			width    : 30,	sortable : true, align: 'center',	dataIndex: 'log_id'		},
		{	id       :'hostname',	header   : 'Host Name',		width    : 80, 	sortable : true,	dataIndex: 'hostname'	},
		{	id       :'created',	header   : 'Created Date', 	width    : 150, 	sortable : true,	dataIndex: 'created'	},
		{	id       :'message',	header   : 'Message', 		width    : 220, 	sortable : true,	dataIndex: 'message'	}
		
	);
	var log_col_model = new Ext.grid.ColumnModel(log_column);
	
	function loadLogDataStore() {
		// create the data store
		log_store = new Ext.data.ArrayStore({
			fields: [
				{name: 'g_type'},
				{name: 'log_id'},
				{name: 'hostname'},
				{name: 'created'},
				{name: 'message'}
			]
		});

		log_store.loadData(log_list);
		//log_store.loadData([['1','2','3','sleep'],['6','5','4','wake'],['3','1','2','sleep']]);
	}

	function createLogGrid() {
		// create the Grid
		log_grid = new Ext.grid.GridPanel({
			id			: 'loggrid',
			store		: log_store,
			region		: 'center',
			//columns: agent_column,
			cm			: log_col_model,
			stripeRows	: true,
			//autoExpandColumn: 'groupname',
			height		: 350,
			width		: 600,
			title		: 'Logs',
			// config options for stateful behavior
			stateful	: true,
			stateId		: 'loggrid',
			autoHeight	: true,
			listeners: {
				render : function(){
					log_tree.expandAll();
				}
			},
			autoExpandColumn: 'message'
		}); 
		
	}
	function createLogDatePanel(){
		Ext.apply(Ext.form.VTypes, {
			daterange : function(val, field) {
				var date = field.parseDate(val);

				if(!date){
					return false;
				}
				if (field.startDateField) {
					var start = Ext.getCmp(field.startDateField);
					if (!start.maxValue || (date.getTime() != start.maxValue.getTime())) {
						start.setMaxValue(date);
						start.validate();
					}
				}
				else if (field.endDateField) {
					var end = Ext.getCmp(field.endDateField);
					if (!end.minValue || (date.getTime() != end.minValue.getTime())) {
						end.setMinValue(date);
						end.validate();
					}
				}
				/*
				 * Always return true since we're only using this vtype to set the
				 * min/max allowed values (these are tested for after the vtype test)
				 */
				return true;
			}
		});

		log_date_panel = new Ext.FormPanel({
			labelWidth: 125,
			frame: true,
			title: 'Date Range',
			bodyStyle:'padding:5px 5px 0',
			width: 350,
			height: 90,
			defaults: {width: 175},
			defaultType: 'datefield',
			region: 'north',
			items: [{
				fieldLabel: 'Start Date',
				name: 'startdt',
				id: 'startdt',
				vtype: 'daterange',
				endDateField: 'enddt' // id of the end date field
			},{
				fieldLabel: 'End Date',
				name: 'enddt',
				id: 'enddt',
				vtype: 'daterange',
				startDateField: 'startdt' // id of the start date field
			}]
		});
	}

	function createLogPanel(){
		createLogDatePanel();
		createLogTreePanel();
		loadLogDataStore();
		createLogGrid();
	}

	function parseLogs(logs){
		var i_count= logs.length;
		var j;
		var msg;

		loglist = [];
		for(j=0;j<i_count;j++){
			//console.log(logs[j].status);
			if(logs[j].status=='Suspend')
				msg=logs[j].hostname + " is hibernated";
			else if(logs[j].status=='Resume')
				msg=logs[j].hostname + " is woken up";

			loglist.push([ 
				'log',
				logs[j].id,
				logs[j].hostname,
				logs[j].created_at,
				msg
			]);
		}

		return loglist;
	}
	function getChartDataCallback(){
		channel.send(5, 'org.krakenapps.sleepproxy.SleepProxyPlugin.getGroupPowerGraph', {from : from_date, to : to_date, group_id: 1}, function(resp){
			chartdata_list=parseChartData(resp['graph']);
//			chartdata_list=[];
			createAll();
		});
	}

	function getSleepLogsCallback(){
		//console.log(from_date + " || " +to_date);
		channel.send(4, 'org.krakenapps.sleepproxy.SleepProxyPlugin.getLogs', {from : from_date, to : to_date, group_id: 1}, function(resp){
			//console.log(resp['logs']);
			log_list=parseLogs(resp['logs']);
			//getChartDataCallback();
			getChartDataCallback();
		});


	}

	//****************************************************
	function createAll(){
		createPanelUi();
		createDashboardTreePanel();
		createChart();
		
		loadPolicyDataStore();
		createPolicyGrid();

		createPolicyCombobox();
		createAssetPanel();
		createAgentGraphWindow();
		createTreePanel();
		loadDataStore();
		createGroupAddWindow()
		createAgentGrid();

		createLogPanel();

		Ext.QuickTips.init();
		p = new MyPanel();
		content.add(p);
		content.doLayout();
	}
	function getSleepPoliciesCallback(resp) {
		policylist = parsePolicies(resp['policies']);
		
		getSleepLogsCallback();
		
	}

	function getAgentsCallback(resp) {
		agentlist = parseAgents(resp['agents']);
	
		channel.send(3, 'org.krakenapps.sleepproxy.SleepProxyPlugin.getSleepPolicies', { }, getSleepPoliciesCallback);//ch3
		
		
	}

	function parseAgentGroups(ag){
		var ret_list = [];
		var i,j;
		group_combobox_list = [];
		var rootnode;
		for(i=0;i<ag.length;i++){
			//console.log(asynctreenode_list[i]);
			ret_list.push({
				text		: ag[i].name,
				id			: ag[i].id,
				parent_id	: ag[i].parent_id,
				policy_id	: ag[i].policy_id,
				created_at	: ag[i].created_at,
				updated_at	: ag[i].updated_at,
				description	: ag[i].description,
				children	: []
				});
			group_combobox_list.push([ ag[i].id, ag[i].name]);
			};
		
		return ret_list;
	}

	function getAgentGroupsCallback(resp) {
		var groupcount = resp['agent_groups'].length;
		
	//******* 그룹 가져오기 **************************
		grouplist = parseAgentGroups(resp['agent_groups']);
		AddNode = function (pnode,pi){
			var j;
			for (j=0;j<groupcount ;j++)
			{
				if(grouplist[j].parent_id == pi){
					AddNode(grouplist[j].children, grouplist[j].id);
					pnode.push(grouplist[j]);
				}
			}
		}
		AddNode(nodes,null);
		
		//********에이전트 목록 불러옴 ******************
		channel.send(2, 'org.krakenapps.sleepproxy.SleepProxyPlugin.getAgents', { }, getAgentsCallback);//ch2
	}

	this.onstart = function(pid, args) {
		content = new Ext.Panel({	title: ''});
		var window = windowManager.createWindow(pid, this.name, 800, 500, content); 
		channel.send(1, 'org.krakenapps.sleepproxy.SleepProxyPlugin.getAgentGroups', { "group_id": 1 }, getAgentGroupsCallback);//ch1	
	}
	
	this.onstop = function() {
		console.log("stopped sleep proxy");
	}
}

processManager.launch(new SleepProxy()); 