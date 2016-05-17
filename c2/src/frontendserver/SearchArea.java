package frontendserver;

import java.util.HashMap;

public class SearchArea {
	
	public Integer numberRequested;
	public String id;
	public double locLat;
	public double locLong;
	public double radius;
	public HashMap<String, Double> etas; //String: drone ID. Long: ETA.
	
	public SearchArea(String id, double locLat, double locLong, Integer numberRequested, double radius) {
		this.numberRequested = numberRequested;
		this.id = id;
		this.locLat = locLat;
		this.locLong = locLong;
		this.radius = radius;
		this.etas = new HashMap<String, Double>();
	}
	
	public void addEta(String droneID, double eta){
		etas.put(droneID, eta);
	}

}
