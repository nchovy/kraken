define(["/lib/jquery.js", "/lib/knockout-2.1.0.debug.js", "/component/dd.js"], function(_$, ko, DD) {

var MyGrid;

(function() {

function Spret(el, data, options) {
	defaultoptions = {
		"debug": false,
		"showIndex": true,
		"showColumnGuide": true,
		"showHorizontalScrollbar": true,
		"colDataBind": function() {},

		"onRenderRow": function() {},
		"onRenderRows": function() {},
		"onRender": function() {},
		"onScroll": function() {},
		"onSelect": function() {},
		"onSelectEnd": function() {},
		"headerTemplate": '<span data-bind="text: headerText"></span>'
	};

	options = $.extend(defaultoptions, options);

	var $container, $viewport, $canvas, $colheader, $inputproxy, $defbody;
	var columns, originalColumns;
	var rowHeight, viewportH, pageSize;
	var currOffset = 0;

	var prevHOffset = 0, currHOffset, hscrollDist;

	data.totalCount.subscribe(function(val) {
		scroll.onResize();
		if(currOffset == 0) {
			scroll.scrollTo(0);
		}
	});

	function init() {

		// drawing ui
		$(el).addClass("spret");
		$container = $('<div>').css("overflow-x", (options.showHorizontalScrollbar ? "scroll" : "hidden")).css("overflow-y", "hidden").css("height", "100%").appendTo(el);
		$viewport = $('<div class="spret-viewport" style="overflow: hidden; width: 100%; position: relative; min-width: 100%">').appendTo($container);
		
		$canvas = $('<table class="table table-bordered table-condensed spret-canvas" cellspacing="0" style="position: relative">').appendTo($viewport);

		$colheader = $('<tr>').appendTo($('<thead>').appendTo($canvas));
		$inputproxy = $('<textarea class="ghost">').appendTo($container);

		$container.on("scroll", function(e) {
			currHOffset = $container[0].scrollLeft;
			hscrollDist = currHOffset - prevHOffset;
			prevHOffset = currHOffset;
			//console.log(hscrollDist);
		})

		// columns
		if(typeof data.columns === "function") {
			// flexible columns
			originalColumns = data.columns();

			$defbody = $('<tr><td></td></tr>').appendTo($('<tbody>').appendTo($canvas));
			$colheader.attr("data-bind", "foreach: columns").html('<th>' + options.headerTemplate + '</th>');

			ko.applyBindings(data, $colheader[0]);

			data.columns.subscribe(function(val) {
				//console.log("columns changed")
				columns = $.map(val, function(obj) {
					return obj.rowText;
				});

				if(val.length > 0) {
					$viewport.css("width", (val.length * 10) + "%");

					if(val[0].headerText !== "#") {
						data.columns.splice(0, 0, {
							"headerText": "#"
						});

						$container.find("th:first-child")
							.addClass("spret-idx")
							.width(40)
							.text("#");
					}
				}

				virtualization.render();
			})
		}
		else {
			originalColumns = data.columns;

			if(options.showIndex) {
				$('<th class="spret-idx">').text("#").width(40).appendTo($colheader);
			}

			$.each(originalColumns, function(i, obj) {
				$('<th>').text(obj.headerText).appendTo($colheader);
			});

			$viewport.css("width", (originalColumns.length * 10) + "%");
		}

		columns = $.map(originalColumns, function(obj) {
			return obj.rowText;
		});

		// init variables
		rowHeight = getSingleRowHeight();
		onResize();

		scroll.initScrollBar();	
		
		$inputproxy.on("keydown", inputproxy.initKeyBinding).focus();
		$canvas.on("mousedown", function() {
			setTimeout(function() {
				$inputproxy.focus();
			}, 10);
			
		});

		$container.on("mousewheel", function(e) {
			e.preventDefault();

			if(e.originalEvent.wheelDelta > 0) {
				scroll.direction = -1;
			}
			else {
				scroll.direction = 1;
			}
			
			var currIndex = getCurrentIndex();
			var delta = -e.originalEvent.wheelDelta / 120 * (pageSize / 6);

			if(pageSize > 10) {
				scrollToIndex(currIndex + delta - scroll.direction);
			}
			else {
				var span = (scroll.direction == -1) ? Math.floor(delta - scroll.direction) : Math.ceil(delta - scroll.direction);
				scrollToIndex(currIndex + span);
			}
		});
	}

	function onResize() {
		viewportH = $container.height() - (options.showHorizontalScrollbar ? 17 : 0);
		$viewport.height(viewportH);
		pageSize = Math.ceil(viewportH / rowHeight) - 1;

		scroll.onResize();
		
		if(options.debug || true) { console.log("rowHeight:", rowHeight, "\tpageSize:", pageSize); }
	}

	var selection = {
		$selection: null,
		"colWidth": [],
		"current": null,
		"tdPaddingH": 0,
		init: function() {
			if($container.find("td").length === 0) return;
			this.tdPaddingH = $container.find("td:first-child").css("padding-left").split('px')[0];

			this.current = new selection.Range([0,0],[0,0]);
			this.$selection = $('<div class="spret-selection" style="position: absolute; border: 1px solid rgba(82,168,236,0.8); background-color: #E1E6F6">').prependTo($viewport);
			this.set(this.current);

			$(window).on("resize", this.onResize);
		},
		set: function(range) {
			this.current = range;
			this.$selection.show();
			//console.log(range.from, range.to)
			this.highlight();

			trigger("onSelect", range);
		},
		get: function() {
			return this.current;
		},
		highlight: function() {
			var h = rowHeight;
			var currIndex = getCurrentIndex();
			if($container.find("td").length === 0) return;
			
			var indexWidth = options.showIndex ? $container.find("th:first-child").width() + (this.tdPaddingH * 2) + 2 : 0;
			var toffset = $container.find("td:first-child").offset();
			var top = toffset.top - (currIndex * h) + 1;
			var left = toffset.left + indexWidth + 3;
			var range = this.current;

			// row
			var ycurr = h * (range.from[0]);
			var ysel = h * ((range.to[0] > range.from[0]) ? range.from[0] : range.to[0]);

			if(currOffset == 1) {
				var btmspan = h - $container.find("tbody tr:first-child").height();
				top = top - btmspan;
			}

			this.$selection
					.offset( { "top": top + ysel })
					.height( h * (Math.abs(range.to[0] - range.from[0]) + 1) - 3);

			// col
			var xidxfrom = range.to[1] > range.from[1] ? range.from[1] : range.to[1];
			var xidxto   = range.to[1] > range.from[1] ? range.to[1] : range.from[1];

			this.$selection
					.offset( { "left": left + this.sumWidth(0, xidxfrom) } )
					.width(this.sumWidth(xidxfrom, xidxto + 1));

			if(options.showColumnGuide) {
				$colheader.find("th").css("padding-bottom", "").css("border-bottom", "");
				for(var i = xidxfrom; i <= xidxto; i++) {
					var $header = $($colheader.find("th")[i + ((options.showIndex) ? 1 : 0)]);
					$header.css("padding-bottom", "0").css("border-bottom", "4px solid #fc0")
				}
			}
		},
		sumWidth: function(from, to) {
			var sum = 0;
			if(from > to) {
				for (var i = from; i >= to; i--) {
					sum = sum + this.colWidth[i] + (this.tdPaddingH * 2) + 2;
				}
			}
			else {
				for (var i = from; i < to; i++) {
					sum = sum + this.colWidth[i] + (this.tdPaddingH * 2) + 2;
				}
			}
			return sum - 3;
		},
		getSelectedWidth: function (relx, from, to) {
			function o(left, width, offset) {
				return { "x": left, "w": width, "offset": offset };
			}

			if(relx > selection.sumWidth(from, to)) {
				return selection.getSelectedWidth(relx, from, to + 1)
			}
			else if (relx < 0) {

				if(-relx > selection.sumWidth(from - 1, to - 1)) {
					return selection.getSelectedWidth(relx, from - 1, to);
				}
				else {
					if(from < 1) {
						return o(-selection.sumWidth(from, to - 1), selection.sumWidth(from, to), from);
					}
					else {
						return o(-selection.sumWidth(from - 1, to - 1), selection.sumWidth(from - 1, to), from - 1);
					}
				}
			}
			else {
				return o(0, selection.sumWidth(from, to), to - 1);
			}
		},
		onResize: function() {
			// update colWidth
			var colWidth = [];
			$.each($container.find("tr:first-child td"), function(i, td) {
				var tw = $(td).width();
				colWidth.push(tw);
			});

			if(options.showIndex) {
				colWidth.splice(0, 1);
			}
			selection.colWidth = colWidth;

			// selection resize
			if(!!selection.$selection) {
				selection.set(selection.get());
			}
		},
		initdrag: function(e) {
			var $el = selection.$selection;
			if(e.which !== 1) return;

			var offset = $(this).offset();
			var ir = parseInt($(this).parent("tr").attr("data-row"));
			var ic = parseInt($(this).attr("data-col"));

			var range = new selection.Range([ir, ic], [ir, ic]);
			selection.set(range);

			function onMouseMove(ed) {
				var irt = Math.ceil((ed.clientY - offset.top) / (rowHeight + 1)) - 1;
				var relx = (ed.clientX - offset.left);
				var w = selection.getSelectedWidth(relx, ic, ic + 1);

				range = new selection.Range([ir, ic], [ir + irt, w.offset]);
				selection.set(range)
			}

			function onMouseUp(ed) {
				trigger("onSelectEnd", range);

				$(document).off("mousemove").off("mouseup");
				$(parent.document).off("mousemove");
			}

			$(document).on("mousemove", onMouseMove).on("mouseup", onMouseUp);
			$(parent.document).on("mousemove", onMouseMove)

		}
	}

	selection.Range = function(from, to) {
		if(from[0] < 0) from[0] = 0;
		if(from[1] < 0) from[1] = 0;
		if(to[0] < 0) to[0] = 0;
		if(to[1] < 0) to[1] = 0;
		if(to[0] >= data.totalCount()) to[0] = data.totalCount() - 1;
		
		this.from = from;
		this.to = to;
	}

	var cursor = {
		$cursor: null,
		"current": [0, 0],
		init: function() {
			this.$cursor = $('<div class="spret-cursor" style="position: absolute; border: 1px solid transparent; background-color: #fff">').prependTo($viewport);
			this.set(this.current);

			$(window).on("resize", this.onResize);
		},
		set: function(position) {
			var currIndex = getCurrentIndex();

			// boundary arrange
			if (position[0] < 0) {
				position[0] = 0;
			}
			if (position[0] >= data.totalCount()) {
				position[0] = data.totalCount() - 1;
			}
			if (position[1] < 0) {
				position[1] = 0;
			}
			if (position[1] >= columns.length) {
				position[1] = columns.length - 1;
			}

			//console.log(position)

			this.current = position;
			this.highlight();
		},
		get: function() {
			return [this.current[0], this.current[1]];
		},
		highlight: function() {
			var ir = this.current[0];
			var ic = this.current[1];
			//console.log(ir, ic);

			var rh = rowHeight;

			if(Math.round(data.totalCount() - pageSize) == ir) {
				rh = $container.find("tbody tr:first-child").height();
			}

			var td = $canvas.find("tr[data-row=" + ir + "] td[data-col=" + ic + "]");
			if(td.length == 0) {
				this.$cursor.hide();
			}
			else {
				this.$cursor.show();
				var tdOffset = td.offset();
				var tdPaddingH = td.css("padding-left").split('px')[0];
				
				this.$cursor.offset({ "top": tdOffset.top + 2, "left": tdOffset.left + 2 })
					.width(td.width() + tdPaddingH * 2 - 3)
					.height(rh - 5);
			}
		},
		onResize: function() {
			cursor.set(cursor.get());
		},
		cellclick: function(e) {
			var ir = parseInt($(this).parent("tr").attr("data-row"));
			var ic = parseInt($(this).attr("data-col"));
			//console.log(ir, ic)
			//cursor.$cursor.show();
			cursor.set([ir, ic]);

			trigger("onCellClick", this, e);
		}
	}

	var inputproxy = {
		"timer": null,
		clearInputproxy: function() {
			$inputproxy.val("");
			clearTimeout(inputproxy.timer)
			inputproxy.timer = null;

			setTimeout(function() {
				$inputproxy.focus();
			}, 10);	
		},

		initKeyBinding: function(e) {
			var _KEY_DOWN = 40, _KEY_UP = 38, _KEY_ENTER = 13, _KEY_PGDOWN = 34, _KEY_PGUP = 33, _KEY_HOME = 36, _KEY_END = 35, _KEY_LEFT = 37, _KEY_RIGHT = 39;
			var currIndex = getCurrentIndex();
			var currCur = cursor.get();
			//console.log(e.keyCode)
			if(e.keyCode === _KEY_DOWN) {
				if(currCur[0] >= currIndex + pageSize - 2) {
					scrollToIndex(currIndex + 1);
				}

				if(currCur[0] < currIndex) {
					scrollToIndex(currCur[0] + 1);
				}
				else if(currCur[0] > currIndex + pageSize) {
					scrollToIndex(currCur[0] - pageSize + 2);
				}

				var position = [currCur[0] + 1, currCur[1]];
				cursor.set(position);
				selection.set(new selection.Range(position, position));
			}
			else if (e.keyCode === _KEY_UP) {
				if(currCur[0] == currIndex) {
					scrollToIndex(currIndex - 1);
				}

				if(currCur[0] < currIndex) {
					scrollToIndex(currCur[0] - 1);
				}
				else if(currCur[0] > currIndex + pageSize) {
					scrollToIndex(currCur[0] - pageSize + 1);
				}

				var position = [currCur[0] - 1, currCur[1]];
				cursor.set(position);
				selection.set(new selection.Range(position, position));
			}
			else if (e.keyCode === _KEY_LEFT) {
				if(currCur[0] < currIndex) {
					scrollToIndex(currCur[0]);
				}
				else if(currCur[0] > currIndex + pageSize) {
					scrollToIndex(currCur[0] - pageSize + 2);
				}

				var position = [currCur[0], currCur[1] - 1];
				cursor.set(position);
				selection.set(new selection.Range(position, position));
			}
			else if (e.keyCode === _KEY_RIGHT) {
				if(currCur[0] < currIndex) {
					scrollToIndex(currCur[0]);
				}
				else if(currCur[0] > currIndex + pageSize) {
					scrollToIndex(currCur[0] - pageSize + 2);
				}

				var position = [currCur[0], currCur[1] + 1];
				cursor.set(position);
				selection.set(new selection.Range(position, position));
			}
			else if (e.keyCode === _KEY_PGDOWN) {
				e.preventDefault();

				// method 1
				/*
				var timer, last = currIndex + pageSize - 1;
				var target = currCur[0] + pageSize - 1;
				var span = pageSize;
				var fn = (function() {
					clearTimeout(timer)
					console.log("scroll", currIndex, span);
					if(currIndex > last) {
					}
					else {
						scrollToIndex(currIndex++);
						cursor.set([target, currCur[1]]);
						timer = setTimeout(fn, span--);
					}
				})
				timer = setTimeout(fn, 0);
				*/

				scrollToIndex(currCur[0] + pageSize - 1);

				var position = [currCur[0] + pageSize - 1, currCur[1]];
				cursor.set(position);
				selection.set(new selection.Range(position, position));
			}
			else if (e.keyCode === _KEY_PGUP) {
				e.preventDefault();
				scrollToIndex(currCur[0] - pageSize + 1);

				var position = [currCur[0] - pageSize + 1, currCur[1]];
				cursor.set(position);
				selection.set(new selection.Range(position, position));
			}
			else if (e.keyCode === _KEY_HOME) {
				scrollToIndex(0);

				var position = [0, currCur[1]];
				cursor.set(position);
				selection.set(new selection.Range(position, position));
			}
			else if (e.keyCode === _KEY_END) {
				scrollToIndex(data.totalCount());

				var position = [data.totalCount() - 1, currCur[1]];
				cursor.set(position);
				selection.set(new selection.Range(position, position));
			}
			else if (e.keyCode === _KEY_ENTER) {
				var idx = parseInt($inputproxy.val());
				if(idx !== NaN) {
					if(options.debug) { console.log("go directly:", idx); }
					scrollToIndex(idx);
				}
				inputproxy.clearInputproxy();
			}
			else {
				
				if(inputproxy.timer == null) {
					inputproxy.timer = setTimeout(inputproxy.clearInputproxy, 2000);
				}
				else {
					clearTimeout(inputproxy.timer);
					inputproxy.timer = setTimeout(inputproxy.clearInputproxy, 2000);
				}
			}
		}
	};

	var scroll = {
		$scrollbar: null,
		$thumb: null,
		$scrollUpSpn: null,
		$scrollDnSpn: null,
		"scrollbarH": null,
		"thumbH": null,
		"direction": null,

		onResize: function() {
			var self = scroll;
			if(!!self.$scrollbar) {
				self.scrollbarH = self.$scrollbar.height();
				self.$thumb.height(pageSize / data.totalCount() * self.scrollbarH);
				self.thumbH = self.$thumb.height(); // may $thumb has min-height

				if(currOffset === 1) {
					self.$thumb.css("top", "").css("bottom", "0");
				}
			}
		},

		initScrollBar: function() {
			var self = scroll;
			self.$scrollbar = $('<div class="spret-scrollbar" style="position: absolute; right: 0px">').insertAfter($container);
			self.$scrollUpSpn = $('<div class="spret-scrollspn up" style="position: absolute; top: 0">').appendTo(self.$scrollbar);
			self.$scrollDnSpn = $('<div class="spret-scrollspn down" style="position: absolute; bottom: 0; height: 100%">').appendTo(self.$scrollbar);
			self.$thumb = $('<div class="spret-thumb" style="position: absolute; width: 100%">').appendTo(self.$scrollbar);

			// init scrollbar
			var onStartY = 0;
			var top = self.$scrollbar.offset().top;
			self.onResize();

			var inst = new DD.Draggable(self.$thumb, {
				"axisY": true,
				"axisX": false,
				"thresholdPixel": 0,
				"stopPropagation": true,
				"onStart": function(e, ui) {
					onStartY = ui.position.y;
				},
				"onDrag": function(e, ui, that) {
					var y = ui.offset.y - onStartY;

					if(y < top) {
						self.scrollTo(0, false);
						return false;
					}
					else if(y > top + self.scrollbarH - self.thumbH) {
						self.scrollTo(1, false);
						return false;
					}
					else {
						var ratio = (y - top) / (self.scrollbarH - self.thumbH);
						//console.log(y, top)
						self.scrollTo(ratio, false);
						return false;
					}
				},
				"onStop": function(e, ui, that) {
					self.updateScrollSpan();
				}
			});

			// scroll span 
			var mouseStillDown = false;
			var mouseDownTimer = null;
			var mouseDownPageY;

			function clearMouseDown() {
				mouseStillDown = false;
				clearTimeout(mouseDownTimer);
				mouseDownTimer = null;
				$(document).off("mouseup", clearMouseDown);
			}

			function updateMouseDownPageY(e) {
				mouseDownPageY = e.pageY;
			}

			self.$scrollUpSpn.on("mousedown", function(e) {
				if(e.which != 1) return false;
				mouseStillDown = true;
				updateMouseDownPageY(e);
				self.scrollTo(currOffset - 0.1);
				
				mouseDownTimer = setInterval(function() {
					if( (mouseDownPageY - top) / (self.scrollbarH - self.thumbH) < (currOffset) ) {
						self.scrollTo(currOffset - 0.1);
					}
				}, 100);

				$(document).on("mouseup", clearMouseDown);
			}).on("mousemove", updateMouseDownPageY);

			self.$scrollDnSpn.on("mousedown", function(e) {
				if(e.which != 1) return false;
				mouseStillDown = true;
				updateMouseDownPageY(e);
				self.scrollTo(currOffset + 0.1);

				mouseDownTimer = setInterval(function() {
					if( (mouseDownPageY - top - self.thumbH) / (self.scrollbarH - self.thumbH) > (currOffset) ) {
						self.scrollTo(currOffset + 0.1);
					}
				}, 100);

				$(document).on("mouseup", clearMouseDown);
			}).on("mousemove", updateMouseDownPageY);
		},

		scrollTo: function(ratio, uiupdate) {
			var self = this;
			if(uiupdate === undefined) uiupdate = true;

			if(ratio < 0) ratio = 0;
			else if(ratio > 1) ratio = 1;

			if(ratio >= currOffset){
				self.direction = 1;
			}
			else {
				self.direction = -1;
			}

			//console.log("=================================\t", Math.abs(ratio - currOffset) * 100)

			currOffset = ratio;

			// unset lasttop
			$canvas.find("tr.lasttop td").css("line-height", "").css("color", "");
			$canvas.find("tr.lasttop td *").show();
			$canvas.find("tr.lasttop").removeClass("lasttop");

			$canvas[0].style.top = "0px";
			if(currOffset === 0) {
				self.$thumb[0].style.top = "0px";
			}
			else if(currOffset === 1) {
				self.$thumb[0].style.top = (self.scrollbarH - self.thumbH) + "px";
				
				// set lasttop
				var ch = (viewportH % rowHeight) + 1;
				var lasttop = $canvas.find("tr:first-child").addClass("lasttop");
				lasttop.find("td").css("line-height", ch + "px");
				if(ch < rowHeight / 2.2) {
					lasttop.find("td").css("color", "transparent");
					lasttop.find("td *").hide();
				}

				if(options.debug) { console.log("scroll to bottom"); }
			}
			else {
				self.$thumb[0].style.top = ((self.scrollbarH - self.thumbH) * currOffset) + "px";
			}

			if(uiupdate) {
				self.updateScrollSpan();
			}

			virtualization.render();
			trigger("onScroll", ratio);
		},

		updateScrollSpan: function() {
			var self = this;
			var pos = parseInt(self.$thumb[0].style.top.split("px")[0]) || 0;
			self.$scrollUpSpn.height(pos);
			self.$scrollDnSpn.height(self.scrollbarH - pos);
		}
	};

	var virtualization = {
		"rowsCache": {
			"length": function() {
				var len = 0;
				for(var i in virtualization.rowsCache) {
					len++;
				}
				return len - 2; // 2 is length of rowsCache's functions
			},
			"clear": function() {
				for(var i in virtualization.rowsCache) {
					if(i == "length" || i == "clear") continue;

					delete virtualization.rowsCache[i];
				}
			}
		},

		removeRowFromCache: function(i) {
			var self = this;

			if(!self.rowsCache[i]) return;

			self.rowsCache[i].remove();

			if(i == cursor.current[0]) {
				//cursor.$cursor.hide();
			}
			delete self.rowsCache[i];

		},

		cleanupRows: function(top, bottom) {
			var self = this;
			
			for (var i in self.rowsCache) {
				if( i < top || i > bottom ) {
					self.removeRowFromCache(i);
					if(options.debug) { console.log("removeRowFromCache", i); }
				}
			}
		},

		renderRows: function(top, bottom) {
			var self = this;

			var rows = [];

			for (var i = top; i < bottom; i++) {
				if(self.rowsCache[i]) {
					//console.log("==> cached", self.rowsCache[i]);
					//continue;
				}
				rows.push(i);
			}
			//console.log(self.rowsCache)

			if(scroll.direction == 1) {
				for (var i = 0, ii = rows.length; i < ii; i++) {
					var idx = bottom - ii + i;
					if(self.rowsCache[idx]) {
						if(self.rowsCache[idx].children("td:not(.spret-idx)").length + 1 === columns.length) {
							var r = self.rowsCache[idx];
							var c = drawCol(idx, columns[columns.length - 1]);
							c.appendTo(r)
						}
					} else {
						var r = drawRow(null, idx);
						if(r != null) {
							r.appendTo($canvas);
							if(options.debug) { console.log("renderRow", idx); }
							trigger("onRenderRow", idx, r);
						}	
					}
					
				}
			}
			else if(scroll.direction == -1) {
				for (var i = rows.length - 1, ii = 0; ii <= i; i--) {
					var idx = top - ii + i;
					if(self.rowsCache[idx]) {
						if(self.rowsCache[idx].children("td:not(.spret-idx)").length + 1 === columns.length) {
							var r = self.rowsCache[idx];
							var c = drawCol(idx, columns[columns.length - 1]);
							c.appendTo(r)
						}
					}
					else {
						var r = drawRow(null, idx);
						if(r != null) {
							r.prependTo($canvas);
							if(options.debug) { console.log("renderRow", idx); }
							trigger("onRenderRow", idx, r);
						}
					}
				}
			}

			trigger("onRenderRows", top, bottom, $canvas, {
				"done": function() {
					cursor.onResize();
					selection.onResize();
				}
			});
			//console.log(self.rowsCache)
		},

		render: function() {
			var self = this;
			var top = Math.max(Math.round((data.totalCount() - pageSize) * currOffset), 0);
			var bottom = top + ((pageSize > data.totalCount()) ? data.totalCount() : pageSize);
			if(options.debug) { console.log("--------------->",top, bottom); }

			self.cleanupRows(top, bottom);
			self.renderRows(top, bottom);

			trigger("onRender", top, bottom);
		}
	};

	function getCurrentIndex() {
		return Math.max(Math.round((data.totalCount() - pageSize) * currOffset), 0);
	}

	function scrollToIndex(idx) {
		if(idx > data.totalCount() - pageSize) {
			idx = data.totalCount() - pageSize;
		}
		
		if(idx < 0) {
			idx = 0;
		}

		var ratio;
		if(data.totalCount() === pageSize) {
			if(scroll.direction == 1) {
				ratio = idx + 1;
			}
			else if(scroll.direction == -1) {
				ratio = 0;
			}
		}
		else {
			ratio = idx / (data.totalCount() - pageSize);
		}

		if(options.debug) { console.log("==========> idx:",  idx, "\tratio:", ratio) };

		scroll.scrollTo(ratio);

	}



	function trigger(type) {
		if(!!options[type]) {
			var params = Array.prototype.slice.call(arguments);
			options[type].apply(this, params.slice(1, params.length));
		}

		if(type === "onRenderRows") {
			selection.onResize();
		}

		if(type === "onRender") {
			if(options.debug) { console.log("render", params[0], params[1]); }
			//interaction.init();
		}

		if(type === "onScroll") {
			if(!!selection.$selection) {
				var currIndex = getCurrentIndex();
				
				if( (Math.min(selection.current.from[0], selection.current.to[0]) > currIndex + pageSize) )
				{
					selection.$selection.hide();
					return;
				}

				if( (Math.max(selection.current.from[0], selection.current.to[0]) < currIndex) ) {
					selection.$selection.hide();
					return;
				}
			}

			var range;
			if(!!selection.$selection) {
				//console.log("scroll set")
				selection.$selection.show();
				range = new selection.Range(selection.current.from, selection.current.to);
			}

			if(!!cursor.$cursor) {
				cursor.set(cursor.current);
			}

			if(!!selection.$selection) {
				selection.set(range);
			}
		}
	}

	function getSingleRowHeight() {
		//var row = data.data()[0];
		var row = {"sample": "data"};
		
		var h = $viewport.height();
		var $row = drawRow(row, 0);
		$row.appendTo($canvas);
		var rowHeight = $row.height();
		$row.remove();
		
		delete virtualization.rowsCache[0];

		return rowHeight;
	}

	function drawCol(rowidx, column, row) {
		var colidx = columns.indexOf(column);
		var coldiv = $('<td class="spret-col">').attr("data-col", colidx);
		if(!!originalColumns[colidx].formatter) {
			///////////////////////////////formatter///////////////////////
			var html = originalColumns[colidx].formatter.html().toString().trim();
			var fnbody = html.split("{{")[1].split("}}")[0];
			var fn = new Function("irow", "prop", fnbody);
			//console.log(fn(rowidx, prop));

			html = html.split("{{")[0] + fn(rowidx, column) + html.split("}}")[1];

			coldiv.html(html);
			////////////////////////////////////////////////////////////////
		}
		else {

			if(!!options.colDataBind) {
				coldiv.attr("data-bind", options.colDataBind(rowidx, colidx, column))
			}
			
			if(row == undefined) {
				var val = '<span class="muted">...</span>';
				coldiv.html(val);
			}
			else {
				//if(options.debug) { console.log("has row"); }
				var val;
				if(typeof row[column] === "function") {
					val = row[column]();
				}
				else {
					val = row[column];
				}
				coldiv.text(val);
			}
		}
		coldiv.on("mousedown", cursor.cellclick).on("mousedown", selection.initdrag);
		return coldiv;
	}

	function drawRow(row, rowidx) {
		
		var rowdiv = $('<tr class="spret-row">');
		if(rowidx > data.totalCount()) {
			rowdiv = null;
			//console.log("limit");
		}
		
		if(options.showIndex) {
			$('<td class="spret-col spret-idx">').text(rowidx + 1).appendTo(rowdiv);
		}

		if(row == undefined) {
			if(data.data != null) {
				row = data.data()[rowidx];
			}
		}

		//console.log("drawRow", columns)
		$.each(columns, function(colidx, prop) {
			var coldiv = drawCol(rowidx, prop, row)
			coldiv.on("mousedown", cursor.cellclick).on("mousedown", selection.initdrag).appendTo(rowdiv);
		});

		if(rowdiv != null) {
			rowdiv.attr("data-row", rowidx);
			virtualization.rowsCache[rowidx] = rowdiv;
		}

		return rowdiv;
	}

	init();
	scroll.scrollTo(0);

	cursor.init();
	selection.init();
	if(typeof data.columns === "function") {
		$defbody.remove();
	}

	$(window).on("resize", onResize)

	if(options.debug) { console.log("rowHeight:", rowHeight, "\tpageSize:", pageSize); }

	return {
		"scrollTo": scroll.scrollTo,
		"scrollToIndex": scrollToIndex,
		"getCurrentIndex": getCurrentIndex,
		"getPageSize": function() {
			return pageSize;
		},
		"clear": function() {
			$canvas.find("tbody").empty();
			virtualization.rowsCache.clear();
			setTimeout(function() {
				scroll.scrollTo(0);	
			}, 1000);
		}
	}

}

MyGrid = Spret;


})();

return MyGrid;

});