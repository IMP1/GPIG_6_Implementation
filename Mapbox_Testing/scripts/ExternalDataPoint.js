// ExternalDataPoint Class
var ExternalDataPoint  = function(coordinates, dataType, symbol){

	this.type          = 'Feature';
	this.dataType      = dataType;
	this.timestamp     = Date.now();

	this.geometry      = {
		'type': 'Point',
		'coordinates': coordinates
	}

	this.properties = {
        "marker-symbol": symbol
    }
};