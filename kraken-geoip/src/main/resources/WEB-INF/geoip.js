GeoIP = function() {
	this.name = "IP Location";
	
	this.onstart = function(pid, args) {
		programManager.loadJS('/geoip/Ext.ux.GMapPanel.js');
		
		function locate(ip) {
			channel.send(1, 'org.krakenapps.geoip.msgbus.GeoIpPlugin.locate', 
			{
				'ip': ip
			},
			function(resp) {
				console.log(resp);
				var fulladdr = '<span class="mapInfo"><b><img src="img/16-image.png" style="vertical-align: absmiddle; padding-right: 5px">'
					+ ip + '</b><br/><img src="img/marker-16.png" style="vertical-align: absmiddle; padding-right: 5px">'
					+ resp.city.replace(/\"/g, '') + ', '
					+ resp.region.replace(/\"/g, '') + ', '
					+ resp.country
					+ '</span><br/><span class="mapInfo" style="font-size: 9pt; color: #666; padding-left: 20px">'
					+ resp.latitude + ', ' + resp.longitude + '</span>';
				var marker = GMap.addMarker(new GLatLng(resp.latitude, resp.longitude), null, false, true);
				GEvent.addListener(marker, 'click', function() {
					marker.openInfoWindowHtml(fulladdr);
				});
				marker.openInfoWindowHtml(fulladdr);
			});
		}
		
		var panel = new Ext.Panel({
			layout: 'border',
			border: false,
			tbar: {
				xtype: 'toolbar',
				items: [
					txtIP = new Ext.form.TextField({
						style: 'margin-right: 2px',
						listeners: {
							specialkey: function(field, e) {
								if(e.getKey() == e.ENTER) {
									locate(field.getValue());
								}
							}
						},
						emptyText: 'Enter IP Address'
					}),
					{
						xtype: 'button',
						iconCls: 'ico-marker',
						text: 'Mark&nbsp;',
						handler: function() {
							locate(txtIP.getValue());
						}
					},
					'->',
					{
						xtype: 'button',
						text: 'Clear Markers',
						handler: function() {
							GMap.getMap().clearOverlays();
						}
					}
				]
			},
			items: [
				GMap = new Ext.ux.GMapPanel({
					region: 'center',
					zoomLevel: 1,
					gmapType: 'map',
					mapConfOpts: ['enableScrollWheelZoom','enableDoubleClickZoom','enableDragging'],
					mapControls: ['GSmallMapControl','GMapTypeControl','NonExistantControl'],
					setCenter: {
						lat: 37.487459,
						lng: 126.893957,
						//marker: { title: 'TSG' }
					},
					markers: [],
					listeners: {
						afterrender: function() {
							GMap.gmap.setUIToDefault();
						},
						resize: function() {
							if(GMap.gmap != null) {
								GMap.gmap.checkResize();
							}
						}
					}
				})
			]
		});	
		
		var window = windowManager.createWindow(pid, this.name, 650, 420, panel);
		
	}
	
	this.onstop = function() {
		
	}
}

processManager.launch(new GeoIP());