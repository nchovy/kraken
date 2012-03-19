Command = function() {
	this.name = "Developer Console";
	
	this.onstart = function(pid, args) {	
		var txtarea = new Ext.form.TextArea({
			region: 'north',
			split: true,
			border: false,
			boxMinHeight: 50,
			value: ''
		});	
		
		var tpl = '<div>[<span style="color:blue">';
		var clipboardBtnHTML = "<object id='clipboard" + pid + "' style='margin-top: -2px' codebase='http://download.macromedia.com/pub/shockwave/cabs/ flash/swflash.cab#version=9,0,0,0' width='16' height='16' align='middle'><param name='allowScriptAccess' value='always' /><param name='allowFullScreen' value='false' /><param name='movie' value='/js/clipboard.swf' /><param name='quality' value='high' /><param name='bgcolor' value='#ffffff' /><param name='wmode' value='transparent' /><param name='flashvars' value='callback=Command.CopyToClipboard' /><embed src='/js/clipboard.swf' flashvars='callback=Command.CopyToClipboard' quality='high' swliveconnect='true' bgcolor='#ffffff' width='16' height='16' wmode='transparent' name='clipboard' align='middle' allowscriptaccess='always' allowfullscreen='false' type='application/x-shockwave-flash' pluginspage='http://www.adobe.com/go/getflashplayer' /></object>";
		
		function DoDoDo(olleh) {
			var raw = txtarea.getRawValue();
			var cmd = raw.split(' ', 1)[0];
			var param = $.trim(raw.substring(raw.split(' ', 1)[0].length));
			
			if(param == "") {
				param = { };
			}
			else {
				try {
					param = JSON.parse(param);
				}
				catch(err) {
					param = { };
					pnlResult.add(new Ext.form.Label({html:tpl + new Date().format("A h:i:s") + '</span>]&nbsp;' + err + '</div>'}));
					pnlResult.doLayout();
					var objDiv = document.getElementById(pnlResult.body.id);
					objDiv.scrollTop = objDiv.scrollHeight;
				}
			}
			
			channel.send(1, cmd, param, function(resp) {
				Command.copyText = JSON.format(resp);
				pnlResult.add(new Ext.form.Label({html:tpl + new Date().format("A h:i:s") + '</span>]<br/><pre style="font-family:Courier New; font-size: 1.1em">' + JSON.format(resp) + '</pre></div>'}));
				pnlResult.doLayout();
				var objDiv = document.getElementById(pnlResult.body.id);
				objDiv.scrollTop = objDiv.scrollHeight;
			},
			function(resp) {
				Command.copyText = JSON.format(resp);
				pnlResult.add(new Ext.form.Label({html:tpl + new Date().format("A h:i:s") + '</span>]<br/><pre style="font-family:Courier New; font-size: 1.1em">' + JSON.format(resp) + '</pre></div>'}));
				pnlResult.doLayout();
				var objDiv = document.getElementById(pnlResult.body.id);
				objDiv.scrollTop = objDiv.scrollHeight;
			});	
		}
		
		function clear() {
			pnlResult.removeAll();
			pnlResult.doLayout();
		}
		
		var pnlResult = new Ext.Panel({
			split: true,
			boxMinHeight: 100,
			autoScroll: 'auto',
			tbar: [
				{
					xtype: 'button',
					text: 'Run',
					listeners: {
						afterrender: function(c) {
							c.el.dom.style.setProperty('width', '100%');
							var tbl = $('#' + c.container.id).parent().parent().parent();
							tbl.width('100%');
						}
					},
					iconCls: 'ico-start',
					handler: DoDoDo
				},'->','-',
				{
					xtype: 'button',
					text: 'Clear',
					handler: clear
				},'-',
				{
					xtype: 'button',
					text: clipboardBtnHTML,
					listeners: {
						afterrender: function(c) {
							c.btnEl.dom.style.setProperty('height', '18px');
							c.btnEl.dom.style.setProperty('margin-top', '-2px');
						}
					}
				}
			],
			
			region: 'center'
		});

		var MainUI = new Ext.TabPanel({
			activeTab: 0,
			border: false,
			defaults: {
				xtype: 'panel',
				layout: 'border',
				border: false,
				split: true
			},
			items: [
				{
					title: 'MessageBus',
					items: [txtarea, pnlResult]
				},
				{
					title: 'Stream',
					tbar: {
						xtype: 'toolbar',
						items: [
							{
								xtype: 'label',
								html: '&nbsp;Streaming'
							},
							{
								xtype: 'button',
								enableToggle: true,
								text: 'Enable',
								pressed: channel.streaming,
								handler: function(c) {
									var bool = channel.streaming;
									channel.streaming = !bool;
									c.toggle(!bool);
								}
							},
							'->',
							{
								xtype: 'button',
								text: 'Clear',
								handler: function() {
									channel.clearStream();
								}
							}
						]
					},
					items: [
						{
							xtype: 'grid',
							region: 'center',
							store: channel.getStreamStore(),
							viewConfig: { forceFit: true },
							columns: [
								{
									xtype: 'gridcolumn',
									dataIndex: 'type',
									header: '&nbsp;',
									sortable: true,
									width: 30
								},
								{
									xtype: 'gridcolumn',
									dataIndex: 'method',
									header: 'Method',
									sortable: true
								},
								{
									xtype: 'gridcolumn',
									dataIndex: 'time',
									header: 'Time',
									sortable: true,
									width: 60,
									renderer: function(v) {
										return v.format("Y-m-d H:i:s");
									}
								}
							]
						}
					]
				}
			]
		});
		var window = windowManager.createWindow(pid, this.name ,600,400, MainUI);
		
	}
	
	this.onstop = function() {
	}
}

Command.copyText = "";

Command.CopyToClipboard = function() {
	if (window.clipboardData)
		window.clipboardData.setData('text', Command.copyText);
	else
		return (Command.copyText);
}

processManager.launch(new Command()); 