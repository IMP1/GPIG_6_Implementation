package drones.util;

import java.util.ArrayList;

/**
 * Object container for Building info.
 * @author Martin Higgs
 */
public class MapObject implements Comparable<MapObject> {
	// Bounding box
	public double minLat = Double.POSITIVE_INFINITY;
	public double minLng = Double.POSITIVE_INFINITY;
	public double maxLat = Double.NEGATIVE_INFINITY;
	public double maxLng = Double.NEGATIVE_INFINITY;
	
	// Points vectors
	public ArrayList<Double> lat = new ArrayList<Double>();
	public ArrayList<Double> lng = new ArrayList<Double>();
	
	/**
	 * Add a new point to building's polygon, adjusting bouding
	 * 		box as necessary.
	 * @param lat Latitude in degrees to add
	 * @param lng Longitude in degrees to add
	 */
	public void addPoint(double lat, double lng) {
		this.lat.add(lat);
		this.lng.add(lng);
		
		if (lat < minLat)
			minLat = lat;
		if (lng < minLng)
			minLng = lng;
		if (lat > maxLat)
			maxLat = lat;
		if (lng > maxLng)
			maxLng = lng;
	}

	/**
	 * Sort by minimum latitude first and longitude second
	 * @param that The MapObject to compare with
	 * @return this (-1 <, 0 =, 1 >) that
	 */
	@Override
	public int compareTo(MapObject that) {
		if (this.minLat < that.minLat)
			return -1;
		else if (this.minLat > that.minLat)
			return 1;
		else {
			if (this.minLng < that.minLng)
				return -1;
			if (this.minLng > that.minLng)
				return 1;
			else
				return 0;
		}
	}
}
