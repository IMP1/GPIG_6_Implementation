package frontendserver;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.UUID;

import broadcast.Broadcast;
import datastore.Datastore;
import network.MoveCommand;
import network.PathCommand;

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
		if(datastore.getSearchArea().etas.size() < this.searchArea.numberRequested){
			throw new RuntimeException("Insufficient replies");
		}
		this.etas = (HashMap<String, Double>) datastore.getSearchArea().etas.clone();
		String[] assigned = new String[this.searchArea.numberRequested];
		for (int i = 0; i < this.searchArea.numberRequested-1; i++){
			String droneID =  popClosestDrone();
			MoveCommand command = new MoveCommand(droneID,LocalDateTime.now(), this.searchArea.locLat, this.searchArea.locLong, this.searchArea.radius);
			Broadcast.broadcast(command.toString());
			assigned[i] = droneID;
		}
		return assigned;

	}	
	public String popClosestDrone(){
		String minKey = null;
		Double minValue = Double.MAX_VALUE;
		for (String key : etas.keySet()) {
	        Double value = etas.get(key);
	        if (value < minValue) {
	            minValue = value;
	            minKey = key;
	        }
	    }
		
	    return minKey;
	}
}
