// A Unit is either the C2 or a drone, storing coordinates and other properties required for area assignment etc

// var Unit = function(id, symbol, coordinates, batteryLevel, status, lastUpdated){    
//     this = Unit(id);    
//     this.id = id;
//     this.symbol = symbol;
//     this.coordinates = coordinates;
//     this.batteryLevel = batteryLevel;
//     this.status = status;
//     this.lastUpdated = dateFromJSON(lastUpdated);
// }

var UnitCount = 0;

var Unit = function(id){   
    if(id == 'c2'){
        this.name = 'C2';
        this.symbol = 'castle';
    }else{
        this.name = 'Drone '+UnitCount;
        this.symbol = 'marker';
    }
    this.coordinates = [0, 0];
    UnitCount++;
    return this;
}

var UnitMarker = function(unit){
    this.type = "Feature";
    this.properties = {
        "name" : unit.id,
        "marker-symbol": unit.symbol
    }
    this.geometry = {
        "type": "Point",
        "coordinates": unit.coordinates
    }
}

function updateUnitFromJSON(unit, unitID, unitJSON){
    
	unit.id              = unitID;
	unit.batteryLevel    = unitJSON.batteryLevel;
	unit.coordinates[1]  = unitJSON.locLat;
	unit.coordinates[0]  = unitJSON.locLong;
	unit.status          = unitJSON.status;
    
    if(unitJSON.currentPath && unitJSON.currentPath.length > 0){
        var pathCoordinates  = ConvertCoordinatesTo2DArray(unitJSON.currentPath, 1);
        unit.unitPath = new UnitPath(pathCoordinates);
    }else{
        unit.unitPath = undefined;
    }
    
    var newTimeStamp = dateFromJSON(unitJSON.timestamp);
    unit.lastUpdated = newTimeStamp;
}

