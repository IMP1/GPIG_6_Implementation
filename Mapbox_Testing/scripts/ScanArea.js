// SearchArea Class
var ScanArea = function(id, depth, flowrate, coordinates){
	this.type = 'Feature';
	this.depth = 0;
	this.flowrate = 0;
	this.id = id;
	this.geometry = {
		'type': 'Polygon',
		'coordinates': coordinates
	}
	return this;
};

function ConvertCoordinatesTo2DArray(JSONCoordinates){
	var data = [];
	for (var i = 0; i < JSONCoordinates.length; i+=2) {
		// Swapped for GeoJSON format
		var lat  = JSONCoordinates[i+1];
		var lng  = JSONCoordinates[i];		
		data.push([lat, lng]);			
	}
	return data;
}