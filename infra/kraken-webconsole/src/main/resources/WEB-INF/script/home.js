require([
	"/lib/jquery.js",
	"/lib/knockout-2.1.0.debug.js",
	"/core/connection.js",
	"/core/program.js",
	"/core/locale.js",
	"/core/page.js",
	"/component/list.js",
	"/component/util.js"
], 

function(_$, ko, socket, programManager, Locale, pageManager, List, Util) {
	console.log(Locale);

	(function() {
		var listNowRunning = [];

		var container = $(".navbar-fixed-bottom .container");
		var vm = new List.ViewModel(listNowRunning);

		vm.select = function(program) {
			var nowRunning = false;
			//console.log(program)

			$.each(container.find("li"), function(i, obj) {
				var ctx = ko.contextFor(obj).$data;
				console.log(ctx)
				if(ctx.id === program.id && ctx.path === program.path) {
					$(obj).find("a").click();

					nowRunning = true;
					return false;
				}
			});

			if(!nowRunning) {
				this.run(program);
			}
		}

		vm.onSelect = function(program, el) {
			Core.Program.go(program);

			container.find("li.active").removeClass("active");
			$(el).parent().addClass("active");
		}

		vm.onActive = function(program) {
			$("#div-launcher").hide();
			$("#start").removeClass("active");
		}

		vm.run = function(program) {
			var items = vm.items();
			function deactiveAll() {
				$.each(items, function(i, obj) {
					obj.isActive(false);
				});
			}

			program.onSelect = function(self, e) { 
				return vm.onSelect(self, e.delegateTarget);
			}

			program.onActive = function() {
				deactiveAll();
				this.isActive(true);
				return vm.onActive(this);
			}

			program.closeProgram = function(self) {
				var curridx = items.indexOf(program) - 1;
				if(curridx === -1) {
					console.log('close');
					return;
					//window.close();
				}

				Core.Program.exit(self);
				vm.remove(program);
				vm.select(items[curridx]);
			}

			deactiveAll();
			program.isActive = ko.observable(true);

			vm.add(program);

			container.find("li:last-child a").click();
		}

		window.vm = vm;
		// taskbar 관련하여, 이 파일에서 view 컨트롤을 담당하기 때문에, launcher에서 이 viewModel에 대한 참조가 필요함

		ko.applyBindings(vm, container.get(0));
	})();


	(function() {
		console.log("----launcher----");
		Core.Program.getPrograms(function(packs, programs) {
			
			$.each(packs, function(i, pack) {


				var vm = new List.ViewModel(pack.programs);

				vm.pack = pack.name;
				vm.onSelect = function(program, el) {
					$("#div-launcher").hide();
					$("#start").removeClass("active");
					parent.vm.select(program);
				}

				if(pack.name === "System") {
					ko.applyBindings(vm, document.getElementById("pack-system"));
				}
				else {
					var page = $('<div data-bind="Kuro.List: self, tmpl: launcher"></div>')
									.addClass("page")
									.appendTo("#pack-all");

					ko.applyBindings(vm, page.get(0));
				}
			});

			afterworks();
		});


		$("#start").on("click", function() {
			if($("#div-launcher").is(":hidden")) {
				$("#start").addClass("active");
				$("#div-launcher").fadeIn('fast');
			}
			else {
				$("#start").removeClass("active");
				$("#div-launcher").hide();
			}
		})

	})();


	function afterworks() {
		var entry = pageManager.urlParam("program");
		if(entry == null) {
			entry = "starter";
		}

		var program = Core.Program.getProgramById(entry);
		console.log(program)
		if(!!program) {
			vm.select(program);
		}

		$("#div-launcher").hide();
		$("#start").removeClass("active");
	}
});