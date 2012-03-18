Ext.ns('Ext.ux.grid');

Ext.ux.grid.ButtonRowSelectionModel = Ext.extend(Ext.grid.RowSelectionModel,  {
	sortable : false,
	menuDisabled : true,
    fixed : true,
	header : '&nbsp;',
	
	
    constructor: function(config){
        Ext.apply(this, config);
		
		this.addEvents('render', 'afterrender');
        Ext.ux.grid.ButtonRowSelectionModel.superclass.constructor.call(this);
    },
    
	renderer : function(v, p, record){
		var elobj = $('<div>').attr('id', this.id + "_" + record.id);
		
		var btn = new Ext.Button({ 
			text: this.text,
			iconCls: this.iconCls,
			width: this.width,
			listeners: {
				afterrender: function() {
					$('#' + this.id).parent().parent().css('padding', '0').css('margin', '-1px');
				}
			}
		});
		
		elobj.button = btn;
		
		var that = this;
		
		setTimeout(function() {
			btn.render(elobj.attr('id'));
			
			that.fireEvent('afterrender', that, elobj, v, p, record);
		}, 100);
		
		this.fireEvent('render', this, elobj, v, p, record);
		
		var tagtext = $('<div>').append($(elobj).clone()).remove().html(); // jquery tag text
		return tagtext;
		
    }
});