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
	
	// Get JSON from API endpoints
	
	var xmlHttpUnits = new XMLHttpRequest();
    	xmlHttpUnits.open( "GET", "http://localhost:8081/GetDroneInfo", false ); // false for synchronous request
    	xmlHttpUnits.send( null );
    
	// Parse JSON
	var unitsJSON = JSON.parse(xmlHttpUnits.responseText);
	
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
	   }
	   
	   // TODO : Remove Units if they no longer exist
	   
	});
	
	// Redraw Map
	map.getSource('markers').setData(markers);
	
}

function setupAPICalls(){
	setInterval(getUnitsInfo(), 1000);
}

function pollFunc(){
	//Drone info
	var xmlHttpDrone = new XMLHttpRequest();
    xmlHttpDrone.open( "GET", "http://localhost:8081/GetDroneInfo", false ); // false for synchronous request
    xmlHttpDrone.send( null );
    var drones = JSON.parse(xmlHttpDrone.responseText);
    console.log(drones);
    // for(var key in drones){
    // 	console.log(key);
    // }
    // console.log(drones);
    //Scan info
    known_scans = ["12016-05-12T17:31:13.269","2","3"];
    var known_scans_string = known_scans.join(",")
	var xmlHttpScan = new XMLHttpRequest();
    xmlHttpScan.open( "GET", "http://localhost:8081/GetScanInfo?known_scans="+known_scans_string, false ); // false for synchronous request
    xmlHttpScan.send( null );
    var scans = JSON.parse(xmlHttpScan.responseText);
    // console.log(scans);
}