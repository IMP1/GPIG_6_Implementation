// A Unit is either the C2 or a drone, storing coordinates and other properties required for area assignment etc

var UnitCount = 0;

var Unit = function(id){  
    
    // Assign name and symbol based on id
    this.id         = id;
    
    if(id == 'c2'){
        this.name   = 'C2';
        this.symbol = 'ferry';
    }else{
        this.name   = 'Drone '+UnitCount;
        this.symbol = 'marker';
    }
    
    // Create visual map marker  
    this.marker                = new UnitMarker(this);
    
    // Init coordinates
    this.coordinates           = [0, 0];    
    
    // Faults
    this.warningBatteryLevel   = 20;
    this.batteryFaultDisplayed = false;
    this.unseenFaultDisplayed  = false;
    this.unseenFaultTooltip;
    
    // Bearing
    this.bearing = 0;
    
    // Depth
    this.lastKnownDepth = 0;

    UnitCount++;
    return this;
}

var TempMarker = function(coordinates){
    this.geometry = {
        "type": "Point",
        "coordinates": coordinates
    }
}

var UnitMarker = function(unit){
    var markerSymbol = unit.symbol;
    markerSymbol += '-15';   
     
    this.type = "Feature";
    this.properties = {
        "marker-symbol": markerSymbol
    }
    this.geometry = {
        "type": "Point",
        "coordinates": unit.coordinates
    }
}

var UnitPath = function(pathCoordinates){
	this.type = 'Feature';
	this.properties = {};
	this.geometry = {
		'type': 'LineString',
		'coordinates': pathCoordinates
	}
	return this;
};

function updateUnitFromJSON(unit, unitID, unitJSON){
    
    // Store For Bearing Calc (bit hacky)
    var storedMarker = new TempMarker(JSON.parse(JSON.stringify(unit.coordinates)));
    
    // JSON
	unit.id              = unitID;
	unit.batteryLevel    = unitJSON.batteryLevel;
	unit.coordinates[1]  = unitJSON.locLat;
	unit.coordinates[0]  = unitJSON.locLong;
	unit.status          = unitJSON.status.toLowerCase();
    
    unit.marker.geometry.coordinates = unit.coordinates;
    
    if(unitJSON.currentPath && unitJSON.currentPath.length > 0){
        var pathCoordinates  = ConvertCoordinatesTo2DArray(unitJSON.currentPath, 1);
        unit.unitPath        = new UnitPath(pathCoordinates);
    }else{
        unit.unitPath = undefined;
    }
    
    unit.lastUpdated = dateFromJSON(unitJSON.timestamp);
    
    // Bearing
    
    if(!storedMarker.geometry.coordinates.equals(unit.marker.geometry.coordinates)){
        unit.bearing = turf.bearing(unit.marker, storedMarker);
    }
 
}

String.prototype.capitalizeFirstLetter = function() {
    return this.charAt(0).toUpperCase() + this.slice(1);
}