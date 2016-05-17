var MessageSeverity = Object.freeze({"high":1, "medium":2, "normal":3, "success":4})

MessageID = 0;

var Message = function(header, body, style){
	this.id = MessageID++;
	this.header = header;
	this.body = body;
	this.style = style;	
}

function ShowNewMessage(header, body, style){
	var msg = new Message(header, body, style);
	showMessage(msg);
}

function showMessage(message){
	
	var messagesElement = document.getElementById('messages');
	
	var message_element = document.createElement('div');
        message_element.id = 'message-'+message.id;
        message_element.className = 'message '+message.style;
        messagesElement.insertBefore(message_element, messagesElement.childNodes[0]);
		
		var message_element_header = document.createElement('h3');
	    	message_element_header.textContent = message.header;
	        message_element.appendChild(message_element_header);
			
		var message_element_body = document.createElement('p');
	    	message_element_body.textContent = message.body;
	        message_element.appendChild(message_element_body);
	
}