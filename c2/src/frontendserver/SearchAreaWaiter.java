package frontendserver;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.UUID;

import broadcast.Broadcast;
import datastore.Datastore;
import network.MoveCommand;
import network.PathCommand;
import network.StatusData.DroneState;

public class SearchAreaWaiter {

	private SearchArea searchArea;
	private Datastore datastore;
	public HashMap<String, Double> etas;
	
	public SearchAreaWaiter(SearchArea searchArea, Datastore datastore) {
		this.searchArea = searchArea;
		this.datastore = datastore;
	}
	public String[] doWait() {
		datastore.setSearchArea(this.searchArea);
		String uniqueID = UUID.randomUUID().toString();
		PathCommand message = new PathCommand(this.searchArea.id, LocalDateTime.now(), this.searchArea.locLat, this.searchArea.locLong);
		Broadcast.broadcast(message.toString());
		LocalDateTime waitingFrom = LocalDateTime.now();
		LocalDateTime waitingUntil = waitingFrom.plusSeconds(20);
		System.out.println(waitingUntil);
		System.out.println(waitingUntil.isAfter(LocalDateTime.now()));
		while(datastore.getSearchArea().etas.size() < datastore.getNumberOfDrones() && waitingUntil.isAfter(LocalDateTime.now())); //ew
		
		//clone the hashmap. Too late now if you didn't reply
		System.out.println(datastore.getSearchArea().etas.size());
		if(datastore.getSearchArea().etas.size() < this.searchArea.numberRequested){
			String[] error = new String[1];
			error[0] = "Error : Insufficient Drones";
			return error;
		}
		this.etas = (HashMap<String, Double>) datastore.getSearchArea().etas.clone();
		String[] assigned = new String[this.searchArea.numberRequested];
		for (int i = 0; i < this.searchArea.numberRequested; i++){
			String droneID =  popClosestDrone();
			MoveCommand command = new MoveCommand(droneID,LocalDateTime.now(), this.searchArea.locLat, this.searchArea.locLong, this.searchArea.radius);
			System.out.println(command.toString());
			Broadcast.broadcast(command.toString());
			datastore.getDroneById(droneID).setStatus(DroneState.MOVING);
			datastore.getDroneById(droneID).setLock();
			assigned[i] = droneID;
		}
		return assigned;

	}	
	public String popClosestDrone(){
		String minKey = null;
		Double minValue = Double.MAX_VALUE;
		for (String key : etas.keySet()) {
	        Double value = etas.get(key);
	        System.err.println(datastore.getDroneById(key).getStatus());
	        System.err.println(datastore.getDroneById(key).isLocked());
	        if (value < minValue && !datastore.getDroneById(key).getStatus().equals(DroneState.MOVING) && !datastore.getDroneById(key).isLocked()) {
	            minValue = value;
	            minKey = key;
	        }
	    }
		if(minValue == Double.MAX_VALUE){
			for (String key : etas.keySet()) {
		        Double value = etas.get(key);
		        if (value < minValue ) {
		            minValue = value;
		            minKey = key;
		        }
		    }
		}
		etas.remove(minKey);
	    return minKey;
	}
}
