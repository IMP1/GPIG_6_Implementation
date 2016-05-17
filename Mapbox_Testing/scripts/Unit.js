// A Unit is either the C2 or a drone, storing coordinates and other properties required for area assignment etc

var Unit = function(id, symbol, coordinates, batteryLevel, status, lastUpdated){
    this.id = id;
    this.symbol = symbol;
    this.coordinates = coordinates;
    this.batteryLevel = batteryLevel;
    this.status = status;
    this.lastUpdated = dateFromJSON(lastUpdated);
    this.timeSinceUpdate = 0;
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
    
    var newTimeStamp = dateFromJSON(unitJSON.timestamp);
    unit.timeSinceUpdate = (newTimeStamp - unit.lastUpdated)/1000; // Convert to seconds
    unit.lastUpdated = newTimeStamp;
}

function dateFromJSON(jsonTimestamp){
    var date = new Date(jsonTimestamp.date.year, jsonTimestamp.date.month, jsonTimestamp.date.day, jsonTimestamp.time.hour, jsonTimestamp.time.minute, jsonTimestamp.time.second, jsonTimestamp.time.nano);    
    return date;
}