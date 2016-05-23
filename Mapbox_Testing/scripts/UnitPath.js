
var UnitPath = function(){
    this.id = 0;
	this.gpsPoints = [];
	
	this.polyData = function(){
		var data = [];
		this.gpsPoints.forEach(function(gpsPoint) {
		   
		   var lat  = gpsPoint[0];
		   var lng  = gpsPoint[1];
		   
		   var latlong = new mapboxgl.LngLat(lng, lat);				   
		   var xy      = project(latlong);			   
		   data.push({"x":xy.x, "y":xy.y});
		   
	   }, this);
	   return data;
	};
	
};