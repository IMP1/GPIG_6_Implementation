window.setInterval(pollFunc, 3000);

function pollFunc(){
	var xmlHttp = new XMLHttpRequest();
    xmlHttp.open( "GET", "http://localhost:8081/drones", false ); // false for synchronous request
    xmlHttp.send( null );
    var drones = JSON.parse(xmlHttp.responseText);
    for(var key in drones){
    	addNewUnit(key, drones[key])
    	console.log(key);
    }
}