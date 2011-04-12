Run = function() {
	this.name = "Run";
	
	this.onstart = function(pid, args) {
		var window = windowManager.createWindow(pid, 'Run', 200, 100); 
		window.append('you better run run run run run');
		$('<input type="text" />').appendTo(window);
		$('<button>Run</button>').appendTo(window);

		$('button', window).click(function() { 
			var command = $(this).prev().attr('value');
		
			channel.send(1, command, {}, function(resp) { alert('result: ' + resp['result']) });
		});
	}
	
	this.onstop = function() {
	}
}

processManager.launch(new Run()); 