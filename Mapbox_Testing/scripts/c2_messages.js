var MessageSeverity = Object.freeze({"high":1, "medium":2, "normal":3, "success":4})

MessageID = 0;
MaxMessages = 20;

var Message = function(header, body, style, clickFunction){
	this.id       = MessageID++;
	this.header   = header;
	this.body     = body;
	this.style    = style;
	this.clickFunction = clickFunction;	
}

function ShowNewMessage(header, body, style, clickFunction){
	var msg = new Message(header, body, style, clickFunction);
	showMessage(msg);
}

function showMessage(message){
	
	var messagesElement = document.getElementById('messages');
	
	var message_element = document.createElement('div');
        message_element.id = 'message-'+message.id;
        message_element.className = 'message '+message.style+' fadeAndScaleIn';
        messagesElement.insertBefore(message_element, messagesElement.childNodes[0]);
		
		var message_element_header = document.createElement('h3');
	    	message_element_header.textContent = message.header;
	        message_element.appendChild(message_element_header);
			
		var message_element_body = document.createElement('p');
	    	message_element_body.textContent = message.body;
	        message_element.appendChild(message_element_body);

	if (typeof message.clickFunction === "function") {      
		message_element.classList.add("clickable"); 
	    message_element.addEventListener('click', function(e) {            
	        message.clickFunction();
	    });
	}
			
	clearOldMessages();
	
}

function clearOldMessages(){
	
	var messagesElement = document.getElementById('messages');
	
	if(messagesElement.childNodes.length > MaxMessages){
		var messageToRemove = messagesElement.childNodes[MaxMessages];
		messageToRemove.remove();
	}
	
}