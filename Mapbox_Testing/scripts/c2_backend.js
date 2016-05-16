// JS Backend handles API calls

var unitExamples = {
	"C2":
	{"batteryLevel":100.0,
	 "locLat":0.001256475,
	 "locLong":0.87252587,
	 "status":"FINE THANKS HOW ARE YOU?",
	 "timestamp":{"date":{"year":2016,"month":5,"day":12},"time":{"hour":17,"minute":31,"second":13,"nano":269000000}}}
	 
	 ,"Drone 1":
	{"batteryLevel":100.0,
	 "locLat":0.001256475,
	 "locLong":0.87252587,
	 "status":"FINE THANKS HOW ARE YOU?",
	 "timestamp":{"date":{"year":2016,"month":5,"day":12},"time":{"hour":17,"minute":31,"second":13,"nano":269000000}}}
	 
	 ,"Drone 2":
	{"batteryLevel":100.0,
	 "locLat":0.001256475,
	 "locLong":0.87252587,
	 "status":"FINE THANKS HOW ARE YOU?",
	 "timestamp":{"date":{"year":2016,"month":5,"day":12},"time":{"hour":17,"minute":31,"second":13,"nano":269000000}}}
	 
	 ,"Drone 3":
	{"batteryLevel":100.0,
	 "locLat":0.001256475,
	 "locLong":0.87252587,
	 "status":"FINE THANKS HOW ARE YOU?",
	 "timestamp":{"date":{"year":2016,"month":5,"day":12},"time":{"hour":17,"minute":31,"second":13,"nano":269000000}}}
	 
	 
}


function getUnitsInfo(){
	
	//TODO : Get JSON from API endpoints
	
	// Parse JSON
	
	var unitsJSON = unitExamples;
	
	Object.keys(unitsJSON).forEach(function (unitKey) {
	   
	   var unitJSON = unitsJSON[unitKey];
	   console.log(unitJSON);
	   
	   
	   
	});
	
	console.log(markers);
	
	
	
}

function setupAPICalls(){
	setInterval(getUnitsInfo, 1000);
}