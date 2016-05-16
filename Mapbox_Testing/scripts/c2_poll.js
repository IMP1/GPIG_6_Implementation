window.setInterval(pollFunc, 3000);

function pollFunc(){
	//Drone info
	var xmlHttpDrone = new XMLHttpRequest();
    xmlHttpDrone.open( "GET", "http://localhost:8081/GetDroneInfo", false ); // false for synchronous request
    xmlHttpDrone.send( null );
    var drones = JSON.parse(xmlHttpDrone.responseText);
    console.log(drones);
    // for(var key in drones){
    // 	console.log(key);
    // }
    // console.log(drones);
    //Scan info
    known_scans = ["12016-05-12T17:31:13.269","2","3"];
    var known_scans_string = known_scans.join(",")
	var xmlHttpScan = new XMLHttpRequest();
    xmlHttpScan.open( "GET", "http://localhost:8081/GetScanInfo?known_scans="+known_scans_string, false ); // false for synchronous request
    xmlHttpScan.send( null );
    var scans = JSON.parse(xmlHttpScan.responseText);
    // console.log(scans);
}