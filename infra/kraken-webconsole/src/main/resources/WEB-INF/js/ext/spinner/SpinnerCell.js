Ext.ns('Ext.ux.grid');

Ext.ux.grid.SpinnerRowSelectionModel = Ext.extend(Ext.grid.RowSelectionModel,  {
	sortable : false,
	menuDisabled : true,
    fixed : true,
	header : '&nbsp;',
	
    constructor: function(config){
        Ext.apply(this, config);
		
		this.addEvents('render');
        Ext.ux.grid.SpinnerRowSelectionModel.superclass.constructor.call(this);
    },
    
	
	renderer : function(v, p, record){
		var elobj = $('<div>').attr('id', this.id + "_" + record.id);
		
		var sf = new Ext.ux.form.SpinnerField({
			width: this.width,
			minValue: 0,
			maxValue: 100000,
			allowDecimals: true,
			decimalPrecision: 1,
			incrementValue: 100,
			alternateIncrementValue: 300,
			accelerate: true,
			value: record.data[this.dataIndex],
			listeners: {
				change: function(c, n, o) {
					record.data.interval = n;
					record.json.interval = n;
				}
			}
		});
		
		var fp = new Ext.FormPanel({
			labelWidth: 1,
			frame: false,
			bodyStyle: 'background: none; border: 0; margin: 0',
			width: this.width,
			items: [sf],
			listeners: {
				afterrender: function() {
					$('#' + this.id).parent().parent().css('padding', '0').css('margin', '-1px');
				}
			}
		});
		
		setTimeout(function() {
			fp.render(elobj.attr('id'));
			
			// 디자인적 요소
			fp.el.dom.style.setProperty('padding', '0');
			fp.el.dom.style.setProperty('margin', '0');
			fp.container.dom.style.setProperty('padding', '0');
			fp.container.dom.style.setProperty('margin', '0');
			
			var itm = $('#' + fp.id).find('.x-form-item');
			itm.css('margin', '0').css('padding', '0');
			itm.find('label.x-form-item-label').css('padding', '0');
			itm.find('.x-form-element').css('padding', '0');
		}, 10);
		
		this.fireEvent('render', this, elobj, v, p, record);
		
		var tagtext = $('<div>').append($(elobj).clone()).remove().html(); // jquery tag text
		return tagtext;
		
    },
});