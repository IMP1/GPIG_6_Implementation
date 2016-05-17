// Debug Vars
var ONLINE = false;

// Utility Functions

function unprojectedDistance(ll0, ll1) {
    
    // Haversine Formulae
    
    var lat1 = ll0.lat;
    var lon1 = ll0.lng;
    var lat2 = ll1.lat;
    var lon2 = ll1.lng;
    
	var radlat1 = Math.PI * lat1/180
	var radlat2 = Math.PI * lat2/180
	var theta = lon1-lon2
	var radtheta = Math.PI * theta/180
	var dist = Math.sin(radlat1) * Math.sin(radlat2) + Math.cos(radlat1) * Math.cos(radlat2) * Math.cos(radtheta);
	dist = Math.acos(dist)
	dist = dist * 180/Math.PI
	dist = dist * 60 * 1.1515
	dist = dist * 1.609344 * 1000 // Convert to Meters
    
	return dist;
}

function arraysEqual(a, b) {
  if (a === b) return true;
  if (a == null || b == null) return false;
  if (a.length != b.length) return false;

  for (var i = 0; i < a.length; ++i) {
    if (a[i] !== b[i]) return false;
  }
  return true;
}

/////////////////////////
// FRONTEND COMMS CODE //
/////////////////////////

function setupAPICalls(){
	getUnitsInfo();
	setInterval(getUnitsInfo, 2000);
}

//////////////////////
// SEARCH UNIT CODE //
//////////////////////

var unitExamples = {
	"c2":
	{"batteryLevel":1,
	 "locLat":53.959,
	 "locLong":-1.08369,
	 "status":"Moving",
	 "timestamp":{"date":{"year":2016,"month":5,"day":17},"time":{"hour":15,"minute":18,"second":13,"nano":269000000}}}
	 
	 ,"Drone 1":
	{"batteryLevel":1,
	 "locLat":53.967,
	 "locLong":-1.09024,
	 "status":"Navigating",
	 "timestamp":{"date":{"year":2016,"month":5,"day":12},"time":{"hour":17,"minute":31,"second":13,"nano":269000000}}}
	 
	 ,"Drone 2":
	{"batteryLevel":1,
	 "locLat":53.967,
	 "locLong":-1.080262,
	 "status":"Stationary",
	 "timestamp":{"date":{"year":2016,"month":5,"day":12},"time":{"hour":17,"minute":31,"second":13,"nano":269000000}}}
	 
	 ,"Drone 3":
	{"batteryLevel":1,
	 "locLat":53.963,
	 "locLong":-1.086676,
	 "status":"Scanning",
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
	
	var unitsJSON;
	
	if(ONLINE){
	
		var xmlHttpUnits = new XMLHttpRequest();
	    	xmlHttpUnits.open( "GET", "http://localhost:8081/GetDroneInfo", false ); // false for synchronous request
	    	xmlHttpUnits.send( null );
	    
		// Parse JSON
		unitsJSON = JSON.parse(xmlHttpUnits.responseText);
	
	}else{
		unitsJSON = unitExamples; 
	}
	
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
		   var coordinates = [unitJSON.locLong, unitJSON.locLat];
    	   unit = addNewUnit(unitKey, 'marker', coordinates, unitJSON.batteryLevel, unitJSON.status, unitJSON.timestamp);
		   var marker = addNewUnitMarker(unit);
		   unit.marker = marker;
	   }
	   
	   // TODO : Remove Units if they no longer exist
	   
	});
	
	// Redraw Map
	map.getSource('markers').setData(markers);
	
	// Redraw UI
	updateUnitUI();
	
}

// Recall Units

function recallUnits(){
	
		var xmlHttpAssignSearchAreas = new XMLHttpRequest();
		var urlString = "http://localhost:8081/RecallUnits";
	    xmlHttpAssignSearchAreas.open( "GET", urlString, false );
	    xmlHttpAssignSearchAreas.send( null );
	
}

//////////////////////
// SEARCH AREA CODE //
//////////////////////

var currentSearchArea;
var searchAreaArray = [];

// Search Area Asssignment

function assignSearchAreas(){
	
	searchAreaArray.forEach(function(searchArea) {
				
		var xmlHttpAssignSearchAreas = new XMLHttpRequest();
		var urlString = "http://localhost:8081/AssignSearchAreas?latitude="+searchArea.center.lat+"&longitude="+searchArea.center.lng+"&numberRequested="+searchArea.assignedDrones+"&radius="+searchArea.radius;
	    xmlHttpAssignSearchAreas.open( "GET", urlString, false ); // false for synchronous request
	    xmlHttpAssignSearchAreas.send( null );
		
	}, this);	
	
}