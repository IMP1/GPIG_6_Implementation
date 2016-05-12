package network;

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
		String a = "1;2016;COMMAND;MOVE;1235.035353;-525106.525252;0";
		System.out.println(Message.getType(a) + "\n\n");
		MoveCommand c = new MoveCommand(a);
		System.out.println(c.id);
		System.out.println(c.timestamp);
		System.out.println(c.latitude);
		System.out.println(c.longitude);
		System.out.println(c.radius);
		System.out.println("\n\n");
		
		MoveCommand c2 = new MoveCommand("2", "2017", "x", "y", "r");
		System.out.println(c2.toString());
	}
	
	private static void testReportMessage() {
		String status = "0001;-1;DATA;STATUS;x;y;100;FINE THANKS HOW ARE YOU?";
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
		String scanData = "some_id;some_time;DATA;SCAN;lat;long;depth;flow;0,1,2,3,4";
		System.out.println(Message.getType(scanData) + "\n");
		ScanData s = new ScanData(scanData);
		System.out.println(s.id);
		System.out.println(s.timestamp);
		System.out.println(s.latitude);
		System.out.println(s.longitude);
		System.out.println(s.depth);
		System.out.println(s.flowRate);
		for (String distance : s.distanceReadings) {
			System.out.print(distance + ", ");
		}
	}

	private static void testPathMessage() {
		// Data
		final String id = "drone_id";
		final String time = "unix_time";
		final String[] points = new String[] { 
			"53.95717145394973" , "-1.0783239204362758",
			"53.95725918465687" , "-1.0784689578071749",
			"53.9573716884264"  , "-1.078658947616643" ,
			"53.957388824762155", "-1.078682603210567" ,
			"53.95727725231522" , "-1.078708680243239" ,
			"53.957098438376896", "-1.0790372508549075",
			"53.956931545367794", "-1.079042093732404" , 
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
		for (String point : pathObj2.points) {
			System.out.print(point + ", ");
		}
		if (!pathObj2.id.equals(id)) System.err.println("FAILURE: Not the same drone id.");
		if (!pathObj2.timestamp.equals(time)) System.err.println("FAILURE: Not the same timestamp.");
		if (pathObj2.points.length != points.length) System.err.println("FAILURE: Not the same number of points.");
		for (int i = 0; i < pathObj2.points.length; i ++) {
			if (!pathObj2.points[i].equals(points[i])) {
				System.err.println("FAILURE: Point " + i + " is not the same. " + pathObj2.points[i] + " != " + points[i] + ".");
			}
		}
	}
	
	private static void testC2() {
		String message = "some_id;some_time;DATA;SCAN;lat;long;depth;flow;0,1,2,3,4";
		
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

