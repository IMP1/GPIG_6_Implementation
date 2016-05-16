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
document.getElementById('btn-clear-all').addEventListener('click', function(e) {            
    deleteAllSearchAreas();
});

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

//// HTML Elements
var filterGroup = document.getElementById('filter-group');
var container = map.getCanvasContainer();
var svg = d3.select(container).append("svg");
var searchAreas = document.getElementById('search-areas');
var map_overlays = document.getElementById('map_overlays');

// Add zoom controls
map.addControl(new mapboxgl.Navigation());

//// Units
var Unit = function(name, symbol, coordinates){
    this.type = "Feature";
    this.properties = {
        "name" : name,
        "marker-symbol": symbol
    }
    this.geometry = {
        "type": "Point",
        "coordinates": coordinates
    }
}

function addNewUnit(name, symbol, coordinates){
    
    // Adds a new Unit (drone/c2) with name, icon and coordinates
    
    var unit = new Unit(name, symbol, coordinates);
    markers.features.push(unit);
    
    var layerID = name; 

    // Add a layer for this symbol type if it hasn't been added already.
    if (!map.getLayer(layerID)) {
       
        map.addLayer({
            "id": layerID,
            "type": "symbol",
            "source": "markers",
            "layout": {
                "icon-image": symbol + "-15",
                "icon-allow-overlap": true
            },
            "filter": ["==", "marker-symbol", symbol]
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
            unit_element_icon.className = 'unit-icon maki  maki-'+symbol;
            unit_element_info.appendChild(unit_element_icon);
            
            var unit_element_text = document.createElement('div');
            unit_element_text.className = 'unit-name';
            unit_element_text.textContent = name;
            unit_element_info.appendChild(unit_element_text);
            
        var unit_element_stats = document.createElement('div');
        unit_element_stats.className = 'stats';        
        unit_element.appendChild(unit_element_stats);
        
        var unit_icons = ['fa-battery-4', 'fa-feed', 'fa-cloud', 'fa-anchor'];
        var unit_text  = ['Battery : 100%', 'State : Scanning', 'Connection : Strong', 'Depth : 10m'];
        
        for(var i = 0; i<4; i++){
        
            var unit_element_stats_stat = document.createElement('div');
            unit_element_stats_stat.className = 'stat';
            unit_element_stats.appendChild(unit_element_stats_stat);
            
                var unit_element_stats_stat_icon = document.createElement('div');
                unit_element_stats_stat_icon.className = 'icon fa '+unit_icons[i];
                unit_element_stats_stat.appendChild(unit_element_stats_stat_icon);
                
                var unit_element_stats_stat_text = document.createElement('div');
                unit_element_stats_stat_text.className = 'text';
                unit_element_stats_stat_text.textContent = unit_text[i];
                unit_element_stats_stat.appendChild(unit_element_stats_stat_text);
                
        }
        
        // On click go to unit coordinates
        unit_element.addEventListener('click', function(e) {            
            map.flyTo({
                center: offsetCoordinates(unit.geometry.coordinates),
                zoom: defaultZoom,        
                speed: 0.6, 
                curve: 1,         
                easing: function (t) {
                    return t;
                }
            });            
        });
    }
    
}

function setMarkerPosition(lat, long, marker){    
    marker.geometry.coordinates = [lat, long];
}

var markers = {
    "type": "FeatureCollection",
    "features": []
};

map.on('load', function () {
    
    map.addSource("markers", {
        "type": "geojson",
        "data": markers
    });
    
    // Add C2 and 3 Drones. Will be handled by C2 backend
    addNewUnit('c2', 'harbor', [-1.0873, 53.9600]);
    addNewUnit('drone1', 'marker', [-1.083877, 53.9619]);
    addNewUnit('drone2', 'marker', [-1.08525, 53.957266]);
    addNewUnit('drone3', 'marker', [-1.0925, 53.95989]);
   
    updateMap();
  

});

// NEW CIRCLES

function project(d) {
  return map.project(getLL(d));
}

function getLL(d) {
  latlong = new mapboxgl.LngLat(d.lng, d.lat)
  return latlong;
}

function unproject(a) {
    return map.unproject({x: a[0], y: a[1]});
}

var currentSearchArea;
var searchAreaArray = [];

// SearchArea Class
var searchAreaID = 0;
function SearchArea(){
    this.id = searchAreaID++;
    this.center = [];
    this.outer  = [];
    this.assignedDrones = 1;
}

function distance(ll0, ll1) {
    var p0 = project(ll0)
    var p1 = project(ll1)
    var dist = Math.sqrt((p1.x - p0.x)*(p1.x - p0.x) + (p1.y - p0.y)*(p1.y-p0.y))
    return dist;
}

var mouseDownCoords;

function arraysEqual(a, b) {
  if (a === b) return true;
  if (a == null || b == null) return false;
  if (a.length != b.length) return false;

  // If you don't care about the order of the elements inside
  // the array, you should sort both arrays here.

  for (var i = 0; i < a.length; ++i) {
    if (a[i] !== b[i]) return false;
  }
  return true;
}

svg.on("mousedown", function() {
    mouseDownCoords = d3.mouse(this);
})

svg.on("mouseup", function() {
    
    // New Search Area    
    if(!searchAreaCreationEnabled) return;
    
    var p  = d3.mouse(this);
    
    console.log(p, mouseDownCoords)
    
    if(!arraysEqual(p, mouseDownCoords)) return;
    
    var ll = unproject([p[0],p[1]])
    
    if(!currentSearchArea){        
        currentSearchArea = new SearchArea();
        currentSearchArea.center = ll;   
        searchAreaArray.push(currentSearchArea); 
        editingSearchArea = true;
        updateMap();
    }else{
        currentSearchArea.outer = ll;        
        addNewSearchArea(currentSearchArea); 
        currentSearchArea = null;
        editingSearchArea = false;
        updateMap();
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
        
        var unit_id    = ['assigned', '', '', '']
        var unit_icons = ['fa-battery-4', 'fa-feed', 'fa-cloud', 'fa-anchor'];
        var unit_text  = ['Assigned '+searchArea.assignedDrones+ ' Drones', 'Coordinates : [100, 200]', 'Radius : 250m'];
        
        for(var i = 0; i<3; i++){
        
            var search_area_stats = document.createElement('div');
            search_area_stats.className = 'info';
            search_area.appendChild(search_area_stats);
                
                var unit_element_stats_stat_text = document.createElement('div');
                unit_element_stats_stat_text.className = 'text';
                unit_element_stats_stat_text.id = 'control-searcharea-'+searchArea.id+'-'+unit_id[i];
                unit_element_stats_stat_text.textContent = unit_text[i];
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

}

function changeDroneAssignmentForSearchArea(inc, searchArea){
    searchArea.assignedDrones += inc;
    var htmlString = 'control-searcharea-'+searchArea.id+'-assigned';   
    document.getElementById(htmlString).textContent = 'Assigned '+searchArea.assignedDrones+ ' Drones';
}

var removeByAttr = function(arr, attr, value){
    var i = arr.length;
    while(i--){
       if( arr[i] 
           && arr[i].hasOwnProperty(attr) 
           && (arguments.length > 2 && arr[i][attr] === value ) ){ 
           arr.splice(i,1);
       }
    }
    return arr;
}

function deleteAllSearchAreas(){
    
    console.log('//TODO : Prompt Are you sure message.');
    
    searchAreaArray.forEach(function(searchArea){
        deleteSearchAreaView(searchArea);
    });
    searchAreaArray = [];
}

function deleteSearchArea(searchArea){    
    console.log('Removing Search Area '+searchArea.id);        
    deleteSearchAreaView(searchArea);    
    removeByAttr(searchAreaArray, 'id', searchArea.id);
}

function deleteSearchAreaView(searchArea){    
    svg.selectAll('#SearchArea-'+searchArea.id).remove();
    svg.selectAll('#SearchArea-Line-'+searchArea.id).remove();
    svg.selectAll('#SearchArea-Marker-'+searchArea.id).remove();    
    document.getElementById('control-searcharea-'+searchArea.id).remove();
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

function redrawSearchAreas(){

    searchAreaArray.forEach(function(searchArea){
        
        if(!searchArea.center){
           return; 
        } 
        
        var dist = distance(searchArea.center, searchArea.outer);
        
        // Remove First
        svg.selectAll('#SearchArea-'+searchArea.id).remove();
        svg.selectAll('#SearchArea-Line-'+searchArea.id).remove();
        svg.selectAll('#SearchArea-Marker-'+searchArea.id).remove();
        
        // Redraw
        var circleLasso = svg.selectAll('#SearchArea-'+searchArea.id)
            .data([dist])
            .enter()
            .append("circle")     
            
        .attr({
          cx: project(searchArea.center).x,
          cy: project(searchArea.center).y,
          r: dist,
          id:'SearchArea-'+searchArea.id
        })
        
        .style({
          stroke: "#414852",
          fill: "#010",
          "fill-opacity": 0.1
        })       
        
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
        
        var markers = svg.selectAll('SearchArea-Marker-'+searchArea.id)
            .data([searchArea.center, searchArea.outer])
            .enter()
            .append("circle")
            
        .attr({
          cx: function(d) { return project(d).x},
          cy: function(d) { return project(d).y},
          r: 8,
          stroke: "#414852",
          fill: "#414852",
          "fill-opacity":0.9,
          id:'SearchArea-Marker-'+searchArea.id
        })
        
        .style({
          "cursor": "move"
        })
        
        .call(drag)
        
        // Redraw
        dispatch.redrawSearchAreas();

        
    });
    
}

map.on("render", function() {
    redrawSearchAreas()
})


// CIRCLES



// var active = true;

// var circleControl = new circleSelector(svg)
//     .projection(project)
//     .inverseProjection(function(a) {
//         return map.unproject({x: a[0], y: a[1]});
//     })
//     .activate(active);

// function project(d) {
//   return map.project(getLL(d));
// }

// function getLL(d) {
//   return new mapboxgl.LngLat(+d.lng, +d.lat)
// }

// d3.select("#circle").on("click", function() {
//   active = !active;
//   circleControl.activate(active)
//   if(active) {
//     map.dragPan.disable();
//   } else {
//     map.dragPan.enable();
//   }
//   d3.select(this).classed("active", active)
// })

// function render() {
//     circleControl.update(svg)
// }

// // re-render our visualization whenever the view changes
// map.on("viewreset", function() {
// render()
// })
// map.on("move", function() {
// render()
// })