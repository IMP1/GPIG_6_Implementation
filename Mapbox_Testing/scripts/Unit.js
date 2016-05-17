// A Unit is either the C2 or a drone, storing coordinates and other properties required for area assignment etc

var Unit = function(id, symbol, coordinates, batteryLevel, status, lastUpdated){
    this.id = id;
    this.symbol = symbol;
    this.coordinates = coordinates;
    this.batteryLevel = batteryLevel;
    this.status = status;
    this.lastUpdated = lastUpdated;
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
	unit.id             = unitID;
	unit.batteryLevel   = unitJSON.batteryLevel;
	unit.coordinates[0] = unitJSON.locLat;
	unit.coordinates[1] = unitJSON.locLong;
	unit.status         = unitJSON.status;
	unit.timestamp      = unitJSON.timestamp;
}