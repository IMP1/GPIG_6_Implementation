package network;

import java.time.LocalDateTime;

@SuppressWarnings("unused")
public class Test {

	public static void main(String[] args) {
		testMoveCommand();
		testReportMessage();
		testScanMessage();
		testPathMessage();
		testC2();
	}
	
	private static void testMoveCommand() {
		String droneID = "1200afja8ahfafhf";
		LocalDateTime time = LocalDateTime.now();
		double lat = 0.001252536;
		double lon = 0.001252536;
		double rad = 4.001252536;
		MoveCommand c = new MoveCommand(droneID, time, lat, lon, rad);
		
		String message = c.toString();
		System.out.println(message);
		
		MoveCommand c2 = new MoveCommand(message);
		System.out.println(c2.id);
		System.out.println(c2.timestamp);
		System.out.println(c2.latitude);
		System.out.println(c2.longitude);
		System.out.println(c2.radius);
		
		System.out.println("\n\n");
	}
	
	private static void testReportMessage() {
		String status = "0001;2016-05-12T17:31:13.269;DATA;STATUS;0.001256475;0.87252587;100;FINE THANKS HOW ARE YOU?";
		System.out.println(Message.getType(status) + "\n\n");
		
		StatusData r = new StatusData(status);
		System.out.println(r.id);
		System.out.println(r.timestamp);
		System.out.println(r.latitude);
		System.out.println(r.longitude);
		System.out.println(r.batteryStatus);
		System.out.println(r.status);
	}
	
	private static void testScanMessage() {
		String scanData = "some_id;2016-05-12T17:31:13.269;DATA;SCAN;0.0365364363;0.52636746;1337.101;1234.56789;0,1,2,3,4";
		System.out.println(Message.getType(scanData) + "\n");
		ScanData s = new ScanData(scanData);
		System.out.println(s.id);
		System.out.println(s.timestamp);
		System.out.println(s.latitude);
		System.out.println(s.longitude);
		System.out.println(s.depth);
		System.out.println(s.flowRate);
		for (double distance : s.distanceReadings) {
			System.out.print(distance + ", ");
		}
	}

	private static void testPathMessage() {
		// Data
		final String id = "drone_id";
		final LocalDateTime time = LocalDateTime.now();
		final double[] points = new double[] { 
			53.95717145394973 , -1.0783239204362758,
			53.95725918465687 , -1.0784689578071749,
			53.9573716884264  , -1.078658947616643 ,
			53.957388824762155, -1.078682603210567 ,
			53.95727725231522 , -1.078708680243239 ,
			53.957098438376896, -1.0790372508549075,
			53.956931545367794, -1.079042093732404 , 
		};
		// Sending
		String message = "";
		PathData pathObj = new PathData(id, time, points);
		message = pathObj.toString();
		System.out.printf("[-> '%s'\n", message);
		
		// Receiving
		System.out.printf("[<- '%s'\n", message);
		System.out.println(Message.getType(message) + "\n");
		if (Message.getType(message) != PathData.class) System.err.println("FAILURE: Not the right message class.");
		PathData pathObj2 = new PathData(message);
		System.out.println(pathObj2.id);
		System.out.println(pathObj2.timestamp);
		System.out.println(pathObj2.points.length);
		for (double point : pathObj2.points) {
			System.out.print(point + ", ");
		}
		if (!pathObj2.id.equals(id)) System.err.println("FAILURE: Not the same drone id.");
		if (!pathObj2.timestamp.equals(time)) System.err.println("FAILURE: Not the same timestamp.");
		if (pathObj2.points.length != points.length) System.err.println("FAILURE: Not the same number of points.");
		for (int i = 0; i < pathObj2.points.length; i ++) {
			if (pathObj2.points[i] != points[i]) {
				System.err.println("FAILURE: Point " + i + " is not the same. " + pathObj2.points[i] + " != " + points[i] + ".");
			}
		}
	}
	
	private static void testC2() {
		String message = "some_id;2016-05-12T17:31:13.269;DATA;SCAN;0.0102;0.2864;6.02;0.12;0,1,2,3,4";
		
		// if ( CLASS_WE_CARE_ABOUT.class.isAssignableFrom(Message.getType(MESSAGE_STRING)) ) { ... }
		
		// void run() { ...
		if (Data.class.isAssignableFrom(Message.getType(message))) {
			// Handle Data Messages
			if (Message.getType(message) == StatusData.class) {
				StatusData report = new StatusData(message);
				
				// ... do whatever with status data ...
				
			} else if (MoveCommand.getType(message) == ScanData.class){
				ScanData scandata = new ScanData(message);
				
				// ... do whatever with scan data ...
				
			}
		} else if (Acknowledgement.class.isAssignableFrom(Message.getType(message))) {
			// Handle Acknowledgement Messages
		}
	}
	
}
