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
		while(datastore.getSearchArea() != null){
			
		}
		datastore.setSearchArea(this.searchArea);
		String uniqueID = UUID.randomUUID().toString();
		PathCommand message = new PathCommand(this.searchArea.id, LocalDateTime.now(), this.searchArea.locLat, this.searchArea.locLong);
		Broadcast.broadcast(message.toString());
		LocalDateTime waitingFrom = LocalDateTime.now();
		LocalDateTime waitingUntil = waitingFrom.plusSeconds(20);
		while(datastore.getSearchArea().etas.size() < datastore.getNumberOfDrones() && waitingUntil.isBefore(LocalDateTime.now())); //ew
		//TODO - Process the replies which *should* have found their way into the searchArea object at this point
		//then set the datastores search area to null to allow another thread to jump in.
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
