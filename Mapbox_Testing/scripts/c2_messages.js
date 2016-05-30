var MessageSeverity = Object.freeze({"high":1, "medium":2, "normal":3, "success":4})

MessageID = 0;
MaxMessages = 20;

var messages = [];
var messagesJSON;

var Message = function(header, body, style, clickFunction){
	this.id            = MessageID++;
	this.header        = header;
	this.body          = body;
	this.style         = style;
	this.clickFunction = clickFunction;	
	this.timestamp     = Date.now();
}

function ShowNewMessage(header, body, style, clickFunction){
	var msg = new Message(header, body, style, clickFunction);
	messages.push(msg);
	showMessage(msg);

	messagesJSON = "text/json;charset=utf-8," + encodeURIComponent(JSON.stringify(messages));

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

        var options = {
		    weekday: "long", year: "numeric", month: "short",
		    day: "numeric", hour: "2-digit", minute: "2-digit"
		};
			
		var message_element_body = document.createElement('p');
	    	message_element_body.textContent = message.body;
	        message_element.appendChild(message_element_body);

	    var message_element_date = document.createElement('p');
        	message_element_date.className = 'date';
	    	message_element_date.textContent = moment(message.timestamp).format('MMMM Do YYYY, h:mm:ss a');
	        message_element.appendChild(message_element_date);

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

function downloadMessageLog(){

	var date = moment(Date.now()).format('MMMMDoYYYYhmmssa');

	var btn_download_messages = document.getElementById('btn-download-messages');
		btn_download_messages.href = 'data:' + messagesJSON;
		btn_download_messages.download = 'SonarScout_Msg_Log_'+date+'.json';
}