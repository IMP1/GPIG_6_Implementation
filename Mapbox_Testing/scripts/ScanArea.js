// ScanArea Class
var ScanArea = function(id, coordinates, timestamp){
	this.type = 'Feature';
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