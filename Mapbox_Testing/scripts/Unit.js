// A Unit is either the C2 or a drone, storing coordinates and other properties required for area assignment etc

var UnitCount = 0;

var Unit = function(id, symbol, coordinates, batteryLevel, status, lastUpdated){
    
    // If ID is C2
    
    if(id == 'c2'){
        this.name = 'C2';
    }else{
        this.name = 'Drone '+UnitCount;
    }
    
    UnitCount++;
    
    this.id = id;
    this.symbol = symbol;
    this.coordinates = coordinates;
    this.batteryLevel = batteryLevel;
    this.status = status;
    this.lastUpdated = dateFromJSON(lastUpdated);
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
	unit.coordinates[1] = unitJSON.locLat;
	unit.coordinates[0] = unitJSON.locLong;
	unit.status         = unitJSON.status;
    
    var newTimeStamp = dateFromJSON(unitJSON.timestamp);
    unit.lastUpdated = newTimeStamp;
}

function dateFromJSON(jsonTimestamp){    
    var date = new Date(jsonTimestamp.date.year, jsonTimestamp.date.month-1, jsonTimestamp.date.day, jsonTimestamp.time.hour, jsonTimestamp.time.minute, jsonTimestamp.time.second, 0);    
    return date;
}