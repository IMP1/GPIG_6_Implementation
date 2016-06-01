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

var mapCenter   = [-1.0873, 53.9600];
var defaultZoom = 18;
var latOffset   = 0;
var longOffset  = 0;

//// Global Vars
var editingSearchArea    = false;
var refreshRate          = 100; //ms
var floodOutlineVisible  = false;
var showingTooltips      = false;
var envDataVisible       = false;
var showingLayerControls = false;


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

document.getElementById('btn-show-layer-controls').addEventListener('click', function(e) {            
    toggleLayerControls();
});

document.getElementById('btn-toggle-flood-outline').addEventListener('click', function(e) {            
    toggleFloodOutline();
});

document.getElementById('btn-toggle-env-data').addEventListener('click', function(e) {            
    toggleEnvData();
});

document.getElementById('btn-toggle-flood-info').addEventListener('click', function(e) {            
    toggleTooltips();
});

document.getElementById('btn-download-messages').addEventListener('click', function(e) {            
    downloadMessageLog();
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

var externalDataPoints = {
    "type": "FeatureCollection",
    "features": []
};



///////////////
// Map Setup //
///////////////
    
mapboxgl.accessToken = 'pk.eyJ1IjoiY29ybWFja2FsaSIsImEiOiJjaW55dTRtMmUwMHJxdmZtMjMyajI0ZHNtIn0.crRNON_GYqYZDSWraRTfBw';
// 54.002357, -1.156809 - top left
// 53.923080, -1.014330
var bounds = [
    [-1.014330, 54.002357], // Southwest coordinates
    [-1.156809, 53.923080]  // Northeast coordinates
];

var map = new mapboxgl.Map({
    container: 'map', 
    style: 'mapbox://styles/cormackali/cinyvygoz0004cbm9atdy33h5', 
    center: offsetCoordinates(mapCenter), 
    zoom: defaultZoom,
    maxBounds: bounds
})

map.on('load', function () {
    
    map.addSource("markers", {
        "type": "geojson",
        "data": markers
    });
    
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

    // External Data Points
    map.addSource("ExternalDataPoints", {
        "type": "geojson",
        "data": externalDataPoints
    });

    map.addLayer({
        "id":     'ExtDataPoints',
        "type":   "symbol",
        "source": "ExternalDataPoints",
        "layout": {
            "icon-image": "{marker-symbol}-15",
            "icon-allow-overlap": true
        }
    });   
    
    // Setup   
    updateMap();
    setupAPICalls(); 
    setInterval(refreshUI, refreshRate);     
    setupKeypresses();  
    enableSearchAreaDrawing();
    
    // Mapbox Elements
    map.addControl(new mapboxgl.Navigation({position: 'top-left'}));
    var toRemove = document.getElementsByClassName('mapboxgl-ctrl-bottom-right')[0];
    toRemove.parentNode.removeChild(toRemove); 

    // Set to opposite of desired (bit hacky)
    floodOutlineVisible  = true;
    showingTooltips      = false;
    envDataVisible       = true;
    showingLayerControls = true;
    toggleLayerControls();       
    toggleFloodOutline();        
    toggleEnvData();           
    toggleTooltips();

});

map.on("render", function() {
    redrawSearchAreas()
})

var last_zoomlevel = 0;

var zoomLevel_popups_high_detail = 16.5;
var zoomLevel_popups_med_detail  = 15.8;
var zoomLevel_popups_low_detail  = 13.8;

map.on('move', function(e) {
   showPopups();
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

          
        var bounds = new mapboxgl.LngLatBounds();

        markers.features.forEach(function(feature) {
            bounds.extend(feature.geometry.coordinates);
        });

        // Check Units aren't in same place
        if(bounds._ne.lat == bounds._sw.lat || bounds._ne.lng == bounds._sw.lng){

        }else{
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


function toggleTooltips(){
    showingTooltips = !showingTooltips;

    var btnElement = document.getElementById('btn-toggle-flood-info');
    if(showingTooltips){
        addNewPopups();
        btnElement.classList.add("on");
    }else{
        removeAllPopups();
        btnElement.classList.remove("on");
    }
}

function toggleLayerControls(){
    showingLayerControls = !showingLayerControls;

    var layerControls  = document.getElementById('layer-controls');
        layerControls.style.display  = showingLayerControls ? '' : 'none';

    var btnElement = document.getElementById('btn-show-layer-controls');
    if(showingLayerControls){
        btnElement.textContent = 'Hide Layer Controls';
    }else{
        btnElement.textContent = 'Show Layer Controls';
    }
}

function toggleEnvData(){
    envDataVisible = !envDataVisible;
    if(!envDataVisible){
        map.setLayoutProperty('york_flood_1', 'visibility', 'none');
        map.setLayoutProperty('york_flood_2', 'visibility', 'none');
        map.setLayoutProperty('york_flood_3', 'visibility', 'none');
        map.setLayoutProperty('york_flood_4', 'visibility', 'none');
    }else{
        map.setLayoutProperty('york_flood_1', 'visibility', 'visible');
        map.setLayoutProperty('york_flood_2', 'visibility', 'visible');
        map.setLayoutProperty('york_flood_3', 'visibility', 'visible');
        map.setLayoutProperty('york_flood_4', 'visibility', 'visible');
    }
    var btnElement = document.getElementById('btn-toggle-env-data');
    if(envDataVisible){
        btnElement.classList.add("on");
    }else{
        btnElement.classList.remove("on");
    }
}

function toggleFloodOutline(){
    floodOutlineVisible = !floodOutlineVisible;
    if(!floodOutlineVisible){
        map.setLayoutProperty('sensor-edge', 'visibility', 'none');
    }else{
        map.setLayoutProperty('sensor-edge', 'visibility', 'visible');
    }
    var btnElement = document.getElementById('btn-toggle-flood-outline');
    if(floodOutlineVisible){
        btnElement.classList.add("on");
    }else{
        btnElement.classList.remove("on");
    }
}

function flyToUnit(unit){
    map.flyTo({
        center: offsetCoordinates(unit.coordinates),
        zoom: defaultZoom,        
        speed: 2, 
        curve: 1,         
        easing: function (t) {
            return t;
        }
    });    
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
                ShowNewMessage('Search Area Creation Error', 'Search Area exceeds maximum radius of '+maxRadius+'m ('+Math.round(currentSearchArea.radius)+'m)', 'medium', '');
            }else if(currentSearchArea.radius <= minRadius){
                ShowNewMessage('Search Area Creation Error', 'Search Area under minimum radius of '+minRadius+'m ('+Math.round(currentSearchArea.radius)+'m)', 'medium', '');
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
var subsampleScans; // Use every nth scan for center

zoomLevel_popups_high_detail = 17.5;
zoomLevel_popups_med_detail  = 15.8;
zoomLevel_popups_low_detail  = 13.8;

var high_level_popups = [];
var med_level_popups  = [];
var low_level_popups  = [];

var levels            = [high_level_popups, med_level_popups];
var radius_levels     = [40, 100];

function addNewPopups(){
    

    // Only add if not loading for first time.
    var showMessage = scanInfoArray.length - lastDataScanned != scanInfoArray.length;

    // Go from last scan data
    for(var i = lastDataScanned; i < scanInfoArray.length; i+= 30){
        addNewPopupIfRequired(i, showMessage);
    }
    lastDataScanned = scanInfoArray.length;
   
}

function removeAllPopups(){
    infoPopups.forEach(function(tooltip) {
        tooltip.remove();
    }, this);
    infoPopups = [];
}

function showPopups(){

    removeAllPopups()
    var zoom = map.getZoom();

    if(zoom >= zoomLevel_popups_high_detail){
        displayPopups(high_level_popups);
    }else if(zoom >= zoomLevel_popups_med_detail){
        displayPopups(med_level_popups);
    }else if(zoom >= zoomLevel_popups_low_detail){
        displayPopups(low_level_popups);
    }else{
        removeAllPopups();
    }
}

function displayPopups(popupArray){
    popupArray.forEach(function(popup){

        var tooltip = new mapboxgl.Popup({closeOnClick: false, closeButton:false, anchor:'bottom'})
            .setLngLat(popup.coordinates)
            .setDOMContent(popup.div)
            .addTo(map);
        infoPopups.push(tooltip);

    });
}

function addNewPopupIfRequired(i, showMessage){

    // Popups are added as objects to high,med and low detail arrays. 

    var scan           = scanInfoArray[i];
    var centerLngLat   = new mapboxgl.LngLat(scan.center[1], scan.center[0]);   

    for (var i = 0; i<levels.length; i++) {       

        var popup_array = levels[i];
        var radius      = radius_levels[i];

        // Check if there's a tooltip within n metres 
        var within_radius  = false;        
        popup_array.forEach(function(tooltip) {

            var tooltipLngLat = new mapboxgl.LngLat(tooltip.coordinates[0], tooltip.coordinates[1]);             
            var dist          = unprojectedDistance(tooltipLngLat, centerLngLat);
            if(dist < radius) within_radius = true;           
            
        }, this);
        
        if(!within_radius){
        
            var depth_string    = roundToDecimalPlaces(scan.depth, 2)   +'m';
            var flowrate_string = roundToDecimalPlaces(scan.flowrate, 2)+'m/s';
            
            var depth_severity  = getDepthSeverity(scan.depth);
            var flow_severity   = getFlowSeverity(scan.flowrate);

            var depth_class     = 'severity-'+depth_severity;
            var flow_class      = 'severity-'+flow_severity;
            
            var warnings        = getWarnings(scan.depth, scan.flowrate);
            
            var html_string     = '';

            warnings.forEach(function(warning) {
                html_string    += '<div class=\'warning\'>  <div class=\'icon\'><img src=\'images\\icons\\warning_'+warning.icon+'.png\'></img></div>     <div class=\'warning_text\'><div class=\'inner\'>'+warning.shortdesc+'</div></div>   </div>'
            }, this);

            html_string        += '<div class=\'left\'>   <div class=\'img icon fa fa-sort-amount-asc '+depth_class+'\'></div> <div class=\'text '+depth_class+'\'>'+depth_string+'</div>   </div>';
            html_string        += '<div class=\'right\'>  <div class=\'img icon fa fa-tachometer '+flow_class+'\'>   </div><div class=\'text '+flow_class+'\'>'+flowrate_string+'</div> </div>';

            var div = document.createElement('div');
                div.innerHTML = html_string;

            if(warnings.length > 0){
                div.addEventListener('click', function(e) {            
                    showWarningDetails(warnings);
                });
            }

            var coordinates = [scan.center[1], scan.center[0]];
            var warningTooltip = new WarningTooltip(div, coordinates);
            popup_array.push(warningTooltip);

            // Show Alerts for close zoom popups
            if(popup_array == high_level_popups && warnings.length > 0){ 

                var messageString = '';

                warnings.forEach(function(warning) {
                    messageString += ' '+warning.longdesc;
                }, this);

                var func = function(){ return flyToCoordinates(coordinates) }

                if(showMessage) ShowNewMessage('New Flood Information', messageString, 'medium', func);
            } 
        }
    }
}

function flyToCoordinates(coordinates){
     map.flyTo({
        center: offsetCoordinates(coordinates),
        zoom: defaultZoom,        
        speed: 2, 
        curve: 1,         
        easing: function (t) {
            return t;
        }
    });    
}

function getWarnings(depth, flow){
    var warnings = [];
    
    // Depth
    if(depth >= 6){
        warnings.push({'icon':'boat', 'shortdesc':'Helicopter Required', 'longdesc':'Depth is > 4m. Must be accessed via air.'});
    }else if(depth >= 2){
        warnings.push({'icon':'boat', 'shortdesc':'Boat Required', 'longdesc':'Depth is > 2m. Boat required to navigate this area.'});
    }else if(depth >= 1){
        warnings.push({'icon':'boat', 'shortdesc':'Waders Required', 'longdesc':'Depth is > 1m. Waders required for safe traversal.'});
    }else if(depth >= .3){
        warnings.push({'icon':'boat', 'shortdesc':'Life-Jackets', 'longdesc':'Depth is > .3m. Life Jacket is required for safety.'});
    }

    // Flow
    
    if(flow >= 1.5 && depth > 4){
        warnings.push({'icon':'boat', 'shortdesc':'Deep Water', 'longdesc':'This area is deep and has fast moving water and requires caution.'});
    }else if(flow >= 3 && depth > .5){
        warnings.push({'icon':'boat', 'shortdesc':'Unstable Surfaces', 'longdesc':'The fast moving water here may make surfaces slick and unstable and requires extra caution.'});
    }else if(flow >= 4){
        warnings.push({'icon':'boat', 'shortdesc':'Rapid Water', 'longdesc':'This area has fast moving water and requires caution.'});
    }

    // Combo
    
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

function showWarningDetails(warnings){

    var prev_warning = document.getElementById('warning-overlay');
    if(prev_warning){
        prev_warning.parentNode.removeChild(prev_warning);
    }

    var map_overlay = document.getElementById('map-overlay');

    var warning_overlay           = document.createElement('div');
        warning_overlay.id = 'warning-overlay'
        warning_overlay.className = 'map-overlay-panel';
    map_overlay.appendChild(warning_overlay);

        var warning_header       = document.createElement('h2');
            warning_header.textContent = 'Warning Details'  
        warning_overlay.appendChild(warning_header);

        warnings.forEach(function(warning) {
            
            var warning_section       = document.createElement('div');
                warning_section.className = 'warning-info';
            warning_overlay.appendChild(warning_section);

                var warning_section_icon       = document.createElement('div');
                    warning_section_icon.className = 'icon';  
                warning_section.appendChild(warning_section_icon);

                    var warning_section_img       = document.createElement('img');
                        warning_section_img.src   = 'images\\icons\\warning_'+warning.icon+'.png';
                    warning_section_icon.appendChild(warning_section_img);

                var warning_section_text       = document.createElement('div');
                    warning_section_text.className = 'text-content';  
                warning_section.appendChild(warning_section_text);

                    var warning_section_header       = document.createElement('h2');
                        warning_section_header.textContent = warning.shortdesc;
                    warning_section_text.appendChild(warning_section_header);

                    var warning_section_body      = document.createElement('p');
                        warning_section_body.textContent = warning.longdesc;
                    warning_section_text.appendChild(warning_section_body);

        }, this);

        var warning_close       = document.createElement('div');
            warning_close.className = 'close';
            warning_close.textContent = 'Click/Tap to Close'
        warning_overlay.appendChild(warning_close);

    warning_overlay.addEventListener('click', function(e) {            
        warning_overlay.parentNode.removeChild(warning_overlay);
    });

}







////////////
// Faults //
////////////

var faultDisplayed = false;

function showUnitFault(){
    
    if(!faultDisplayed){        
        ShowNewMessage('Major Drone Fault', 'There is an unexpected fault with Drone. Please attend unit.', 'high', '');    
        faultDisplayed = true;    
    }
}

function addUnitBatteryFault(unit){    
    if(!unit.batteryFaultDisplayed){     
        console.log(unit);
        ShowNewMessage('Drone Battery Warning', 'Drone Battery at '+Math.round(unit.batteryLevel)+'%. It has been automatically recalled to C2.', 'medium', '');    
        unit.batteryFaultDisplayed = true;    
    }
}

function removeUnitBatteryFault(unit){
    unit.batteryFaultDisplayed = false;  
}

function showUnitUnseenFault(unit, timeUnseenSeconds){
    if(!unit.unseenFaultDisplayed){ 
        //Show msg + add marker
        unit.unseenFaultDisplayed = true;
        var func = function(){ return flyToUnit(unit) }
        ShowNewMessage(unit.name+' Unresponsive', unit.name+' has not relayed information in '+timeUnseenSeconds+' seconds. Please investigate last known location of search unit. Click here to see last broadcast location.', 'high', func);    

        //Tooltip
        var tooltip = new mapboxgl.Popup({closeOnClick: false, closeButton:false})
            .setLngLat(unit.coordinates)
            .setHTML('Major Unit Fault')
            .addTo(map);

        unit.unseenFaultTooltip = tooltip;
    }
}

function removeUnitUnseenFault(unit){
    if(unit.unseenFaultDisplayed){
        unit.unseenFaultDisplayed = false; 
        unit.unseenFaultTooltip.remove(); 
    }
    
}