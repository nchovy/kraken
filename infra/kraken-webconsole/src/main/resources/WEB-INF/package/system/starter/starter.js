require(["/lib/jquery.js", "/lib/knockout-2.1.0.debug.js", "/core/program.js", "/component/carousel.js", "/bootstrap/js/bootstrap.amd.js"], 
	function(_$, ko, Program, Carousel, bootstrap) {

	Program.getPrograms(function(packs) {
		var starters = [];
		$.each(packs, function(i, pack) {
			if(!pack.starter) {
				starters.push(pack);
			}
		});


		var vm = new Carousel.viewModel(starters);
		
		vm.onScrollEnd = function() {
			
			document.querySelector('#indicator > li.active').className = '';
			document.querySelector('#indicator > li:nth-child(' + (this.currPageX+1) + ')').className = 'active';
			
		}
		
		vm.scrollToPage = function(idx) {
			
			Carousel.iScroll.scrollToPage(idx, 0);
			
		}
		
		ko.applyBindings(vm, document.getElementById("treeview"));
		
		$("#indicator > li:first-child").addClass("active");
		
		$('.dropdown-toggle').dropdown()

		$("#add").on("click", function() {
			
			vm.add({ name: "asdf" });
			
			
			var len = vm.items().length;
			
			setTimeout(function() {
				vm.scrollToPage(len - 1);
			}, 500);
			
		});

	});

	
});