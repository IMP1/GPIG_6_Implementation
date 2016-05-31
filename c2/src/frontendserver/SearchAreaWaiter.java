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
	private static Object lock = new Object();
	
	public SearchAreaWaiter(SearchArea searchArea, Datastore datastore) {
		this.searchArea = searchArea;
		this.datastore = datastore;
	}
	public String[] doWait() {
		synchronized(lock){
			System.out.println("THREAD:"+Thread.currentThread().getId());
			datastore.setSearchArea(this.searchArea);
			String uniqueID = UUID.randomUUID().toString();
			PathCommand message = new PathCommand(this.searchArea.id, LocalDateTime.now(), this.searchArea.locLat, this.searchArea.locLong);
			Broadcast.broadcast(message.toString());
			LocalDateTime waitingFrom = LocalDateTime.now();
			LocalDateTime waitingUntil = waitingFrom.plusSeconds(20);
			while(datastore.getSearchArea().etas.size() < datastore.getNumberOfDrones() && waitingUntil.isAfter(LocalDateTime.now())){
				
			}; //ew
			System.err.println("Done Waiting after "+waitingFrom.compareTo(LocalDateTime.now())+datastore.getSearchArea().etas.size());
			
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
				datastore.getDroneById(droneID).setStatus(DroneState.MOVING);
				datastore.getDroneById(droneID).setLock();
				System.err.println(command.toString());
				Broadcast.broadcast(command.toString());
				
				
				assigned[i] = droneID;
			}
			return assigned;
		}
	}	
	public String popClosestDrone(){
		String minKey = null;
		Double minValue = Double.MAX_VALUE;
		for (String key : etas.keySet()) {
	        Double value = etas.get(key);
	        System.err.println("Condering Drone "+key);
	        System.err.println("Status"+datastore.getDroneById(key).getStatus());
	        System.err.println("Locked?"+datastore.getDroneById(key).isLocked());
	        System.err.println("eta "+value);
	        if (value <= minValue && !datastore.getDroneById(key).getStatus().equals(DroneState.MOVING) && !datastore.getDroneById(key).isLocked()) {
	        	minValue = value;
	            minKey = key;
	        }
	    }
		if(minValue == Double.MAX_VALUE){
			for (String key : etas.keySet()) {
		        Double value = etas.get(key);
		        if (value <= minValue ) {
		            minValue = value;
		            minKey = key;
		        }
		    }
		}
		etas.remove(minKey);
		System.out.println(minKey);
	    return minKey;
	}
}
