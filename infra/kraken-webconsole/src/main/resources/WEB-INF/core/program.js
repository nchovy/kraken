define(["/lib/jquery.js", "/core/locale.js", "/core/connection.js", "/component/list.js", "/lib/knockout-2.1.0.debug.js"], function(_$, Locale, socket, List, ko) {
	var programManager = (function() {

		var programs;
		var packs;

		this.getPrograms = function(callback) {
			if(!programs || !packs) {
				socket.send("org.krakenapps.dom.msgbus.ProgramPlugin.getAvailablePrograms", {}, function(m) {
					programs = m.body.programs;
					packs = m.body.packs;

					var starter = {
						created: "2013-01-02 17:31:03+0900",
						description: null,
						name: "Starter",
						pack: "System",
						path: "starter",
						seq: 4,
						updated: "2013-01-02 17:31:03+0900",
						visible: true
					};

					packs[0].programs.push(starter);
					programs.push(starter);

					$.each(packs, function(i, pack) {
						$.each(pack.programs, function(j, program) {
							program.icon = ko.observable("/images/app.png");
							var appicon = "package/" + pack.dll + "/" + program.path + "/icon.png";
							$.get(appicon, function() {
								program.icon(appicon);
							});
						});
					});

					callback(packs, programs);
				});
			}
			else {
				console.log("getAvailablePrograms cache");
				callback(packs, programs);
			}
		}

		this.getProgramById = function(id) {
			var program = null;
			$.each(programs, function(i, obj) {
				if(obj.path === id) {
					program = obj;
					return false;
				}
			});

			return program;
		}

		this.go = function(program) {
			var iframes = $(".mainframe iframe");
			iframes.hide();

			var current = $(".mainframe iframe[data-program=" + program.path + "]")

			if(current.length == 1) {
				current.show();
			}
			else if(current.length == 0) {
				this.launch(program);
			}

			$("title").text("^_^ " + program.name);
		}

		function findPackDllbyName(name) {
			var dll;
			$.each(packs, function(i, obj) {
				if(obj.name === name) {
					dll = obj.dll;
					return false;
				}
			});

			return dll;
		}

		this.launch = function(program) {
			var packdll = findPackDllbyName(program.pack);
			var localedUrl = "package/" + packdll + "/" + program.path + "/index." + Locale.getCurrentLocale() + ".html";
			var defUrl = "package/" + packdll + "/" + program.path + "/index.html";
			if(Locale.getCurrentLocale() == "en") {
				localedUrl = defUrl;
			}

			var iframe = $("<iframe>").attr("data-program", program.path)
					.addClass("v-stretch")
					.addClass("h-stretch")
					.appendTo(".mainframe");

			var request = $.ajax({
				url: localedUrl,
				//type: 'POST',
				//data: {},
				success: function() {
					iframe.attr("src", localedUrl);
				},
				error: function() {
					iframe.attr("src", defUrl);
				},
				complete: function() {
					var loadingdiv = $("<div>").css("position", "absolute")
							.css("background", "url('../images/random_grey_variations.png')")
							.css("top", "0px")
							.css("left", "0px")
							.addClass("v-stretch")
							.addClass("h-stretch")
							.appendTo(".mainframe")

					$(iframe.get(0).contentDocument).ready(function($) {
						setTimeout(function() {
							loadingdiv.fadeOut(function() {
								$(this).remove();
							});
						}, 300)
					});
				}
			})
		}

		this.exit = function(program) {
			var iframe = $(".mainframe iframe[data-program=" + program.path + "]");
			iframe.remove();
		}

		return {
			go: go,
			launch: launch,
			exit: exit,
			getPrograms: getPrograms,
			getProgramById: getProgramById,
		}
	})();

	
	var Core = parent.Core;
	if(!Core) {
		Core = parent.Core = {};
	}

	if(!Core.Program) {
		console.log("register Program manager globally");
		parent.Core.Program = programManager;
	}

	return Core.Program;
});