// ScanArea Class
var ScanArea = function(id, depth, flowrate, coordinates, timestamp){
	this.type = 'Feature';
	this.depth = 0;
	this.flowrate = 0;
	this.id = id;
	if(timestamp){
		this.timestamp = dateFromJSON(timestamp);
	}	
	this.geometry = {
		'type': 'Polygon',
		'coordinates': coordinates
	}
	return this;
};