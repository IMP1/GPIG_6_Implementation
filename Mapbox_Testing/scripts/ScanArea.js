// ScanArea Class
var ScanArea = function(id, depth, flowrate, coordinates, timestamp){
	this.type = 'Feature';
	this.center = [];
	this.depth = depth;
	this.flowrate = flowrate;
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