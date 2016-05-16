package frontendserver;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.UUID;

import broadcast.Broadcast;
import datastore.Datastore;
import network.PathCommand;

public class SearchAreaWaiter implements Runnable {

	private SearchArea searchArea;
	private Datastore datastore;
	
	public SearchAreaWaiter(SearchArea searchArea, Datastore datastore) {
		this.searchArea = searchArea;
		this.datastore = datastore;
	}

	@Override
	public void run() {
		while(datastore.getSearchArea() != null){
			
		}
		datastore.setSearchArea(this.searchArea);
		String uniqueID = UUID.randomUUID().toString();
		PathCommand message = new PathCommand(this.searchArea.id, LocalDateTime.now(), this.searchArea.locLat, this.searchArea.locLong);
		Broadcast.broadcast(message.toString());
		try {
		    Thread.sleep(30000);
		} catch(InterruptedException ex) {
		    Thread.currentThread().interrupt();
		}
		//TODO - Process the replies which *should* have found their way into the searchArea object at this point
		//then set the datastores search area to null to allow another thread to jump in.
		Integer[] selectedDrones = new Integer[this.searchArea.numberRequested];
		HashMap theReplies = (HashMap) this.searchArea.etas.clone();


}
