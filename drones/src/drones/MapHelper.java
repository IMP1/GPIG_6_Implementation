package drones;


import java.util.ArrayList;

import java.util.Locale;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.PathWrapper;

import network.ScanData;

/**
 * Abstract helper class containing static methods
 * for getting more complex information from the shared map.
 * @author Martin Higgs
 */
public abstract class MapHelper {
	
	private static ArrayList<ScanData> scanDataList = new ArrayList<ScanData>();
	
	/**
	 * Get the closest outdoors point to the specified location.
	 * If the location is outdoors already, then it is returned as is.
	 * @param lat Requested latitude in degrees
	 * @param lng Requested longitude in degrees
	 * @return Double array: [latitude, longitude]
	 */
	public static double[] getExternalPoint(double lat, double lng) {
		double[] result = {lat, lng};
		
		// TODO: Check if point is inside the building nodes
		// http://stackoverflow.com/questions/8721406/how-to-determine-if-a-point-is-inside-a-2d-convex-polygon

		return result;
	}

	public static void addScan(ScanData scanData) {
		synchronized (scanDataList) {
			scanDataList.add(scanData);
		}
	}
	
	//TODO: functions used by navigation and routing regarding querying the list of scan data.

	/**
	 * Check if the direct path between 2 points is blocked.
	 * @param srcLat Starting latitude in degrees
	 * @param srcLng Starting longitude in degrees
	 * @param dstLat Destination latitude in degrees
	 * @param dstLng Destination longitude in degrees
	 * @return True if the path between the two points is blocked and
	 * 		requires routing. False otherwise.
	 */
	public static boolean pathBlocked(double srcLat, double srcLng, double dstLat, double dstLng) {
		// TODO: Check vector intersection
		return false;
	}
	
	/**
	 * Calculate a route from the start location to the destination.
	 * @param srcLat Starting latitude in degrees
	 * @param srcLng Starting longitude in degrees
	 * @param dstLat Destination latitude in degrees
	 * @param dstLng Destination longitude in degrees
	 * @return A pathwrapper including the route. Be warned, this does not include
	 * 		the start or destination locations however!
	 */
	public static PathWrapper route(double srcLat, double srcLng, double dstLat, double dstLng) {
		// Construct and make routing request
		GHRequest req = new GHRequest(srcLat, srcLng, dstLat, dstLng).
		    setWeighting("fastest").
		    setVehicle("foot").
		    setLocale(Locale.UK);
		GHResponse rsp = Drone.map().route(req);

		// Log errors and return null result if so
		if(rsp.hasErrors()) {
			for(Throwable e : rsp.getErrors())
				System.err.println(e.getMessage());
			return null;
		}

		// Return best route
		return rsp.getBest();
	}
}
