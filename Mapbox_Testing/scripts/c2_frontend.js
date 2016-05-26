///////////////////////
// Utility Functions //
///////////////////////

function offsetCoordinates(inputCoord){
    var coords = [inputCoord[0]+latOffset, inputCoord[1]+longOffset];
    return coords;
}

function project(d) {
  return map.project(getLL(d));
}

function getLL(d) {
  var latlong = new mapboxgl.LngLat(d.lng, d.lat);
  return latlong;
}

function unproject(a) {
    return map.unproject({x: a[0], y: a[1]});
}

function distance(ll0, ll1) {
    var p0 = project(ll0);
    var p1 = project(ll1);
    var dist = Math.sqrt((p1.x - p0.x)*(p1.x - p0.x) + (p1.y - p0.y)*(p1.y-p0.y));
    return dist;
}





///////////////////
// Constant Vars //
///////////////////

var mapCenter  = [-1.0873, 53.9600];
var defaultZoom = 18;
var latOffset  = 0;
var longOffset = 0;

//// Global Vars
var editingSearchArea = false;
var refreshRate = 100; //ms
var floodOutlineVisible = true;




///////////////////
// Button Events //
///////////////////

document.getElementById('btn-assign-search-areas').addEventListener('click', function(e) {            
    assignSearchAreas();
});

document.getElementById('btn-clear-all').addEventListener('click', function(e) {            
    deleteAllSearchAreas();
});

document.getElementById('btn-recall-all').addEventListener('click', function(e) {            
    recallUnits();
});

document.getElementById('btn-see-all').addEventListener('click', function(e) {            
    showAllUnits();
});

document.getElementById('btn-show-flood-outline').addEventListener('click', function(e) {            
    toggleFloodOutline();
});



/////////////////////////
// Feature Collections //
/////////////////////////

var scanData = {
    "type": "FeatureCollection",
    "features": []
};

var unitPathData = {
    "type": "FeatureCollection",
    "features": []
};

var markers = {
    "type": "FeatureCollection",
    "features": []
};



///////////////
// Map Setup //
///////////////
    
mapboxgl.accessToken = 'pk.eyJ1IjoiY29ybWFja2FsaSIsImEiOiJjaW55dTRtMmUwMHJxdmZtMjMyajI0ZHNtIn0.crRNON_GYqYZDSWraRTfBw';

var map = new mapboxgl.Map({
    container: 'map', 
    style: 'mapbox://styles/cormackali/cinyvygoz0004cbm9atdy33h5', 
    center: offsetCoordinates(mapCenter), 
    zoom: defaultZoom
})

map.on('load', function () {
    
    map.addSource("markers", {
        "type": "geojson",
        "data": markers
    });
   
    updateMap();
    
    // ScanArea Data
    map.addSource('ScanAreaData',{
        "type": "geojson",
        "data": scanData
    });
    
    map.addLayer({
        'id': 'ScanAreaData',
        'type': 'fill',
        'source': 'ScanAreaData',
        'layout': {},
        'paint': {
            'fill-color': '#088',
            'fill-opacity': 0.5
        }
    });
    
    // Unit Movement Paths
    
    map.addSource('UnitPathData',{
        "type": "geojson",
        "data": unitPathData
    });
    
    map.addLayer({
        "id": "route",
        "type": "line",
        "source": "UnitPathData",
        "layout": {
            "line-join": "round",
            "line-cap": "round"
        },
        "paint": {
            "line-color": "#888",
            "line-width": 4
        }
    });
    
    // Setup
    setupAPICalls(); 
    setInterval(refreshUI, refreshRate);     
    setupKeypresses();  
    enableSearchAreaDrawing();
    
    // Mapbox Elements
    map.addControl(new mapboxgl.Navigation());
    var toRemove = document.getElementsByClassName('mapboxgl-ctrl-bottom-right')[0];
    toRemove.parentNode.removeChild(toRemove); 

});

map.on("render", function() {
    redrawSearchAreas()
})

var zoomLevel_popups = 17;

map.on('move', function(e) {
    var z = e.target.getZoom();
    if (z < zoomLevel_popups) {
        removeAllPopups();
    }else{
        addNewPopups();
    }
});







///////////////////
// HTML Elements //
///////////////////

var filterGroup  = document.getElementById('filter-group');
var container    = map.getCanvasContainer();
var svg          = d3.select(container).append("svg");
var searchAreas  = document.getElementById('search-areas');
var map_overlays = document.getElementById('map_overlays');







///////////////////
// Map Functions //
///////////////////

function showAllUnits(){  
    if(markers.features.length >= 2){

        console.log(markers.features);  
        var bounds = new mapboxgl.LngLatBounds();
        markers.features.forEach(function(feature) {
            bounds.extend(feature.geometry.coordinates);
        });
        map.fitBounds(bounds, { padding: '100' });    
    }
}

function updateMap(){
    if(editingSearchArea){
        map['doubleClickZoom'].disable();
        map['dragPan'].disable();
    }else{
        map['doubleClickZoom'].enable();
        map['dragPan'].enable();
    }
}

function addNewUnitMapLayer(unit){
    map.addLayer({
        "id":     unit.id,
        "type":   "symbol",
        "source": "markers",
        "layout": {
            "icon-image": unit.symbol + "-15",
            "icon-allow-overlap": true
        }
    });   
}

function toggleFloodOutline(){
    floodOutlineVisible = !floodOutlineVisible;
    if(!floodOutlineVisible){
        map.setLayoutProperty('sensor-edge', 'visibility', 'none');
    }else{
        map.setLayoutProperty('sensor-edge', 'visibility', 'visible');
    }
    
}





////////////////
// Refresh UI //
////////////////

function refreshUI(){
    updateUnitUI();
    redrawSearchAreas();    
    updateUnitFeatureCollections();
}

function updateUnitFeatureCollections(){
    
    markers.features      = [];
    unitPathData.features = [];
    
    units.forEach(function(unit) { 
        // Paths
        if(unit.unitPath){
          unitPathData.features.push(unit.unitPath);
        }
        
        // Map Markers
        markers.features.push(unit.marker);
    }, this);
    
    // Update Map Sources
    map.getSource('UnitPathData').setData(unitPathData);
    map.getSource('markers').setData(markers);
    
}






/////////////////////////
// Search Area Drawing //
/////////////////////////

var mouseDownCoords;
var minRadius = 5;
var maxRadius = 500;//metres

function enableSearchAreaDrawing(){
    
    svg.on("mousedown", function() {
        // Enable dragging, only draws when map not moved.
        mouseDownCoords = d3.mouse(this);
    })

    svg.on("mouseup", function() {
        
        // New Search Area            
        var p = d3.mouse(this);
        
        // Enable dragging, only draws when map not moved.
        if(!arraysEqual(p, mouseDownCoords)) return;
        
        var ll = unproject([p[0],p[1]])
        
        if(!currentSearchArea){        
            currentSearchArea        = new SearchArea();
            currentSearchArea.center = ll;   
            currentSearchArea.outer  = ll;
            searchAreaArray.push(currentSearchArea); 
            editingSearchArea        = true;            
        }else{
            currentSearchArea.outer  = ll;   
            currentSearchArea.radius = unprojectedDistance(currentSearchArea.center, currentSearchArea.outer)
            
            // Check MinRadius < Radius < MaxRadius
            
            if(currentSearchArea.radius < maxRadius && currentSearchArea.radius > minRadius){
                currentSearchArea.complete = true;     
                addNewSearchAreaControls(currentSearchArea); 
                currentSearchArea = null;
                editingSearchArea = false;
            }else if(currentSearchArea.radius >= maxRadius){
                ShowNewMessage('Search Area Creation Error', 'Search Area exceeds maximum radius of '+maxRadius+'m ('+Math.round(currentSearchArea.radius)+'m)', 'medium');
            }else if(currentSearchArea.radius <= minRadius){
                ShowNewMessage('Search Area Creation Error', 'Search Area under minimum radius of '+minRadius+'m ('+Math.round(currentSearchArea.radius)+'m)', 'medium');
            }
        }
        
        updateMap();        
        redrawSearchAreas();        
    })
    
    svg.on("mousemove.circle", function() {    
        if(!currentSearchArea) return;        
        var p = d3.mouse(this);
        var ll = unproject([p[0],p[1]])        
        currentSearchArea.outer = ll;        
        redrawSearchAreas();
    })
    
}

function deleteSearchAreaView(searchArea){    
    svg.selectAll('#SearchArea-'+searchArea.id).remove();
    svg.selectAll('#SearchArea-Line-'+searchArea.id).remove();
    svg.selectAll('#SearchArea-Marker-'+searchArea.id).remove();
    svg.selectAll('#SearchArea-Text-'+searchArea.id).remove();    
    deleteSearchAreaControls(searchArea);
}

function redrawSearchAreas(){    

    searchAreaArray.forEach(function(searchArea){
        
        redrawSearchAreaControls(searchArea);
        
        searchArea.drawRadius = distance(searchArea.center, searchArea.outer);
        searchArea.radius     = unprojectedDistance(searchArea.center, searchArea.outer)
        
        // Remove First
        svg.selectAll('#SearchArea-'+searchArea.id).remove();
        svg.selectAll('#SearchArea-Line-'+searchArea.id).remove();
        svg.selectAll('#SearchArea-Marker-'+searchArea.id).remove();
        svg.selectAll('#SearchArea-Text-'+searchArea.id).remove();
        
        // Redraw
        var circleLasso = svg.selectAll('#SearchArea-'+searchArea.id)
            .data([searchArea.drawRadius])
            .enter()
            .append("circle")     
            
        .attr({
          cx: project(searchArea.center).x,
          cy: project(searchArea.center).y,
          r: searchArea.drawRadius,
          id:'SearchArea-'+searchArea.id
        });
        
        if(searchArea.assignedDrones.length > 0){
            // Drones have been assigned to the search area
            circleLasso.style({           
                stroke: "#414852",
                fill: "#009900",
                "fill-opacity": 0.2
            })    
        }else{
            circleLasso.style({           
                stroke: "#414852",
                fill: "#010",
                "fill-opacity": 0.1
            })    
        }          
        
        // Draw Line
        
        var line = svg.selectAll('SearchArea-Line-'+searchArea.id)
            .data([searchArea.outer])
            .enter()
            .append("line")
            
        .attr({
            x1: project(searchArea.center).x,
            y1: project(searchArea.center).y,
            x2: project(searchArea.outer).x,
            y2: project(searchArea.outer).y,
            id:'SearchArea-Line-'+searchArea.id
        })
        
        .style({
            stroke: "#414852",
            "stroke-dasharray": "5 5"
        })
        
        // Markers
        
        var markerRadius = 15;
        
        var markers = svg.selectAll('SearchArea-Marker-'+searchArea.id)
            .data([searchArea.center, searchArea.outer])
            .enter()
            .append("circle")
            
        .attr({
          cx: function(d) { return project(d).x},
          cy: function(d) { return project(d).y},
          r: markerRadius,
          fill: "#414852",
          "fill-opacity":0.9,
          id:'SearchArea-Marker-'+searchArea.id
        })
        
        .style({
          "cursor": "move"
        })
        
        // ID
        
        var searcharea_id_text = svg.selectAll('SearchArea-Text-'+searchArea.id)
            .data([searchArea.center])
            .enter()
            .append("text")
            
        .attr({
          x: project(searchArea.center).x,
          y: project(searchArea.center).y,
          fill: "#fff",
          id:'SearchArea-Text-'+searchArea.id
        })
        .attr("font-size", "20px")
        .attr("text-anchor", "middle")
        .attr("dy", ".35em")
        
        .text(searchArea.id);
        
        if(searchArea.assignedDrones.length > 0){
            // Drones have been assigned to the search area
            svg.selectAll('#SearchArea-Line-'+searchArea.id).remove();
            svg.selectAll('#SearchArea-Marker-'+searchArea.id).remove();    
          
        }
        
    });
    
}









////////////////
// Flood Info //
////////////////

var infoPopups = [];
var lastDataScanned = 0;
var subsampleScans = 5; // Use every nth scan for center;

function roundToDecimalPlaces(num, dp){
    var mult = Math.pow(10, dp);
    return Math.round(num * mult) / mult
}

function addNewPopups(){
    // Go from last scan data
    if(map.getZoom() > zoomLevel_popups){
        for(var i = lastDataScanned; i < scanData.features.length; i+= subsampleScans){
            addNewPopupIfRequired(i);
        }
        lastDataScanned = scanData.features.length;
    }
}

function addNewPopupIfRequired(i){
    var scan         = scanData.features[i];
    var centerLngLat = new mapboxgl.LngLat(scan.center[1], scan.center[0]);
    
    // Check if there's a tooltip within n metres        
    var tooltip_radius = 40;
    var within_radius = false;
    
    infoPopups.forEach(function(tooltip) {
        
        var dist = unprojectedDistance(tooltip._lngLat, centerLngLat);
        if(dist < tooltip_radius){
            within_radius = true;
        }            
        
    }, this);
    
    if(!within_radius){
    
        var depth_string    = roundToDecimalPlaces(scan.depth, 2)   +'m';
        var flowrate_string = roundToDecimalPlaces(scan.flowrate, 2)+'m/s';
        
        var depth_severity = getDepthSeverity(scan.depth);
        var flow_severity  = getFlowSeverity(scan.flowrate);

        var depth_class     = 'severity-'+depth_severity;
        var flow_class      = 'severity-'+flow_severity;
        
        var warnings        = getWarnings(depth_severity, flow_severity);
        
        var html_string     = '';
        
        console.log(depth_severity, flow_severity)

        warnings.forEach(function(warning) {
            html_string    += '<div class=\'warning\'>  <div class=\'icon\'><img src=\'images\\icons\\warning_'+warning[0]+'.png\'></img></div>     <div class=\'warning_text\'><div class=\'inner\'>'+warning[1]+'</div></div>   </div>'
        }, this);

        html_string        += '<div class=\'left\'>   <div class=\'img icon fa fa-sort-amount-asc '+depth_class+'\'></div> <div class=\'text '+depth_class+'\'>'+depth_string+'</div>   </div>';
        html_string        += '<div class=\'right\'>  <div class=\'img icon fa fa-tachometer '+flow_class+'\'>   </div><div class=\'text '+flow_class+'\'>'+flowrate_string+'</div> </div>';

        var tooltip = new mapboxgl.Popup({closeOnClick: false, closeButton:false})
            .setLngLat([scan.center[1], scan.center[0]])
            .setHTML(html_string)
            .addTo(map);
            
        infoPopups.push(tooltip);
    }  
}

function getWarnings(depth_severity, flow_severity){
    var warnings = [];
    
    // Warnings of form 'icon', 'text' (2 Lines)
    
    if(depth_severity >= 6){
        warnings.push(['boat', 'Boat Required']);
    }
    
    if(depth_severity >= 4){
        warnings.push(['boat', 'Rapid Water']);
    }
    
    return warnings;
}

function getDepthSeverity(depth){
    if(depth > 5){
        return 7;
    }else if(depth > 4){
        return 6;
    }else if(depth > 3){
        return 5;
    }else if(depth > 2){
        return 4;
    }else if(depth > 1){
        return 3;
    }else if(depth > .5){
        return 2;
    }else{
        return 1;
    }
}

function getFlowSeverity(flow){
    if(flow > 2){
        return 7;
    }else if(flow > 1.6){
        return 6;
    }else if(flow > 1.3){
        return 5;
    }else if(flow > .9){
        return 4;
    }else if(flow > .7){
        return 3;
    }else if(flow > .4){
        return 2;
    }else{
        return 1;
    }
}

function removeAllPopups(){
    infoPopups.forEach(function(tooltip) {
        tooltip.remove();
    }, this);
    lastDataScanned = 0;
    infoPopups = [];
}