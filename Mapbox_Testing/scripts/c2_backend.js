// Debug Vars
var ONLINE = true;

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

var removeByAttr = function(arr, attr, value){
    var i = arr.length;
    while(i--){
       if( arr[i] 
           && arr[i].hasOwnProperty(attr) 
           && (arguments.length > 2 && arr[i][attr] === value ) ){ 
           arr.splice(i,1);
       }
    }
    return arr;
}

var getByAttr = function(arr, attr, value){
    var i = arr.length;
    while(i--){
       if( arr[i] 
           && arr[i].hasOwnProperty(attr) 
           && (arguments.length > 2 && arr[i][attr] === value ) ){ 
           return arr[i];
       }
    }
	return false;
}

/////////////////////////
// FRONTEND COMMS CODE //
/////////////////////////

function setupAPICalls(){
	var refreshInterval = 1000;
	getUnitsInfo();
	getScanInfo();
	setInterval(getUnitsInfo, refreshInterval);
	setInterval(getScanInfo, refreshInterval);
	
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
	 "timestamp":{"date":{"year":2016,"month":5,"day":17},"time":{"hour":16,"minute":30,"second":13,"nano":269000000}}}
	 
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

function changeDroneAssignmentForSearchArea(inc, searchArea){    
	
	if(searchArea.requestedDrones == 1 && inc == -1){
		ShowNewMessage('Drone Assignment Error', 'Cannot assign less than one drone', 'medium');
		return;
	}else if(searchArea.requestedDrones == units.length && inc == 1){
		ShowNewMessage('Drone Assignment Error', 'Cannot assign more Drones than exist', 'medium');
		return;
	}
		
	searchArea.requestedDrones += inc;
}

// Search Area Asssignment

var currentlyAssigningSearchAreas;

function assignSearchAreas(){
	
	if(units.length == 0){
		ShowNewMessage('Search Area Assignment Error', 'No Search Units in network to assign search areas.', 'high');
		return;
	}
	
	if(searchAreaArray.length == 0){
		ShowNewMessage('Search Area Assignment Error', 'No Search Areas created, cannot assign to units.', 'high');
		return;
	}	
	
	if(currentlyAssigningSearchAreas){
		ShowNewMessage('Search Area Assignment Error', 'Already in the process of assigning search areas.', 'high');
		return;
	}
	
	currentlyAssigningSearchAreas = true;
	
	searchAreaArray.forEach(function(searchArea) {	
		
		if(ONLINE){	
			
			if(searchArea.assignedDrones.length == 0){	
				
				var xmlHttpAssignSearchAreas = new XMLHttpRequest();
				var urlString = "http://localhost:8081/AssignSearchAreas?latitude="+searchArea.center.lat
								+"&longitude="+searchArea.center.lng
								+"&numberRequested="+searchArea.requestedDrones
								+"&radius="+searchArea.radius;
				xmlHttpAssignSearchAreas.open( "GET", urlString, false ); // false for synchronous request
				xmlHttpAssignSearchAreas.send( null );
				
				ShowNewMessage('Succesfully Sent Search Area '+searchArea.id+' Assignment Request', '', 'success');
				
				// Responds with the drone uids assigned to this search area 
				var searchAreaResponse = JSON.parse(xmlHttpAssignSearchAreas.responseText);
				
				if (searchAreaResponse.length > 0){
					// Succesfully assigned > 1 drone
					// Get Drones by UID
					
					searchAreaResponse.forEach(function(droneID) {
						
						var unit = getByAttr(units, 'id', droneID);
						if(!unit){
							ShowNewMessage('Drone Assignment Error', 'Assigned Drone ID ('+droneID+') does not exist.', 'high');
						}else{
							searchArea.assignedDrones.push(unit);
						}					
						
					}, this);
					
				}
				
			}
		
		}else{
			
			ShowNewMessage('Succesfully Sent Search Area '+searchArea.id+' Assignment Request (OFFLINE)', '', 'success');
			
			var searchUnit = units[1];
			searchArea.assignedDrones = [searchUnit];
			
		}
		
		
	}, this);	
	
	currentlyAssigningSearchAreas = false;
	
}

// Delete all Search Areas

function deleteAllSearchAreas(){
    
    console.log('//TODO : Prompt Are you sure message.');
    
    searchAreaArray.forEach(function(searchArea){
        deleteSearchAreaView(searchArea);
    });
    searchAreaArray = [];
}

///////////////
// SCAN CODE //
///////////////

var scanAreas = [];

//{"e9b7d680-8704-4c37-88e6-3243a9c4ae0c2016-05-20T11:22:36.506":{"locLat":53.957184,"locLong":-1.078302,"depth":1.0,"flowRate":5.0,"distanceReadings":

var scanExamples = {
	"scan1":
	{"locLat":53.959,
	 "locLong":-1.08369,
	 "depth":10,
	 "flowRate":10,
	 // Latlong are reversed
	 "distanceReadings":[ [ -1.080062204934591, 53.955374658400174 ], 
	 			[ -1.079072578584086, 53.955751100286385 ], 
				[ -1.079302491776628, 53.956027547632175 ], 
				[ -1.079492420066118, 53.956198120612022 ], 
				[ -1.080082197386116, 53.95626870233059 ], 
				[ -1.08047205019086, 53.95626870233059 ], 
				[ -1.080641986028826, 53.956151066066589 ], 
				[ -1.080791929415266, 53.956004020269859 ], 
				[ -1.080232140772556, 53.955645226349397 ], 
				[ -1.080252133224082, 53.955504060681911 ], 
				[ -1.080072201160353, 53.955374658400174 ]],
	 "timestamp":{"date":{"year":2016,"month":5,"day":17},"time":{"hour":16,"minute":30,"second":13,"nano":269000000}}}
	 
}

function getScanInfo(){
	
	var scanAreasJSON;
	
	if(ONLINE){
		
		var knownScans = '';
		scanAreas.forEach(function(scanArea) {
			knownScans += scanArea.id+',';
		}, this);
		
		var xmlHttpScans = new XMLHttpRequest();
	    	xmlHttpScans.open( "GET", "http://localhost:8081/GetScanInfo?known_scans="+knownScans, false ); // false for synchronous request
	    	xmlHttpScans.send( null );
	    
		// Parse JSON
		scanAreasJSON = JSON.parse(xmlHttpScans.responseText);
	}else{
		scanAreasJSON = scanExamples;
	}
	
	Object.keys(scanAreasJSON).forEach(function (scanKey) {
		   
		   var scanJSON = scanAreasJSON[scanKey];
		   
		   var scan;
		   scanAreas.forEach(function(existingScan) {
			   if(existingScan.id == scanKey){
				   scan = existingScan;
			   }
		   }, this);
		   
		   console.log(scanJSON);
		   
		   if(scan){
			   
		   }else{
			   // Else Create a new one
			   
			   var scanArea = new ScanArea();
			   	   scanArea.id = scanKey;
				   scanArea.center = [scanJSON.locLat, scanJSON.locLong];
				   scanArea.depth = scanJSON.depth;
				   scanArea.flowrate = scanJSON.flowRate;
//				   scanArea.timestamp = scanJSON.timestamp;
				   scanArea.gpsPoints = scanJSON.distanceReadings;
				   // scanArea.polyData is generated dynamically for redrawing
			   
			   scanAreas.push(scanArea);
		   }		   
		});	
}

///////////////
// PATH CODE //
///////////////

var pathExamples = {
	"path1":
	{"points":[ [ 53.965099,-1.083076 ], 
	 			[53.964935,-1.082089 ],
				[53.964594,-1.081188 ], 
				[53.964026,-1.080201 ], 
				[53.963408,-1.079063 ], 
				[53.962827,-1.07784],
				[53.962209,-1.076639],
				[53.961552,-1.075523],
				[53.961401,-1.074536],
				[53.961211,-1.073678],
				[53.960795,-1.073184],
				[53.960378,-1.07269],
				[53.959961,-1.072197],
				[53.959482,-1.071811],
				[53.959002,-1.071424]],
	 "timestamp":{"date":{"year":2016,"month":5,"day":17},"time":{"hour":16,"minute":30,"second":13,"nano":269000000}}}
	 
}

var unitPaths = [];

function newPath(){
	
	var pathsJSON = pathExamples;
	
	Object.keys(pathsJSON).forEach(function (pathKey) {
		   
	   var pathJSON = pathsJSON[pathKey];
	   
	   var unitPath;
	   unitPaths.forEach(function(existingPath) {
		   if(existingPath.id == pathKey){
			   unitPath = existingPath;
		   }
	   }, this);
	   
	   if(unitPath){
		   
	   }else{
		   // Else Create a new one
		   
		   var unitPath = new UnitPath();
		   	   unitPath.id = pathKey;
			   unitPath.gpsPoints = pathJSON.points;
			   // unitPath.polyData is generated dynamically for redrawing
		   
		   unitPaths.push(unitPath);
	   }		   
	});	
	
}
