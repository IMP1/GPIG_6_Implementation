package network;

import java.time.LocalDateTime;

import network.StatusData.DroneState;

@SuppressWarnings("unused")
public class Test {

	public static void main(String[] args) {
		testMoveCommand();
		testStatusMessage();
		testScanMessage();
		testPathMessage();
	}
	
	private static void testMoveCommand() {
		System.out.println("\tTEST: Move Command");
		String id = "1200afja8ahfafhf";
		LocalDateTime time = LocalDateTime.now();
		double lat = 0.001252536;
		double lon = 0.001252536;
		double rad = 4.001252536;
		MoveCommand c = new MoveCommand(id, time, lat, lon, rad);
		
		String message = c.toString();
		System.out.println(message);
		
		MoveCommand c2 = new MoveCommand(message);
		System.out.println(c2.id);
		System.out.println(c2.timestamp);
		System.out.println(c2.latitude);
		System.out.println(c2.longitude);
		System.out.println(c2.radius);
		
		System.out.flush();
		if (!id.equals(c2.id)) System.err.println("FALURE: different ids.");
		if (!time.equals(c2.timestamp)) System.err.println("FALURE: different timestamps.");
		if (lat != c2.latitude) System.err.println("FALURE: different latitude.");
		if (lon != c2.longitude) System.err.println("FALURE: different longitude.");
		if (rad != c2.radius) System.err.println("FALURE: different search radius.");
		System.err.flush();
		
		System.out.println("\n");
	}
	
	private static void testStatusMessage() {
		System.out.println("\tTEST: Status Message");
		String id = "567wgjsg8wtn2t";
		LocalDateTime time = LocalDateTime.now();
		double lat = 0.0074322;
		double lon = 213.637;
		double battery = 0.1134;
		DroneState state = DroneState.MOVING;
		double[] path = new double[] {
			0.123483, 0.83564363,
			0.6825725, 0.546164531,
			0.654984651, 0.879564,
		};
		StatusData d = new StatusData(id, time, lat, lon, battery, state, path);
		
		String message = d.toString();
		System.out.println(message);
		
		StatusData d2 = new StatusData(message);
		System.out.println(d2.id);
		System.out.println(d2.timestamp);
		System.out.println(d2.latitude);
		System.out.println(d2.longitude);
		System.out.println(d2.batteryStatus);
		System.out.println(d2.status);
		
		System.out.flush();
		if (!id.equals(d2.id)) System.err.println("FALURE: different ids.");
		if (!time.equals(d2.timestamp)) System.err.println("FALURE: different timestamps.");
		if (lat != d2.latitude) System.err.println("FALURE: different latitude.");
		if (lon != d2.longitude) System.err.println("FALURE: different longitude.");
		if (battery != d2.batteryStatus) System.err.println("FALURE: different battery level.");
		if (!state.equals(d2.status)) System.err.println("FALURE: different drone status.");
		System.err.flush();
		
		System.out.println("\n");
	}
	
	private static void testScanMessage() {
		System.out.println("\tTEST: Scan Message");
		String id = "ghhuj2t8hjg4w4qwghse";
		LocalDateTime time = LocalDateTime.now();
		double lat = 0.0074322;
		double lon = 213.637;
		double depth = 12.56;
		double flow = 0.00001;
		double[] distances = new double[] {
			0.01, 0.01, 0.01, 0.01, 0.02, 12.3456
		};
		ScanData s = new ScanData(id, time, lat, lon, depth, flow, distances);
		
		String message = s.toString();
		System.out.println(message);
		
		ScanData s2 = new ScanData(message);
		System.out.println(s2.id);
		System.out.println(s2.timestamp);
		System.out.println(s2.latitude);
		System.out.println(s2.longitude);
		System.out.println(s2.depth);
		System.out.println(s2.flowRate);
		for (double distance : s2.distanceReadings) {
			System.out.print(distance + ", ");
		}
		
		System.out.flush();
		if (!id.equals(s2.id)) System.err.println("FALURE: different ids.");
		if (!time.equals(s2.timestamp)) System.err.println("FALURE: different timestamps.");
		if (lat != s2.latitude) System.err.println("FALURE: different latitude.");
		if (lon != s2.longitude) System.err.println("FALURE: different longitude.");
		if (depth != s2.depth) System.err.println("FALURE: different depth.");
		if (flow != s2.flowRate) System.err.println("FALURE: different flow rate.");
		if (distances.length != s2.distanceReadings.length) System.err.println("FALURE: different number of distance readings.");
		for (int i = 0; i < Math.min(distances.length, s2.distanceReadings.length); i ++) {
			if (distances[i] != s2.distanceReadings[i]) System.err.printf("FALURE: different distance reading #(%d).\n", i);
		}
		System.err.flush();
		
		System.out.println("\n");
	}

	private static void testPathMessage() {
		System.out.println("\tTEST: Path Message");
		String id = "67yth4wge9ew9";
		String commandID = "sghij3etg83tnjwgf";
		LocalDateTime time = LocalDateTime.now();
		double eta = 12;
		PathData p = new PathData(id, time, commandID, eta);
		
		String message = p.toString();
		System.out.println(message);
		
		PathData p2 = new PathData(message);
		System.out.println(p2.id);
		System.out.println(p2.timestamp);
		System.out.println(p2.pathCommandID);
		System.out.println(p2.eta);
		
		System.out.flush();
		if (!p2.id.equals(id)) System.err.println("FAILURE: Not the same drone id.");
		if (!p2.timestamp.equals(time)) System.err.println("FAILURE: Not the same timestamp.");
		if (!p2.pathCommandID.equals(commandID)) System.err.println("FAILURE: Not the same drone id.");
		if (p2.eta != eta) System.err.println("FAILURE: Not the same number eta.");
		System.err.flush();
		
		System.out.println("\n");
	}
	
}

