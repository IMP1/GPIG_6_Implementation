package frontendserver;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.UUID;

import broadcast.Broadcast;
import datastore.Datastore;
import network.PathCommand;

public class FrontendServerHandler implements Runnable{
	
	protected Socket socket;
	protected Datastore datastore;
	protected BufferedReader reader;
	protected PrintWriter writer;
	protected Integer etaRequestID;
	
	public FrontendServerHandler(Socket socket, Datastore datastore) throws IOException {
		this.socket = socket;
		this.datastore = datastore;
		this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		this.writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
		etaRequestID = 0;
	}

	@Override
	public void run() {
//		System.out.println("Frontend Server Request Received, handler started");
		try {
			String message = reader.readLine();
			process_request(message);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void process_request(String request){
		if(request.contains("GetDroneInfo")){
			//TODO add in C2 with ID "c2".
			String data = getDroneData();
			reply(data);
		}
		if(request.contains("AssignSearchAreas")){
			//TODO - Clever things.
			HTTPRequest reqObj = new HTTPRequest(request);
			Double loclat = Double.parseDouble(reqObj.params.get("latitude"));
			Double loclong = Double.parseDouble(reqObj.params.get("longitude"));
			Integer numberRequested = Integer.parseInt(reqObj.params.get("numberRequested"));
			Double searchRadius = Double.parseDouble(reqObj.params.get("radius"));
			etaRequestID++;
			String uniqueID = UUID.randomUUID().toString();
			SearchArea searchArea = new SearchArea(uniqueID, loclat, loclong, numberRequested, searchRadius);
			SearchAreaWaiter waiter = new SearchAreaWaiter(searchArea, datastore);
			String[] result = waiter.doWait();
			reply(result.toString());//maybe?
		}
		if(request.contains("ClearSearchAreas")){
			//TODO - More clever things.
		}
		if(request.contains("RecallUnits")){
			//TODO - GET TO THE CHOPPA.
		}
		if(request.contains("GetScanInfo")){
			System.out.println(request);
			String known_scans_string = request.replace("GET /GetScanInfo?known_scans=", "").replace(" HTTP/1.1", "");//ew.
			String[] known_scans = known_scans_string.split(",");
			String data = getScanData(known_scans);
			reply(data);
		}
	}
	
	public String getDroneData(){
		return datastore.getDronesAsJSON();
		
	}
	
	public String getScanData(String[] known_scans){
		return datastore.getScansAsJSON(known_scans);
	}
	
	public void reply(String content){
		writer.println("HTTP/1.1 200 OK");
		writer.println("Access-Control-Allow-Origin:*");//STFU
		writer.println("Content-Type: text/html");
		writer.println();
		writer.println(content);
	}

}
