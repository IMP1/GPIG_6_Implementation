package drones.util;

public class Position {
	// Earth's radius in meters for distance calculation
		// For reference, 1m is roughly 0.0000085 (8.5e^-6) degrees
		private static final int EARTH_RADIUS = 6731000;
		/**
		 * Convert meters to degrees
		 * @param m Meters distance
		 * @return Degrees (ignoring curvature of earth
		 */
		public static double mToD(double m) {
			double deg = Math.toDegrees(m / EARTH_RADIUS);
			return deg;
		}
		
		/**
		 * Convert degrees to metres, with a compensation factor to keep accuracy
		 * @param d Degrees distance
		 * @return Metres (ignoring curvature of earth
		 */
		public static int dtoM(double d) {
			int met = (int) (Math.toRadians(d) * EARTH_RADIUS * 100);
			return met;
		}
		
		/**
		 * Private helper for naively calculating distance in metres, ignores curvature of earth.
		 * @param latDiff Latitude difference in degrees
		 * @param longDiff Longitude difference in degrees
		 * @return Absolute difference in metres
		 */
		public static double latLongDiffInMeters(double latDiff, double longDiff) {
			double latDiffM = Math.toRadians(latDiff) * EARTH_RADIUS;
			double longDiffM = Math.toRadians(longDiff) * EARTH_RADIUS;
			double dist = Math.sqrt(Math.pow(latDiffM, 2) + Math.pow(longDiffM, 2));
			return dist;
		}
		
}
