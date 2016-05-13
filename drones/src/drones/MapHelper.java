package drones;

import java.util.ArrayList;

import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.storage.index.QueryResult;
import com.graphhopper.util.EdgeIteratorState;

import drones.scanner.ScannerHandler.Scan;

/**
 * Abstract helper class containing static methods
 * for getting more complex information from the shared map.
 * @author Martin Higgs
 */
public abstract class MapHelper {
	
	private static ArrayList<Scan> scanDataList = new ArrayList<Scan>();
	
	/**
	 * Get the closest outdoors point to the specified location.
	 * If the location is outdoors already, then it is returned as is.
	 * @param lat Requested latitude in degrees
	 * @param lng Requested longitude in degrees
	 * @return Double array: [latitude, longitude]
	 */
	public static double[] getExternalPoint(double lat, double lng) {
		double[] result = {lat, lng};
		
		// Return the closest edge to the point
		LocationIndex index = Drone.map().getLocationIndex();
		QueryResult qr = index.findClosest(lat, lng, EdgeFilter.ALL_EDGES );
		EdgeIteratorState edge = qr.getClosestEdge();
		
		// TODO: Check if edge is a building
		
		// TODO: Check if point is inside the building nodes
		// http://stackoverflow.com/questions/8721406/how-to-determine-if-a-point-is-inside-a-2d-convex-polygon

		return result;
	}
	
	public static void addScan(Scan scanData) {
		synchronized (scanDataList) {
			scanDataList.add(scanData);
		}
	}
	
	//TODO: functions used by navigation and routing regarding querying the list of scan data.
	
}
