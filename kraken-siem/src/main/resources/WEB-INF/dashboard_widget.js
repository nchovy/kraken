Sample = function() {
    this.name = "Task Manager";

    this.onstart = function(pid, args) {
		
		var anotherpanel = new Ext.Panel({
			title: 'another',
			html: 'anotther apnle'
		});
		
		PanUi = Ext.extend(Ext.Panel, {
			initComponent: function() {
				this.title = 'Pan';
				this.items = [
					{
						xtype: 'panel',
						tbar: {
							xtype: 'toolbar',
							items: [
								{
									text: 'add',
									iconCls: 'ico-add'
								}
							]
						}
					}
				]
				PanUi.superclass.initComponent.call(this);
			}
		});
		
		var panel = new Ext.Panel({
			title: 'test',
			html: 'asdfasdf',
			items: new PanUi()
		});
		
		var window = windowManager.createWindow(pid, 'Task Manager', 500, 300, panel);

	}

	this.onstop = function() {
	}
}


processManager.launch(new Sample());