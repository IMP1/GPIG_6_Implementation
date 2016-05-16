// JS Backend handles API calls

var unitExamples = {
	"c2":
	{"batteryLevel":100.0,
	 "locLat":-1.08369,
	 "locLong":53.959,
	 "status":"FINE THANKS HOW ARE YOU?",
	 "timestamp":{"date":{"year":2016,"month":5,"day":12},"time":{"hour":17,"minute":31,"second":13,"nano":269000000}}}
	 
	 ,"Drone 1":
	{"batteryLevel":100.0,
	 "locLat":-1.09024,
	 "locLong":53.967,
	 "status":"FINE THANKS HOW ARE YOU?",
	 "timestamp":{"date":{"year":2016,"month":5,"day":12},"time":{"hour":17,"minute":31,"second":13,"nano":269000000}}}
	 
	 ,"Drone 2":
	{"batteryLevel":100.0,
	 "locLat":-1.080262,
	 "locLong":53.967,
	 "status":"FINE THANKS HOW ARE YOU?",
	 "timestamp":{"date":{"year":2016,"month":5,"day":12},"time":{"hour":17,"minute":31,"second":13,"nano":269000000}}}
	 
	 ,"Drone 3":
	{"batteryLevel":100.0,
	 "locLat":-1.086676,
	 "locLong":53.963,
	 "status":"FINE THANKS HOW ARE YOU?",
	 "timestamp":{"date":{"year":2016,"month":5,"day":12},"time":{"hour":17,"minute":31,"second":13,"nano":269000000}}}
	 
	 
}

var units = [];

function addNewUnit(id, symbol, coordinates, batteryLevel, status, lastUpdated){
	
	// Map symbol is generated based on ID
	console.log(id)
	if(id == "c2"){
		symbol = "castle";
	}else{
		symbol = "marker";
	}
	
    var unit = new Unit(id, symbol, coordinates, batteryLevel, status, lastUpdated);
    units.push(unit);
	return unit;    
}

function getUnitsInfo(){
	
	//TODO : Get JSON from API endpoints
	
	// Parse JSON
	
	var unitsJSON = unitExamples;
	
	Object.keys(unitsJSON).forEach(function (unitKey) {
	   
	   var unitJSON = unitsJSON[unitKey];
	   
	   // Check if Unit already exists
	   var unit;
	   units.forEach(function(existingUnit) {
		   if(existingUnit.id == unitKey){
			   unit = existingUnit;
		   }
	   }, this);
	   
	   if(unit){
		   // If Unit Exists Update It
		   updateUnitFromJSON(unit, unitKey, unitJSON);
	   }else{
		   // Else Create a new one
		   var coordinates = [unitJSON.locLat, unitJSON.locLong];
    	   unit = addNewUnit(unitKey, 'marker', coordinates, unitJSON.batteryLevel, unitJSON.status, unitJSON.timestamp);
		   var marker = addNewUnitMarker(unit);
		   unit.marker = marker;
		   console.log(unit);
	   }
	   
	   // TODO : Remove Units if they no longer exist
	   
	});
	
	map.getSource('markers').setData(markers);
	
}

function setupAPICalls(){
	setInterval(function(){getUnitsInfo()}, 1000);
	map.repaint = true;
}
