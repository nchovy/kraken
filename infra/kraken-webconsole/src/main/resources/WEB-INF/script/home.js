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

var vmTasks;

	(function() {
		var container = $(".navbar-fixed-bottom .container");
		vmTasks = new List.ViewModel([]);

		vmTasks.onSelect = function(program) {
			$("#div-launcher").hide();
			$("#start").removeClass("active");

			$(".mainframe").removeClass("blurry");
			
			Core.Program.go(program);
		}

		vmTasks.onBeforeRemove = function(program) {
			if(vmTasks.length() == 1) return false;
		}

		vmTasks.onAfterRemove = function(program) {
			Core.Program.exit(program);
			vmTasks.selectAt(vmTasks.length() - 1);
		}

		ko.applyBindings(vmTasks, container.get(0));
	})();


	(function() {
		Core.Program.getPrograms(function(packs, programs) {
			
			$.each(packs, function(i, pack) {


				var vmPrograms = new List.ViewModel(pack.programs);

				vmPrograms.pack = pack.name;
				vmPrograms.run = function(program) {
					$("#div-launcher").hide();
					$("#start").removeClass("active");

					$(".mainframe").removeClass("blurry");

					var found = false;
					var foundprogram;
					$.each(vmTasks.items(), function(i, obj){
						if(obj.path === program.path) {
							found = true;
							foundprogram = obj;
							return false;
						}
					})
					
					if(!found) {
						vmTasks.add(program);
						vmTasks.select(program);
					}
					else {
						vmTasks.select(foundprogram);
					}
				}

				if(pack.name === "System") {
					ko.applyBindings(vmPrograms, document.getElementById("pack-system"));
				}
				else {
					var page = $('<div data-bind="Kuro.List: self, tmpl: launcher" class="box-pack"></div>').appendTo("#pack-all");
					ko.applyBindings(vmPrograms, page.get(0));
				}
			});

			afterworks();
		});


		$("#start").on("click", function() {
			if($("#div-launcher").is(":hidden")) {
				$("#start").addClass("active");
				$("#div-launcher").fadeIn('fast', function() {
					$(".mainframe").addClass("blurry");
				});
			}
			else {
				$("#start").removeClass("active");
				$("#div-launcher").hide();

				$(".mainframe").removeClass("blurry");
			}
		});

	})();


	function afterworks() {
		var entry = pageManager.urlParam("program");
		if(entry == null) {
			entry = "starter";
		}

		var program = Core.Program.getProgramById(entry);
		if(!!program) {
			vmTasks.add(program);
			vmTasks.select(program);
		}

		$("#div-launcher").hide();
		$("#start").removeClass("active");
	}
});