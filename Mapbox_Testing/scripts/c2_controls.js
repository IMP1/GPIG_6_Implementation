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
    
    var unit_icons = ['fa-battery-4', 'fa-info-circle', 'fa-clock-o', 'fa-sort-amount-asc'];
    
    for(var i = 0; i<unit_element_ids.length; i++){
    
        var unit_element_stats_stat       = document.createElement('div');
        unit_element_stats_stat.className = 'stat';
        unit_element_stats.appendChild(unit_element_stats_stat);
        
            var unit_element_stats_stat_icon       = document.createElement('div');
            unit_element_stats_stat_icon.className = 'icon fa '+unit_icons[i];
            unit_element_stats_stat_icon.id        = layerID+'-icon-'+unit_element_ids[i];
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
    searchUnitsEmpty.hidden = units.length > 0;
    
    units.forEach(function(unit) {
       
       // Name
        var element_name = document.getElementById(unit.id+'-'+'name');
            element_name.textContent = unit.name;
        
        // Battery
        updateBatteryLevelUI(unit);
        
        // State
        updateStatus(unit);
        
        // Time
        updateTimeLastSeen(unit);
        
        // Depth
        var element_depth = document.getElementById(unit.id+'-'+unit_element_ids[3]);
            element_depth.textContent = 'Depth : 20m';
       
       
   }, this);
   
}

function updateBatteryLevelUI(unit){
    
    var fa_battery_icon = 'fa-battery-full';
    if(unit.batteryLevel <= 75){
        fa_battery_icon = 'fa-battery-three-quarters'
    }
    if(unit.batteryLevel <= 50){
        fa_battery_icon = 'fa-battery-half'
    }
    if(unit.batteryLevel <= 25){
        fa_battery_icon = 'fa-battery-quarter'
    }
    if(unit.batteryLevel <= 10){
        fa_battery_icon = 'fa-battery-empty'
    }
    
    var element_battery_icon             = document.getElementById(unit.id+'-icon-'+unit_element_ids[0]);
        element_battery_icon.className   = 'icon fa '+fa_battery_icon;
    
    var element_battery_text             = document.getElementById(unit.id+'-'+unit_element_ids[0]);
        element_battery_text.textContent = Math.round(unit.batteryLevel) + '%';
         
     // Add Warning if Battery Level < Threshold
    
    if(unit.batteryLevel < unit.warningBatteryLevel){
        element_battery_icon.classList.add("warning");
        element_battery_text.classList.add("warning");
        addUnitBatteryFault(unit);
    }else{
        element_battery_icon.classList.remove("warning");
        element_battery_text.classList.remove("warning");
        removeUnitBatteryFault(unit);
    }
}

function updateStatus(unit){
    
    var element_state = document.getElementById(unit.id+'-'+unit_element_ids[1]);
        element_state.textContent = 'Status : '+unit.status.capitalizeFirstLetter();
    
    if(unit.status == 'fault'){
        
    }
    
}

function updateTimeLastSeen(unit){
    
    // Number Of Seconds to show a warning after
    var warningSecs = 30;
    
    var element_lastseen_icon = document.getElementById(unit.id+'-icon-'+unit_element_ids[2]);
    var element_lastseen      = document.getElementById(unit.id+'-'+unit_element_ids[2]);
        
    var timeUnit = 's';
    var curTime = Date.now();    
    var timeDifSecs = Math.round((curTime-unit.lastUpdated) / 1000);
    var usedTimeDif = timeDifSecs;
    
    // Conv to mins
    
    if(timeDifSecs > 60){
        var timeDifMins = Math.round(timeDifSecs/60);
        usedTimeDif = timeDifMins;
        timeUnit = 'm';
        // element_lastseen.style.color = 'red';
        
        // Conv to hours
        if(timeDifMins > 60){
            timeDifHours = Math.round(timeDifMins/60);
            usedTimeDif = timeDifHours;
            timeUnit = 'h';
        }  
    }   
    
    if(timeDifSecs > warningSecs){
        element_lastseen_icon.classList.add("warning");
        element_lastseen.classList.add("warning");
    }else{
        element_lastseen.classList.remove("warning");
        element_lastseen_icon.classList.remove("warning");
    }
        
    element_lastseen.textContent = 'Last seen '+usedTimeDif+timeUnit+' ago';
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

function redrawSearchAreasUI(){
    console.log(searchAreaArray.length)
    var searchAreasEmpty = document.getElementById('search-areas-empty');
        searchAreasEmpty.hidden = searchAreaArray.length > 0;
}

function redrawSearchAreaControls(searchArea){    
    
    redrawSearchAreasUI();
       
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
