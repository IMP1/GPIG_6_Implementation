// ScanData Class
var ScanInfo = function(id, depth, flowrate, center, timestamp){
	this.id        = 'data-'+id;
	this.center    = center;
	this.depth     = depth;
	this.flowrate  = flowrate;	
	if(timestamp){
		this.timestamp = dateFromJSON(timestamp);
	}
	return this;
};