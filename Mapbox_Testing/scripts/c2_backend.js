// Debug Vars
var ONLINE = true;

///////////////////////
// Utility Functions //
///////////////////////

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

function dateFromJSON(jsonTimestamp){
    var date = new Date(jsonTimestamp.date.year, jsonTimestamp.date.month-1, jsonTimestamp.date.day, jsonTimestamp.time.hour, jsonTimestamp.time.minute, jsonTimestamp.time.second, 0);    
    return date;
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

function ConvertCoordinatesTo2DArray(JSONCoordinates, subsampleRate){
	var data = [];
	for (var i = 0; i < JSONCoordinates.length; i+=2/subsampleRate) {
		// Swapped for GeoJSON format
		var lat  = JSONCoordinates[i+1];
		var lng  = JSONCoordinates[i];		
		data.push([lat, lng]);			
	}
	return data;
}














/////////////////////////
// FRONTEND COMMS CODE //
/////////////////////////

function setupAPICalls(){
	
	var refreshRate = 1000;
	
	getUnitsInfo();
	getScanInfo();
	
	setInterval(getUnitsInfo, refreshRate);
	setInterval(getScanInfo, refreshRate);
	
}

function setupKeypresses(){
	document.onkeydown = function(evt) {
		evt = evt || window.event;
		if (evt.keyCode == 27) {
			// Escape
			cancelSearchAreaCreation();
		}
	};
}

















//////////////////////
// SEARCH UNIT CODE //
//////////////////////

var unitExamples = {
	"c2":
	{"batteryLevel":1,
	 "locLat":53.959,
	 "locLong":-1.09369,
	 "status":"Moving",
	 "timestamp":{"date":{"year":2016,"month":5,"day":17},"time":{"hour":16,"minute":30,"second":13,"nano":269000000}}}
	 
	 ,"Drone 1":
	{"batteryLevel":1,
	 "locLat":53.95566264825,
	 "locLong":-1.07909046359,
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

function getUnitsInfo(){
	
	if(ONLINE){
	
		var xmlHttpUnits = new XMLHttpRequest();
	    	xmlHttpUnits.open( "GET", "http://localhost:8081/GetDroneInfo", true ); // true for asynchronous request
						
			xmlHttpUnits.onload = function (e) {
				if (xmlHttpUnits.readyState === 4) {
					if (xmlHttpUnits.status === 200) {
						parseUnitsInfo(JSON.parse(xmlHttpUnits.responseText));
					} else {
						console.error(xmlHttpUnits.statusText);
					}
				}
			};
			xmlHttpUnits.onerror = function (e) {
				console.error(xmlHttpUnits.statusText);
			};
			xmlHttpUnits.send(null);		
	
	}else{
		parseUnitsInfo(unitExamples);
	}
}

function parseUnitsInfo(unitsJSON){
	
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
		   addNewUnit(unitKey, unitJSON);
	   }
	   
	   // TODO : Remove Units if they no longer exist
	   
	});
	
	// Redraw UI
	updateUnitUI();
	
}

function addNewUnit(unitKey, unitJSON){
	
	// Create new Unit Object	
	var unit = new Unit(unitKey);
	updateUnitFromJSON(unit, unitKey, unitJSON);	
	units.push(unit);
	
	// Add to Map
	addNewUnitMapLayer(unit);
	
	// Add controls
	addNewUnitControls(unit);

	updateUnitFeatureCollections()
	showAllUnits();
}


function recallUnits(){
		
		var xmlHttpRecall = new XMLHttpRequest();
	    	xmlHttpRecall.open( "GET", "http://localhost:8081/RecallUnits", true ); // true for asynchronous request
						
			xmlHttpRecall.onload = function (e) {
				if (xmlHttpRecall.readyState === 4) {
					ShowNewMessage('Drone Recall Succesful', '', 'success');
				}
			};
			xmlHttpRecall.onerror = function (e) {
				console.error(xmlHttpRecall.statusText);
				ShowNewMessage('Drone Recall Error', 'Unable to Recall Drones', 'high');
			};
			xmlHttpRecall.send(null);	
	
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

function cancelSearchAreaCreation(){
	if(currentSearchArea){
		svg.selectAll('#SearchArea-'+currentSearchArea.id).remove();
		svg.selectAll('#SearchArea-Line-'+currentSearchArea.id).remove();
		svg.selectAll('#SearchArea-Marker-'+currentSearchArea.id).remove();
		svg.selectAll('#SearchArea-Text-'+currentSearchArea.id).remove();    
		searchAreaArray.pop();
		currentSearchArea = null;
		editingSearchArea = false;
		updateMap();
	}
}

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
				
				var urlString = "http://localhost:8081/AssignSearchAreas?latitude="+searchArea.center.lat
								+"&longitude="+searchArea.center.lng
								+"&numberRequested="+searchArea.requestedDrones
								+"&radius="+searchArea.radius;
				
				var xmlHttpAssignSearchAreas = new XMLHttpRequest();
				
					xmlHttpAssignSearchAreas.open( "GET", urlString, true ); // true for asynchronous request
								
					xmlHttpAssignSearchAreas.onload = function (e) {
						if (xmlHttpAssignSearchAreas.readyState === 4) {
							if (xmlHttpAssignSearchAreas.status === 200) {
								parseSearchAreaAssignmentResponse(JSON.parse(xmlHttpAssignSearchAreas.responseText), searchArea);
							} else {
								console.error(xmlHttpAssignSearchAreas.statusText);
								ShowNewMessage('Search Area Assignment Error', 'Unable to Recall Drones', 'high');
							}
						}
					};
					xmlHttpAssignSearchAreas.onerror = function (e) {
						console.error(xmlHttpAssignSearchAreas.statusText);
						ShowNewMessage('Search Area Assignment Error', 'Unable to connect to C2', 'high');
					};
					xmlHttpAssignSearchAreas.send(null);				
				
			}
		
		}else{
			
			ShowNewMessage('Search Area Assignment Error', 'Cannot to connect to C2', 'medium');
						
		}
		
		
	}, this);	
	
	currentlyAssigningSearchAreas = false;
	
}

function parseSearchAreaAssignmentResponse(searchAreaResponse, searchArea){
	
	if (searchAreaResponse.length > 0){
		searchAreaResponse.forEach(function(droneID) {
			
			var unit = getByAttr(units, 'id', droneID);
			if(!unit){
				ShowNewMessage('Drone Assignment Error', 'Assigned Drone ID ('+droneID+') does not exist.', 'high');
			}else{
				ShowNewMessage('Succesfully Assigned Drone', unit.name+' assigned to search area '+searchArea.id+'.', 'success');
				searchArea.assignedDrones.push(unit);
				searchArea.hasBeenAssignedDrones = true;
				removeUnasignedSearchAreas(unit, searchArea);
			}					
			
		}, this);
		
	}
}

function removeUnasignedSearchAreas(assignedUnit, assignedSearchArea){

	// Remove Search Areas which previously had this unit assigned and now have none
	// Loop backwards through array to enable removal

	for (var i = searchAreaArray.length - 1; i >= 0; i--) {

		var searchArea = searchAreaArray[i];

		// Skip if its the just assigned Search Area
		if(searchArea == assignedSearchArea || searchArea.assignedDrones.length == 0) continue;

		// Loop backwards through array to enable removal
		for (var d = searchArea.assignedDrones.length - 1; d >= 0; d--) {
			var unit = searchArea.assignedDrones[d];
			if(unit == assignedUnit){
				console.log('unit reassigned')
				// This Search Area had the now re-assigned drone, so remove from its assigned drones
				searchArea.assignedDrones.splice(d, 1);
			}

		}

		if(searchArea.assignedDrones.length == 0){
			deleteSearchArea(searchArea);
		}	

	}
}

function deleteAllSearchAreas(){
    
    if(!currentSearchArea){
		searchAreaArray.forEach(function(searchArea){
			deleteSearchAreaView(searchArea);
		});
		searchAreaArray = [];
	}else{
		ShowNewMessage('Search Area Clearance Error', 'Cannot clear whilst creating new search area.', 'medium');
	}    
    
}
















///////////////
// SCAN CODE //
///////////////

var scanAreas = [];
var lastTimestamp = new Date('1900-11-25 10:11:55');

 Date.prototype.timestampFormat = function() {
   var yyyy = this.getFullYear().toString();
   var MM   = (this.getMonth()+1).toString(); // getMonth() is zero-based
   var dd   = this.getUTCDate().toString();
   var HH   = this.getHours().toString();
   var mm   = this.getMinutes().toString();
   var ss   = this.getSeconds().toString();
   var dateString =  yyyy+'-'+(MM < 10 ? '0': '')+MM+'-'+(dd < 10 ? '0': '')+dd+'_'+(HH < 10 ? '0': '')+HH+'-'+(mm < 10 ? '0': '')+mm+'-'+(ss < 10 ? '0': '')+ss;
   return dateString;
 };

function getScanInfo(){
	var scanAreasJSON;
	
	if(ONLINE){
		
		var xmlHttpScans = new XMLHttpRequest();
	    	xmlHttpScans.open( "GET", "http://localhost:8081/GetScanInfo?last_timestamp="+lastTimestamp.timestampFormat(), true ); // false for synchronous request
			
			xmlHttpScans.onload = function (e) {
				if (xmlHttpScans.readyState === 4) {
					if (xmlHttpScans.status === 200) {
						parseScanAreaResponse(JSON.parse(xmlHttpScans.responseText));
					} else {
						console.error(xmlHttpScans.statusText);
					}
				}
			};
			xmlHttpScans.onerror = function (e) {
				console.error(xmlHttpScans.statusText);
			};
			xmlHttpScans.send(null);	
			
	}else{
		parseScanAreaResponse(scanTestData);
	}
	
}

function parseScanAreaResponse(scanAreasJSON){
	
	Object.keys(scanAreasJSON).forEach(function (scanKey) {
		
		var scanJSON = scanAreasJSON[scanKey];
		
		var subsampleRate = 1;
		var polygonCoordinates = ConvertCoordinatesTo2DArray(scanJSON.distanceReadings, subsampleRate);
			
		var scanArea = new ScanArea(scanKey, scanJSON.depth, scanJSON.flowRate, [polygonCoordinates], scanJSON.received)
			scanArea.center = [scanJSON.locLat, scanJSON.locLong];
		scanData.features.push(scanArea);

		if(scanArea.timestamp > lastTimestamp){
			lastTimestamp = scanArea.timestamp;
		}		
		
		
	});		
		
	// Redraw Map
	map.getSource('ScanAreaData').setData(scanData);
}