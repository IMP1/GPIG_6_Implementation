/////////////
// Unit UI //
/////////////

// Needed for updates
var unit_element_ids   = ['battery', 'state', 'lastseen', 'depth'];

function addNewUnitControls(unit){
    
    var layerID = unit.id; 

    var unit_element       = document.createElement('div');
    unit_element.id        = layerID;
    unit_element.className = 'unit';
    unit_element.setAttribute('for', layerID);
    filterGroup.appendChild(unit_element);
    
    var unit_element_info       = document.createElement('div');
    unit_element_info.className = 'info';        
    unit_element.appendChild(unit_element_info);

        var unit_element_icon       = document.createElement('i');
        unit_element_icon.className = 'unit-icon maki  maki-'+unit.symbol;
        unit_element_info.appendChild(unit_element_icon);
        
        var unit_element_text         = document.createElement('div');
        unit_element_text.className   = 'unit-name';
        unit_element_text.textContent = name;
        unit_element_text.id          = layerID+'-'+'name';
        unit_element_info.appendChild(unit_element_text);
        
    var unit_element_stats       = document.createElement('div');
    unit_element_stats.className = 'stats';        
    unit_element.appendChild(unit_element_stats);
    
    var unit_icons = ['fa-battery-4', 'fa-feed', 'fa-cloud', 'fa-anchor'];
    
    for(var i = 0; i<unit_element_ids.length; i++){
    
        var unit_element_stats_stat       = document.createElement('div');
        unit_element_stats_stat.className = 'stat';
        unit_element_stats.appendChild(unit_element_stats_stat);
        
            var unit_element_stats_stat_icon       = document.createElement('div');
            unit_element_stats_stat_icon.className = 'icon fa '+unit_icons[i];
            unit_element_stats_stat.appendChild(unit_element_stats_stat_icon);
            
            var unit_element_stats_stat_text       = document.createElement('div');
            unit_element_stats_stat_text.className = 'text';
            unit_element_stats_stat_text.id        = layerID+'-'+unit_element_ids[i];
            unit_element_stats_stat.appendChild(unit_element_stats_stat_text);
            
    }
    
    // On click go to unit coordinates
    unit_element.addEventListener('click', function(e) {            
        map.flyTo({
            center: offsetCoordinates(unit.coordinates),
            zoom: defaultZoom,        
            speed: 2, 
            curve: 1,         
            easing: function (t) {
                return t;
            }
        });            
    });
    
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










//////////////////
// Search Areas //
//////////////////

function addNewSearchAreaControls(searchArea){
    
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
                
    redrawSearchAreas();

}

function redrawSearchAreaControls(searchArea){
    
    var searchAreasEmpty = document.getElementById('search-areas-empty');
    if(searchAreaArray.length > 0){
        searchAreasEmpty.hidden = true;
    }else{
        searchAreasEmpty.hidden = false;
    }
       
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
    
}

function deleteSearchAreaControls(searchArea){    
    document.getElementById('control-searcharea-'+searchArea.id).remove();    
}
