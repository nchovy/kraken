define(["/lib/jquery.js", "/lib/knockout-2.1.0.debug.js", "/lib/d3.v2.amd.js", "/component/kuro.js", "/component/data.js"], function(_$, ko, d3, $K, Data) {
	var componentName = "Kuro.Chart.Column";
	var Column = $K.namespace("Chart.Column");

	function tweenValue(fn, oldval, newval, time, callback) {
		var loop = Math.round(time / 20);
		var diff = newval - oldval;
		var span = diff / loop;

		var v = oldval;

		for(var i = 0; i < loop; i++) {
			setTimeout(function() {
				v = v + span;
				fn.call(null, v);
			}, 20 * (i + 1));
		}

		if(!!callback) {
			setTimeout(callback, time);
		}
	}

	function updateScaleY(self, maxValue) {
		if(!maxValue) maxValue = self.getMaxValue();

		// update scaleY
		var oldval = self.scaleY();
		var newval = (self.viewHeight() - 60) / maxValue;
		
		tweenValue(function(d) {
			self.scaleY ( d );
		}, oldval, newval, 200);
		
		//self.scaleY ( (self.viewHeight() - 60) / maxValue ); // no animation
	}

	function updateRefLineY(self, maxValue, scaleY) {
		console.log("updateRefLineY")
		var yfn = d3.scale.linear()
					.domain([0, maxValue])
					.range([0, maxValue]);

		var newRefLineY = d3.nest().map(yfn.ticks(5)).map(function(d) {
			return {
				"value": ko.observable(Math.floor(yfn(d))),
				"scaled": ko.observable(Math.floor(yfn(d)) * scaleY)
			}
		});

		if(newRefLineY.length == 0) return;
		//console.log(newRefLineY)
		var mtop = 140 - newRefLineY[newRefLineY.length - 1].scaled(); // set mTop

		self.refLineYMax(getRefLineYMax(newRefLineY))

		$.each(newRefLineY, function(i, obj) {
			if(self.refLineY().length > i) {
				self.refLineY()[i].value(obj.value());

				// animate value
				var oldval = self.refLineY()[i].scaled();
				var newval = obj.scaled() + mtop;

				tweenValue(function(d) {
					if(self.refLineY()[i] != undefined) {
						self.refLineY()[i].scaled(d);
					}
				}, oldval, newval, 200);

				//self.refLineY()[i].scaled(obj.scaled()); // no animate
			}
			else {
				obj.scaled( obj.scaled() + mtop )
				self.refLineY.push(obj);
			}
		})

		if(newRefLineY.length < self.refLineY().length) {
			var d = self.refLineY().length - newRefLineY.length;
			var len = self.refLineY().length

			for(var i = len; i > newRefLineY.length ; i--) {
				self.refLineY.splice(i - 1, 1);
			}
		}
	}

	function extendProperty(item, len, self) {
		item.bar = {
			posX: len * 70,
			posY: ko.computed(function() {
				return 140 - item.value() * self.scaleY();
			}),
			height: ko.computed(function() {
				var h = item.value() * self.scaleY();
				if(h < 0) h = 0;
				h = h.toFixed(7);

				return h;
			}),
			label: ko.observable(item[self.labelKey])
		};
	}

	function getRefLineYMax(refLineYvals) {
		return d3.max(refLineYvals.map(function(obj) {
			return obj.value();
		}));
	}

	function makeObservable(self, obj, valueKey) {
		var oldval = 0;
		function assignOldValue(_oldval) {
			oldval = _oldval;
		}

		function assignNewValue(_newval) {
			subscriptionBefore.dispose();
			subscriptionAfter.dispose();

			var subself = this;
			subself.target(oldval);

			if(_newval > self.getMaxValue()) {
				var maxValue = _newval;
				var scaleY = (self.viewHeight() - 60) / maxValue;
				// update scaleY
				updateScaleY(self, maxValue)

				// update refLineY
				updateRefLineY(self, maxValue, scaleY);
			}

			setTimeout(function() {

				tweenValue(function(d) {
					//console.log("tween");
					subself.target(d);

				}, oldval, _newval, 200, function() {

					subself.target(_newval);

					//console.log("subscribe");
					subscriptionBefore = obj[valueKey].subscribe(assignOldValue, this, "beforeChange");
					subscriptionAfter = obj[valueKey].subscribe(assignNewValue);

				});

			}, 200);
		}

		var subscriptionBefore = obj[valueKey].subscribe(assignOldValue, this, "beforeChange");
		var subscriptionAfter = obj[valueKey].subscribe(assignNewValue);
	}

	Column.ViewModel = function(_data, options) {
		var self = this;
		var data = $.extend([], _data);

		this.viewHeight = ko.observable(200);

		// initialize ViewModel
		Data.ViewModel.call(this, data);

		options = $.extend({}, options);

		this.valueKey = options.valueKey;
		this.labelKey = options.labelKey;
		

		// initialize value array
		var vals = data.map(function(d) {
			return d[options.valueKey]();
		})

		// scale
		this.scaleY = ko.observable( (self.viewHeight() - 60) / (d3.max(vals) || 1) );

		$.each(data, function(i, obj) {
			// extended props
			extendProperty(obj, i, self)
			
			// observe changing value
			makeObservable(self, obj, options.valueKey)
		})

		// refLine
		var yfn = d3.scale.linear()
					.domain([0, d3.max(vals)])
					.range([0, d3.max(vals)]);

		var _refLineY = d3.nest().map(yfn.ticks(5)).map(function(d) {
			return {
				"value": ko.observable(Math.floor(yfn(d))),
				"scaled": ko.observable(Math.floor(yfn(d)) * self.scaleY())
			}
		})
		
		this.refLineY = ko.observableArray(_refLineY);
		this.refLineYMax = ko.observable(getRefLineYMax(this.refLineY()));

		// getMaxValue
		this.getMaxValue = ko.computed(function() {
			var max;
			$.each(self.items(), function(i, obj) {
				var curr = obj.value();
				if(max === undefined) {
					max = curr;
				}
				else {
					max = Math.max(max, curr);
				}
			});

			return max;
		});


		this.over = function(data, e) {
			var offset = $(self.el).find("g").offset();

			var top = (data.bar.posY() < 30) ? 30 : data.bar.posY();

			$(".mychart").find(".tooltip")
				.addClass("in")
				.offset({ "top": top + offset.top - 30, "left": data.bar.posX + offset.left + 25 })
				.find(".tooltip-inner")
					.text(data.value())

		}

		this.out = function(data) {
			$(".mychart").find(".tooltip.in").removeClass("in")
		}
	}

	Column.ViewModel.inherit(Data.ViewModel);

	Column.ViewModel.prototype.add = function(item) {
		var self = this;
		var maxValue = Math.max(item.value(), self.getMaxValue());
		var scaleY = (self.viewHeight() - 60) / maxValue;

		// update scaleY
		updateScaleY(self, maxValue)
		
		// animate value
		tweenValue(function(d) {
			item.value(d);
		}, 0, item.value(), 200)

		// extended props
		extendProperty(item, self.items().length, self)
		
		// update refLineY
		updateRefLineY(self, maxValue, scaleY)

		return this._super.add.call(this, item);
	}

	ko.bindingHandlers[componentName] = {
		init: function(el, viewModelAccr, allBindingAccr, viewModel, bindingCtx) {
			return { 'controlsDescendantBindings': true };
		},
		update: function(el, viewModelAccr, allBindingAccr, viewModel, bindingCtx) {
			var allBindings = allBindingAccr();
			
			var gridTemplateName = (!!allBindings.tmpl) ? allBindings.tmpl.id : "tmpl.default." + componentName;
			viewModel.templateName = gridTemplateName;
			viewModel.el = el;

			var templateEngine = new ko.nativeTemplateEngine();

			ko.renderTemplate(gridTemplateName, viewModel, { templateEngine: templateEngine }, el);
		}
	}

	ko[componentName] = {
		ViewModel: Column.ViewModel
	}

	return ko[componentName];
});