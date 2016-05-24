var UnitPath = function(pathCoordinates){
	this.type = 'Feature';
	this.properties = {};
	this.geometry = {
		'type': 'LineString',
		'coordinates': pathCoordinates
	}
	return this;
};