define(["/lib/jquery.js", "/core/locale.js", "/core/connection.js", "/component/list.js"], function(_$, Locale, socket, List) {
	var programManager = (function() {

		var programs;
		var packs;

		this.getPrograms = function(callback) {
			if(!programs || !packs) {
				console.log("getAvailablePrograms")
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

					console.log(packs)

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
				program.onActive();
			}
			else if(current.length == 0) {
				this.launch(program);
			}

			$("title").text("^_^ " + program.name);
		}

		this.launch = function(program) {
			var localedUrl = "package/" + program.pack + "/" + program.path + "/index." + Locale.getCurrentLocale() + ".html";
			var defUrl = "package/" + program.pack + "/" + program.path + "/index.html";
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