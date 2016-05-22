// SearchArea Class
//var scanAreaID = 0;

var ScanArea = function(){
    this.id = 0;
    this.center = [];
	this.depth = 0;
	this.flowrate = 0;
	this.timestamp = {};
	this.gpsPoints = [];	
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

var ScanAreaGeoJSON = function(id, coordinates){
	this.type = 'Feature';
	this.properties = {
		'id': id
	}
	this.geometry = {
		'type': 'Polygon',
		'coordinates': coordinates
	}
	return this;
};