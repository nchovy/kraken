define(["/lib/knockout-2.1.0.debug.js", "/lib/jquery.js", "/component/data.js", "/lib/iscroll-amd.js"], function(ko, _$, Data, iScroll) {
	var className = "Kuro.Carousel";
	
	var iScrollInst;

	ko.bindingHandlers[className] = {
		init: function(el, viewModelAccr, allBindingAccr, viewModel, bindingCtx) {
			return { 'controlsDescendantBindings': true };
		},
		update: function(el, viewModelAccr, allBindingAccr, viewModel, bindingCtx) {
			
			if(iScrollInst === undefined) {
				//console.log("init");
				
				var viewModel = viewModelAccr(), allBindings = allBindingAccr();
				var gridTemplateName = (!!allBindings.tmpl) ? allBindings.tmpl.id : "tmpl.default." + className;
				var templateEngine = new ko.nativeTemplateEngine();
				
				ko.renderTemplate(gridTemplateName, viewModel, { templateEngine: templateEngine }, el);
				
				var scroller = $(el).find(".wrapper").children(":first-child");
				var len = viewModel.items().length;
				
				
				if(true) { // full size
					var w = $(el).width();
					scroller.width(w * len);
					scroller.find("li").css("width", 100/len + "%");
				}
				else { // defined size
					scroller.width(600 * len);
				}
				
				var wrapper = $(el).find(".wrapper")[0];
				
				//console.log(allBindings);
				
				iScrollInst = new iScroll(wrapper, {
					snap: true,
					checkDOMChanges: true,
					momentum: false,
					hScrollbar: false,
					onScrollEnd: allBindings.onScrollEnd
				});
				
				ko[className].iScroll = iScrollInst;
				
				
				$(window).on("resize", function() {
					var w = $(el).width();
					scroller.width(w * viewModel.items().length);
					
					var translateX = iScrollInst.currPageX * w;
					scroller.css("-webkit-transform", "translate(-" + translateX + "px, 0px) scale(1) translateZ(0px)");
				});
			}
			else {
				
				//console.log("update");
				
				var scroller = $(el).find(".wrapper").children(":first-child");
				var len = viewModel.items().length;
				
				if(true) { // full size
					var w = $(el).width();
					scroller.width(w * len);
					scroller.find("li").css("width", 100/len + "%");
				}
				else { // defined size
					scroller.width(600 * len);
				}
				
			}
			
		}
	}
	
	ko[className] = {
		name: className,
		viewModel: Data.ViewModel
	}

	return ko[className];
})