TaskManager = function() {
    this.name = "Task Manager";

    this.onstart = function(pid, args) {

        var MyGrid = new Ext.grid.GridPanel({
			tbar: {
				xtype: 'toolbar',
				items: [
					{
						xtype: 'button',
						text: 'Kill',
						iconCls: 'ico-remove',
						handler: function() {
							var selected = MyGrid.getSelectionModel().getSelected().data;
							$.each(windowManager.getWindows(), function(idx, win) {
								if(selected.pid == win.pid) {
									win.destroy();
								}
							});
						}
					}
				]
			},
			sm: new Ext.grid.RowSelectionModel({singleSelect:true}),
            store: processManager.getProcessStore(),
            height: 300,
            columns: [
				{
					header: 'PID',
					sortable: true,
					width: 100,
					align: 'right'
				},
				{
					header: 'Plugin Name',
					sortable: true,
					width: 200
				}
			]
        });


		var window = windowManager.createWindow(pid, 'Task Manager', 500, 300, MyGrid);
	}

	this.onstop = function() {
	}
}

processManager.launch(new TaskManager()); 