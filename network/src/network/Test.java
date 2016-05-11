package network;

public class Test {

	public static void main(String[] args) {
		String a = "1;2016;COMMAND;MOVETO;1235.035353;-525106.525252;0";
		System.out.println(Message.getType(a) + "\n\n");
		Command c = new Command(a);
		System.out.println(c.id);
		System.out.println(c.timestamp);
		System.out.println(c.latitude);
		System.out.println(c.longitude);
		System.out.println(c.radius);
		System.out.println("\n\n");
		
		Command c2 = new Command("2", "2017", "x", "y", "r");
		System.out.println(c2.toString());
		
		String report = "0001;-1;INFO;x;y;100;FINE THANKS HOW ARE YOU?";
		System.out.println(Message.getType(report) + "\n\n");
		
		Report r = new Report(report);
		System.out.println(r.id);
		System.out.println(r.timestamp);
		System.out.println(r.latitude);
		System.out.println(r.longitude);
		System.out.println(r.batteryStatus);
		System.out.println(r.status);
		
		String scanData = "some_id;some_time;DATA;lat;long;depth;flow;0,1,2,3,4";
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

}

