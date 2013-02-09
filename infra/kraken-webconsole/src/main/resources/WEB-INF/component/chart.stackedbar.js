define(["/lib/jquery.js", "/lib/d3.v2.amd.js", "/component/kuro.js"], function (_$, d3, $K) {
	var StackedBar = $K.namespace("Chart.StackedBar");

	StackedBar.ViewModel = function (el, z, options) {
		//$(el).html("hello stacked");
		//console.log(z)

		var _data;

		var margin = { top: 10, right: 0, left: 50, bottom: 10 },
			width = 100 - margin.left - margin.right,
			height = $(el).height() - margin.top - margin.bottom;

		var x = d3.scale.ordinal()
			.rangeRoundBands([0, width], .1);

		var y = d3.scale.linear()
			.rangeRound([height, 0]);

		var color = d3.scale.ordinal()
			.range(["#7EB5D6", "#2A75A9", "#274257", "#DFC184", "#8F6048", "#644436", "#88aaaa", "#0A224E", "#6699aa"]);


		var svg = d3.select(el).append("svg")
			.attr("width", width + margin.left + margin.right)
			.append("g")
				.attr("transform", "translate(" + margin.left + ",0)");


		var tooltip;

		function onMouseOver(d) {
			this.isMouseOver = true;
			tooltip.find(".tooltip-inner").text(d.value);
			tooltip.addClass("in");

			d3.select(this).transition()
				.attr("x", -20)
				.attr("width", x.rangeBand() + 20)
				.attr("fill-opacity", "1");
		}

		function onMouseMove(i, e) {
			if(!this.isMouseOver) return;
			
			var coord = d3.svg.mouse(this);
			tooltip.css("left", "70px").css("top", coord[1] + "px");
		}

		function onMouseOut(i, e) {
			if(!this.isMouseOver) return;
			
			var coord = d3.svg.mouse(this);
			tooltip.css("left", "70px").css("top", coord[1] + "px");
			
			d3.select(this).transition()
				.attr("x", 0)
				.attr("width", x.rangeBand())
				.attr("fill-opacity", ".5");

			tooltip.removeClass("in");
		}

		function init(data) {
			_data = data;

			color.domain(d3.keys(data[0]));

			data.forEach(function (d) {
				var y0 = 0;
				d.ages = color.domain().map(function (name) { return { name: name, y0: y0, y1: y0 += +d[name], value: d[name] }; });
				d.ages.forEach(function (d) { d.y0 /= y0; d.y1 /= y0; });
			});

			data.sort(function (a, b) { return b.ages[0].y1 - a.ages[0].y1; });

			x.domain(data.map(function (d) { return d.State; }));

			var state = svg.selectAll(".state")
				.data(data)
				.enter().append("g")
					.attr("class", "state")
					.attr("transform", function (d) { return "translate(" + x(d.State) + ", " + margin.top + ")"; });

			tooltip = $('<div class="tooltip fade left"><div class="tooltip-arrow"></div><div class="tooltip-inner"></div></div>').appendTo(el);

			var rect = state.selectAll("rect")
				.data(function (d) { return d.ages; })
				.enter().append("rect")
					.attr("width", x.rangeBand())
					.attr("y", function (d) { return y(d.y1); })
					.attr("height", function (d) { return y(d.y0) - y(d.y1); })
					.attr("fill-opacity", ".5")
					.style("fill", function (d) { return color(d.name); })
					.on("mouseover", onMouseOver)
					.on("mousemove", onMouseMove)
					.on("mouseout", onMouseOut)
					.on("click", function(d) {

						//$("#timeline-popup").slideDown().css("top", (y(d.y1) + 90) + "px");

					})

			var legend = svg.select(".state:last-child").selectAll(".legend")
				.data(function (d) {
					return d.ages;
				})
				.enter().append("g")
					.attr("class", "legend")
					.attr("transform", function (d) { return "translate(-50," + y(d.y0) + ")"; });

			legend.append("line")
				.attr("x2", 10);

			legend.append("text")
				.attr("x", 13)
				.attr("dy", ".35em")
				.text(function (d) { return d.name; });
		}

		init(z);

		function resize() {

			y = d3.scale.linear()
				.rangeRound([height, 0]);

			var state = svg.selectAll(".state")
				.data(_data)

			var rect = state.selectAll("rect")
				.data(function (d) { return d.ages; })
				.attr("y", function (d) { return y(d.y1); })
				.attr("height", function (d) { return y(d.y0) - y(d.y1); });

			var legend = svg.select(".state:last-child").selectAll(".legend")
			  .data(function (d) { return d.ages; })
			  .attr("transform", function (d) { return "translate(-50," + y(d.y0) + ")"; })
		}


		this.updateData = function(data) {
			_data = data;

			y = d3.scale.linear()
				.rangeRound([height, 0]);

			color.domain(d3.keys(data[0]));

			data.forEach(function (d) {
				if(!d.ages) {
					var y0 = 0;
					d.ages = color.domain().map(function (name) { return { name: name, y0: y0, y1: y0 += +d[name], value: d[name] }; });
					d.ages.forEach(function (d) { d.y0 /= y0; d.y1 /= y0; });
				}
			});

			data.sort(function (a, b) { return b.ages[0].y1 - a.ages[0].y1; });

			var state = svg.selectAll(".state")
				.data(data)

			var rect = state.selectAll("rect")
				.data(function (d) {
					return d.ages;
				})
				.attr("data-count", function(d) {
					if(y(d.y0) - y(d.y1) < 1) {
						d.tooSmall = true;
					}
					return d.value;
				})
				.on("mouseover", null)
				.on("mousemove", null)
				.on("mouseout", null)
				.transition()
					.attr("y", function (d) { return y(d.y1); })
					.attr("height", function (d) {
						return y(d.y0) - y(d.y1); 
					})
					.each("end", function (d) {
						
						d3.select(this)
							.on("mouseover", onMouseOver)
							.on("mousemove", onMouseMove)
							.on("mouseout", onMouseOut)
					});
			
			var legend = svg.select(".state:last-child").selectAll(".legend")
			  .data(function (d) { return d.ages; })
			  .transition()
			  	.attr("transform", function (d) { return "translate(-50," + y(d.y0) + ")"; })

			var text = svg.select(".state:last-child").selectAll("text")
				.data(function (d) { return d.ages; })
				.text(function (d, i) { 
					if(!!d.tooSmall) {
						if(i == 10) return d.name;
						else return "";
					}
					else {
						return d.name;
					}
				});
		}


		$(window).on("resize", function () {
			height = $(el).height() - margin.top - margin.bottom;
			resize();
		})


	}

	return StackedBar;
});