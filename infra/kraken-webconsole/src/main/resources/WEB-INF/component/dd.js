define([], function() {

	var _droppable;
	var _draggable;

	function getEventObject(dragTarget, e, elp) {
		return {
			"dragTarget": dragTarget,
			"offset": {
				"y": e.pageY,
				"x": e.pageX
			},
			"position": {
				"x": elp.x,
				"y": elp.y
			}
		};
	}

	function Droppable(el, option) {
		var that = this;
		el = $(el);

		this.onDrop = option.onDrop;


		el.on("mouseover", function(e) {
			_droppable = that;

			if(!!_draggable) {
				$(this).addClass(option.activeClass);
			}

		})
		.on("mouseout", function(e) {
			_droppable = null;

			if(!!_draggable) {
				$(this).removeClass(option.activeClass);
			}
		})
		.on("mouseup", function(e) {
			if(!!_draggable) {
				$(this)
					.css("opacity", ".2")
					.animate({
						opacity: 1
					}, 300, function() {
						$(this).removeClass(option.activeClass);
					});
			}
		});
	}
	
	function Draggable(el, option) {
		var that = this;
		var defaultOption = {
			"dragTarget": el,
			"thresholdPixel": 20,
			"axisY": true,
			"axisX": true,
			"method": "coordinate", // "translate"
			"initX": 0,
			"initY": 0,
			"stopPropagation": false
		};
		
		option = $.extend(defaultOption, option);
		
		if(!/(coordinate|translate)/.test(option.method)) {
			throw new TypeError("unexpected method");
		}
		
		//console.log(option);
		
		var dragHandler = $(el);
		var dragTarget = $(option.dragTarget);
		
		dragHandler.on("mousedown", function(ee) {
			ee.preventDefault();
			if(option.stopPropagation) {
				ee.stopPropagation();
			}

			var initp = {
				x: ee.pageX,
				y: ee.pageY
			};
			var elp = {
				x: ee.offsetX,
				y: ee.offsetY
			};
			
			var isDraggable = false;
			
			function onStop(e) {
				//console.log("onStop");
				var ui = getEventObject(dragTarget, e, elp);
				
				$(document).off("mousemove").off("mouseup");
				$(parent.document).off("mousemove").off("mouseup");
				dragHandler.off("mousemove");
				dragTarget.css("-webkit-transition", "");
				
				option.onStop(ee, ui, that);
			}

			function onMouseOver(e) {
				
				if( Math.abs(initp.x - e.pageX) < option.thresholdPixel && Math.abs(initp.y - e.pageY) < option.thresholdPixel ) return;
				
				if(!isDraggable) {
					
					isDraggable = true;

					var ui = getEventObject(dragTarget, ee, elp);
					option.onStart(ee, ui);
					
					if(option.method === "translate") {
						dragTarget.css("-webkit-transition", "0ms");
					}
					
				}
				else {
					var ui = getEventObject(dragTarget, e, elp);
					
					if(option.onDrag(e, ui, that)) {
						if(option.method === "translate") {
							
							var x = (ui.offset.x - ui.position.x + option.initX);
							dragTarget.css("-webkit-transform", "translate(" + x + "px, 0px) translateZ(0px)");
						}
						else if(option.method === "coordinate") {
							if(option.axisX) {
								var x = (ui.offset.x - ui.position.x + option.initX);
								dragTarget.offset({ "left": x, });
							}
							if(option.axisY) {
								var y = (ui.offset.y - ui.position.y + option.initY);
								dragTarget.offset({ "top": y });
							}
						}
					}
				}
			}
			
			var onStopOccured = false;
			
			$(document).on("mousemove", onMouseOver).on("mouseup", onStop);
			$(parent.document).on("mousemove", onMouseOver).on("mouseup", onStop);
			
		});
	}

	return {
		Draggable: Draggable,
		Droppable: Droppable,
		dragabble: _draggable,
		droppable: _droppable
	}

});