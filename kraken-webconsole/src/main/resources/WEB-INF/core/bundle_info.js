Bundle.Info = function(config) {
	Ext.apply(this, config); // parent, bundle, callback
	var that = this;
	
	console.log(that.bundle);
	
	var propgrid = new Ext.grid.PropertyGrid({
		region: 'center',
		border: false,
		source: that.bundle,
		viewConfig: { forceFit: true }
	});
	
	var panel = new Ext.Panel({
		items: propgrid,
		layout: 'border',
		border: false
	});
	
	var windowInfo;
	
	this.onstart = function() {
		windowInfo = windowManager.createChildWindow({
			title: that.bundle.name + ' ' + that.bundle.version,
			width: 450,
			height: 200,
			items: panel,
			parent: that.parent,
			modal: false,
			maximizable: false,
			resizable: true,
			listeners: {
				beforeclose: function() {
					if(that.callback != null) 
						that.callback();
				},
				activate: function() {
					this.el.dom.style.setProperty('opacity', '1');
				},
				deactivate: function() {
					this.el.dom.style.setProperty('opacity', '0.7');
				}
			},
			//style: 'opacity: 0.9',
			onupdate: function(newbundle) {
				delete that.bundle;
				that.bundle = newbundle;
				//console.log(this);
				this.toFront();
				
				that.onupdate();
			}
		});
		
		return windowInfo;
	};
	
	this.onupdate = function() {
		windowInfo.setTitle(that.bundle.name + ' ' + that.bundle.version);
	
		propgrid.setSource(that.bundle);
	}
}