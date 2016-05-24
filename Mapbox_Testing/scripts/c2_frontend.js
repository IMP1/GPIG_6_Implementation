//// Utility Functions

function offsetCoordinates(inputCoord){
    var coords = [inputCoord[0]+latOffset, inputCoord[1]+longOffset];
    return coords;
}

//// Constant Vars
var mapCenter  = [-1.0873, 53.9600];
var defaultZoom = 15;
var latOffset = -.005;
var longOffset = 0;

//// Global Vars
var searchAreaCreationEnabled = true;
var editingSearchArea = false;

//// Button Events
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

function showAllUnits(){
    
    var bounds = new mapboxgl.LngLatBounds();

    markers.features.forEach(function(feature) {
        bounds.extend(feature.geometry.coordinates);
    });

    map.fitBounds(bounds, { padding: '100' });
    
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

//// Map Setup
    
mapboxgl.accessToken = 'pk.eyJ1IjoiY29ybWFja2FsaSIsImEiOiJjaW55dTRtMmUwMHJxdmZtMjMyajI0ZHNtIn0.crRNON_GYqYZDSWraRTfBw';

var map = new mapboxgl.Map({
    container: 'map', // container id
    style: 'mapbox://styles/cormackali/cinyvygoz0004cbm9atdy33h5', 
    center: offsetCoordinates(mapCenter), 
    zoom: defaultZoom
});

var scanData = {
    "type": "FeatureCollection",
    "features": []
};

var unitPathData = {
    "type": "FeatureCollection",
    "features": []
};

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
    
     // Backend Call
    setupAPICalls();  
    setupKeypresses();  
    setInterval(refreshUI, 250);
    
    // Remove Mapbox Elements
    var toRemove = document.getElementsByClassName('mapboxgl-ctrl-bottom-right')[0];
    toRemove.parentNode.removeChild(toRemove);

});

//// HTML Elements
var filterGroup = document.getElementById('filter-group');
var container = map.getCanvasContainer();
var svg = d3.select(container).append("svg");
var searchAreas = document.getElementById('search-areas');
var map_overlays = document.getElementById('map_overlays');

// Add zoom controls
map.addControl(new mapboxgl.Navigation());

var markers = {
    "type": "FeatureCollection",
    "features": []
};

var unit_element_ids   = ['battery', 'state', 'lastseen', 'depth'];

function addNewUnitMarker(unit){
    
    // Adds a new Unit Map Marker    
    var unit_marker = new UnitMarker(unit);
    markers.features.push(unit_marker);
    
    var layerID = unit.id; 

    // Add a layer for this symbol type if it hasn't been added already.
    if (!map.getLayer(layerID)) {
       
        map.addLayer({
            "id": layerID,
            "type": "symbol",
            "source": "markers",
            "layout": {
                "icon-image": unit.symbol + "-15",
                "icon-allow-overlap": true
            },
            "filter": ["==", "marker-symbol", unit.symbol]
        });

        // Add HTML elements for each Unit
        var unit_element = document.createElement('div');
        unit_element.id = layerID;
        unit_element.className = 'unit';
    	unit_element.setAttribute('for', layerID);
        filterGroup.appendChild(unit_element);
        
        var unit_element_info = document.createElement('div');
        unit_element_info.className = 'info';        
        unit_element.appendChild(unit_element_info);

            var unit_element_icon = document.createElement('i');
            unit_element_icon.className = 'unit-icon maki  maki-'+unit.symbol;
            unit_element_info.appendChild(unit_element_icon);
            
            var unit_element_text = document.createElement('div');
            unit_element_text.className = 'unit-name';
            unit_element_text.textContent = name;
            unit_element_text.id = layerID+'-'+'name';
            unit_element_info.appendChild(unit_element_text);
            
        var unit_element_stats = document.createElement('div');
        unit_element_stats.className = 'stats';        
        unit_element.appendChild(unit_element_stats);
        
        var unit_icons = ['fa-battery-4', 'fa-feed', 'fa-cloud', 'fa-anchor'];
        var unit_text  = ['Battery : 100%', 'State : Scanning', 'Last Seen : 1s Ago', 'Depth : 10m'];
        
        for(var i = 0; i<4; i++){
        
            var unit_element_stats_stat = document.createElement('div');
            unit_element_stats_stat.className = 'stat';
            unit_element_stats.appendChild(unit_element_stats_stat);
            
                var unit_element_stats_stat_icon = document.createElement('div');
                unit_element_stats_stat_icon.className = 'icon fa '+unit_icons[i];
                unit_element_stats_stat.appendChild(unit_element_stats_stat_icon);
                
                var unit_element_stats_stat_text = document.createElement('div');
                unit_element_stats_stat_text.className = 'text';
                unit_element_stats_stat_text.id = layerID+'-'+unit_element_ids[i];
                unit_element_stats_stat_text.textContent = unit_text[i];
                unit_element_stats_stat.appendChild(unit_element_stats_stat_text);
                
        }
        
        // On click go to unit coordinates
        unit_element.addEventListener('click', function(e) {            
            map.flyTo({
                center: offsetCoordinates(unit_marker.geometry.coordinates),
                zoom: defaultZoom,        
                speed: 0.6, 
                curve: 1,         
                easing: function (t) {
                    return t;
                }
            });            
        });
    }
    
    return unit_marker;
    
}

function updateUnitUI(){
    
    var searchUnitsEmpty = document.getElementById('search-units-empty');
    if(units.length > 0){
        searchUnitsEmpty.hidden = true;
    }else{
        searchUnitsEmpty.hidden = false;
    }
    
    units.forEach(function(unit) {
        
	   var layerID = unit.id;
       
       // Name
        var element_name = document.getElementById(layerID+'-'+'name');
            element_name.textContent = unit.name;
        
        // Battery
        var element_battery = document.getElementById(layerID+'-'+unit_element_ids[0]);
            element_battery.textContent = Math.round(unit.batteryLevel*100) + '%';
        
        // State
        var element_state = document.getElementById(layerID+'-'+unit_element_ids[1]);
            element_state.textContent = unit.status;
        
        // Time
        var element_lastseen = document.getElementById(layerID+'-'+unit_element_ids[2]);
        
        var timeUnit = 's';
        var curTime = Date.now();
        var timeDif = Math.round((curTime-unit.lastUpdated) / 1000);
        
        element_lastseen.style.color = '';
        
        // Conv to mins
        var warningMins = 3;
        if(timeDif > 60*warningMins){
            timeDif = Math.round(timeDif/60);
            timeUnit = 'm';
            // element_lastseen.style.color = 'red';
            
            // Conv to hours
            if(timeDif > 60){
                timeDif = Math.round(timeDif/60);
                timeUnit = 'h';
            }  
        }
        
            
        element_lastseen.textContent = 'Last seen '+timeDif+timeUnit+' ago';
        
        // Depth
        var element_depth = document.getElementById(layerID+'-'+unit_element_ids[3]);
            element_depth.textContent = 'Depth : 20m';
       
       
   }, this);
   
}

function setMarkerPosition(lat, long, marker){    
    marker.geometry.coordinates = [lat, long];
}

function refreshUI(){
    updateUnitUI();
    redrawSearchAreasUI();
    updateUnitPaths();
}

function updateUnitPaths(){
    
    unitPathData.features = [];
    
    units.forEach(function(unit) {        
        if(unit.unitPath){
          unitPathData.features.push(unit.unitPath);
        }
    }, this);
    
    map.getSource('UnitPathData').setData(unitPathData);
}

// Map Projection/Unprojection

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

var mouseDownCoords;

svg.on("mousedown", function() {
    mouseDownCoords = d3.mouse(this);
})

var maxRadius = 500;//metres

svg.on("mouseup", function() {
    
    // New Search Area    
    if(!searchAreaCreationEnabled) return;
    
    var p  = d3.mouse(this);
    
    if(!arraysEqual(p, mouseDownCoords)) return;
    
    var ll = unproject([p[0],p[1]])
    
    if(!currentSearchArea){        
        currentSearchArea = new SearchArea();
        currentSearchArea.center = ll;   
        currentSearchArea.outer  = ll;
        searchAreaArray.push(currentSearchArea); 
        editingSearchArea = true;
        updateMap();
    }else{
        currentSearchArea.outer  = ll;   
        currentSearchArea.radius = unprojectedDistance(currentSearchArea.center, currentSearchArea.outer)
        
        // Check Radius < MaxRadius
        
        if(currentSearchArea.radius < maxRadius){
            currentSearchArea.complete = true;     
            addNewSearchArea(currentSearchArea); 
            currentSearchArea = null;
            editingSearchArea = false;
            updateMap();
        }else{
            ShowNewMessage('Search Area Creation Error', 'Search Area exceeds maximum radius of '+maxRadius+'m ('+Math.round(currentSearchArea.radius)+'m)', 'medium');
        }
    }
    
    redrawSearchAreas();
    
})

function addNewSearchArea(searchArea){
    
    var search_area = document.createElement('div');
        search_area.id = 'control-searcharea-'+searchArea.id;
        search_area.className = 'search-area';
        searchAreas.appendChild(search_area);
        
        var search_area_header = document.createElement('div');
        search_area_header.className = 'header';  
        search_area_header.textContent = searchArea.id;      
        search_area.appendChild(search_area_header);
        
        var search_area_drone_numbers = document.createElement('div');
        search_area_drone_numbers.className = 'drone-assignment';        
        search_area.appendChild(search_area_drone_numbers);
        
            var search_area_drone_arrow_up = document.createElement('div');
            search_area_drone_arrow_up.className = 'arrow fa fa-arrow-up';        
            search_area_drone_numbers.appendChild(search_area_drone_arrow_up);
            search_area_drone_arrow_up.addEventListener('click', function(e) {            
                changeDroneAssignmentForSearchArea(+1, searchArea);
            });
            
            var search_area_drone_arrow_down = document.createElement('div');
            search_area_drone_arrow_down.className = 'arrow fa fa-arrow-down';        
            search_area_drone_numbers.appendChild(search_area_drone_arrow_down);
            search_area_drone_arrow_down.addEventListener('click', function(e) {            
                changeDroneAssignmentForSearchArea(-1, searchArea);
            });
        
        var search_stat_id    = ['assigned', 'radius', 'status']
        
        var search_area_stats = document.createElement('div');
            search_area_stats.className = 'info';
            search_area.appendChild(search_area_stats);
        
        for(var i = 0; i<search_stat_id.length; i++){            
                
                var unit_element_stats_stat_text = document.createElement('div');
                unit_element_stats_stat_text.className = 'text';
                unit_element_stats_stat_text.id = 'control-searcharea-'+searchArea.id+'-'+search_stat_id[i];
                //unit_element_stats_stat_text.textContent = search_stat_text[i];
                search_area_stats.appendChild(unit_element_stats_stat_text);
                
        }
        
        var search_area_controls = document.createElement('div');
            search_area_controls.className = 'controls';
            search_area.appendChild(search_area_controls); 
            
            var search_area_controls_delete = document.createElement('div');
                search_area_controls_delete.className = 'button icon fa fa-close';
                search_area_controls.appendChild(search_area_controls_delete);
                search_area_controls.addEventListener('click', function(e) {            
                    deleteSearchArea(searchArea);
                });
                
    redrawSearchAreasUI();

}

function deleteSearchAreaView(searchArea){    
    svg.selectAll('#SearchArea-'+searchArea.id).remove();
    svg.selectAll('#SearchArea-Line-'+searchArea.id).remove();
    svg.selectAll('#SearchArea-Marker-'+searchArea.id).remove();
    svg.selectAll('#SearchArea-Text-'+searchArea.id).remove();    
    document.getElementById('control-searcharea-'+searchArea.id).remove()
}

svg.on("mousemove.circle", function() {
    
    if(!currentSearchArea) return;
    
    var p = d3.mouse(this);
    var ll = unproject([p[0],p[1]])
    
    currentSearchArea.outer = ll;
    
    redrawSearchAreas();
})

var dispatch = d3.dispatch("redrawSearchAreas", "clear");
d3.rebind(this, dispatch, "on")

var drag = d3.behavior.drag()
    .on("drag", function(d,i) {
        // if(!active) return;
        // if(circleSelected) {
        // dragging = true;
        // var p = d3.mouse(svg.node());
        // var ll = unproject([p[0],p[1]])
        // if(i) {
        //     circleOuter = ll;
        // } else {
        //     var dlat = circleCenter.lat - ll.lat;
        //     var dlng = circleCenter.lng - ll.lng;
        //     circleCenter = ll;
        //     circleOuter.lat -= dlat;
        //     circleOuter.lng -= dlng;
        // }
        // update();
        // } else {
        // return false;
        // }
    })
    .on("dragend", function(d) {
        // // kind of a dirty hack...
        // setTimeout(function() {
        // dragging = false;
        // },100)
    })
    
function redrawSearchAreasUI(){
    
    var searchAreasEmpty = document.getElementById('search-areas-empty');
    if(searchAreaArray.length > 0){
        searchAreasEmpty.hidden = true;
    }else{
        searchAreasEmpty.hidden = false;
    }
    
    searchAreaArray.forEach(function(searchArea){
        if(searchArea.complete){
            var htmlString = 'control-searcharea-'+searchArea.id+'-assigned';  
            document.getElementById(htmlString).textContent = 'Requesting '+searchArea.requestedDrones+ ' Search Units';
            
            htmlString = 'control-searcharea-'+searchArea.id+'-radius';  
            document.getElementById(htmlString).textContent = 'Radius: '+Math.round(searchArea.radius)+ 'm';
            
            htmlString = 'control-searcharea-'+searchArea.id+'-status';  
            
            if(searchArea.assignedDrones.length > 0){
                 var unitsString = '';
                searchArea.assignedDrones.forEach(function(unit) {
                    unitsString += ' '+unit.name;
                    unitsString += ',';
                }, this);
                unitsString = unitsString.substring(0, unitsString.length - 1);
                
                document.getElementById(htmlString).textContent = 'Assigned: '+unitsString;
            }else{
                document.getElementById(htmlString).textContent = 'Assigned: 0 Search Units'
            }           
           
        } 
          
    }, this);
    
}

function redrawSearchAreas(){
    
    redrawSearchAreasUI();

    searchAreaArray.forEach(function(searchArea){
        
        searchArea.drawRadius = distance(searchArea.center, searchArea.outer);
        searchArea.radius    = unprojectedDistance(searchArea.center, searchArea.outer)
        
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
        
        .call(drag)  
        
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
        
        // Redraw
        dispatch.redrawSearchAreas();

        
    });
    
}

map.on("render", function() {
    redrawSearchAreas()
    // redrawScanAreas();
})

////////////////
// SCAN AREAS //
////////////////



//  var lineFunction = d3.svg.line()
//                           .x(function(d) { return d.x; })
//                           .y(function(d) { return d.y; })
//                          .interpolate("linear");
                            
// function redrawScanAreas(){

//     scanAreas.forEach(function(scanArea){
        
//         // Remove First
//         svg.selectAll('#ScanArea-'+scanArea.id).remove();
        
//         svg.append("path")
//             .attr("d", lineFunction(scanArea.polyData()))
//             .attr("stroke-width", 2)
// 			.attr("opacity", .05)
//             .attr("fill", "blue")
//             .attr("stroke", "red")
//             .attr({id:'ScanArea-'+scanArea.id});

        
//     });
    
// }     

////////////////
// UNIT PATHS //
////////////////
                            
// function redrawUnitPaths(){
    
// //    console.log(unitPaths);

//     unitPaths.forEach(function(unitPath){
        
//         console.log(unitPath.polyData())
        
//         // Remove First
//         svg.selectAll('#UnitPath-'+unitPath.id).remove();
        
//         svg.append("path")
//             .attr("d", lineFunction(unitPath.polyData()))
//             .attr("stroke-width", 2)
// 			.attr("opacity", 1)
//             .attr("stroke", "blue")
//             .attr("fill", "none")
//             .attr({id:'UnitPath-'+unitPath.id});

        
//     });
    
// }                                                   