// A Unit is either the C2 or a drone, storing coordinates and other properties required for area assignment etc

var UnitCount = 0;

var Unit = function(id){  
    
    // Assign name and symbol based on id
    this.id         = id;
    
    if(id == 'c2'){
        this.name   = 'C2';
        this.symbol = 'harbor';
    }else{
        this.name   = 'Drone '+UnitCount;
        this.symbol = 'marker';
    }
    
    // Create visual map marker  
    this.marker = new UnitMarker(this);
    
    // Init coordinates
    this.coordinates = [0, 0];
    
    UnitCount++;
    return this;
}

var UnitMarker = function(unit){
    this.type = "Feature";
    this.properties = {
        "marker-symbol": unit.symbol
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
    
	unit.id              = unitID;
	unit.batteryLevel    = unitJSON.batteryLevel;
	unit.coordinates[1]  = unitJSON.locLat;
	unit.coordinates[0]  = unitJSON.locLong;
	unit.status          = unitJSON.status;
    
    unit.marker.geometry.coordinates = unit.coordinates;
    
    if(unitJSON.currentPath && unitJSON.currentPath.length > 0){
        var pathCoordinates  = ConvertCoordinatesTo2DArray(unitJSON.currentPath, 1);
        unit.unitPath        = new UnitPath(pathCoordinates);
    }else{
        unit.unitPath = undefined;
    }
    
    unit.lastUpdated = dateFromJSON(unitJSON.timestamp);
}

