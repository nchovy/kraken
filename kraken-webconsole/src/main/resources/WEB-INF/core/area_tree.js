var Kraken = {
	ext: {}
};

Kraken.ext.AreaTreePanel = Ext.extend(Ext.tree.TreePanel, {
	constructor: function(cfg, fn) {
		var that = this;
		
		function makeLeaf(obj) {
			var children = [];
			Ext.each(obj.children, function(item, idx) {
				children.push(makeLeaf(item));
			});
			
			var leaf = {
				text: obj.name,
				expanded: true,
				children: children,
				listeners: fn,
				data: obj
			}
			return leaf;
		}
		
		function getAreaTree() {
			channel.send(1, 'org.krakenapps.dom.msgbus.AreaPlugin.getAreaTree', {},
				function(resp) {
					console.log(resp);
					that.setRootNode(new Ext.tree.AsyncTreeNode(makeLeaf(resp.areaTree)));
				}
			);
		}
		
		cfg = Ext.apply({
			root: new Ext.tree.AsyncTreeNode({ text: 'Area', expanded: true, children: [] }),
			listeners: {
				beforerender: getAreaTree
			}
		}, cfg);
		
		Kraken.ext.AreaTreePanel.superclass.constructor.call(this, cfg);
	},
	
	initComponent: function() {
		Kraken.ext.AreaTreePanel.superclass.initComponent.call(this);
	}
});