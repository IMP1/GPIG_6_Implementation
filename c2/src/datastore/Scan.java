package datastore;

public class Scan {
	private String locLat;
	private String locLong;
	private String depth;
	private String flowRate;
	private String[] distanceReadings;
	
	public Scan(String locLat, String locLong, String depth, String flowRate, String[] distanceReadings) {
		this.locLat = locLat;
		this.locLong = locLong;
		this.depth = depth;
		this.flowRate = flowRate;
		this.distanceReadings = distanceReadings;
	}

}
