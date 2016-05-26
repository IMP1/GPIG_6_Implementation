package frontendserver;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import broadcast.Broadcast;
import datastore.Datastore;
import datastore.Drone;
import gpig.all.schema.BoundingBox;
import gpig.all.schema.Coord;
import gpig.all.schema.GISPosition;
import gpig.all.schema.GPIGData;
import gpig.all.schema.Point;
import gpig.all.schema.Poly;
import gpig.all.schema.Timestamp;
import gpig.all.schema.datatypes.WaterEdge;
import network.MoveCommand;
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
			reply(datastore.gson.toJson(result));
		}
		if(request.contains("ClearSearchAreas")){
			//TODO - More clever things.
		}
		if(request.contains("RecallUnits")){
			HashMap<String,Drone> drones= datastore.getDrones();
			Double locLat = drones.get("c2").getLocLat();
			Double locLong = drones.get("c2").getLocLong();
			for (String key : drones.keySet()){
				System.out.println(key);
				if(key != "c2"){
					MoveCommand command = new MoveCommand(key,LocalDateTime.now(), locLat, locLong, 0);
					Broadcast.broadcast(command.toString());
				}

			}
		}
		if(request.contains("GetScanInfo")){
//			System.out.println(request);
			String known_scans_string = request.replace("GET /GetScanInfo?last_timestamp=", "").replace(" HTTP/1.1", "");//ew.
//			String str = "1986-04-08 12:30";
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
			LocalDateTime dateTime = LocalDateTime.parse(known_scans_string, formatter);
			dateTime = dateTime.plusSeconds(1);
			System.out.println(dateTime);
			String data = getScanData(dateTime);
//			System.err.println(data);
			reply(data);
		}
		if(request.contains("ExternalEndpoint")){
	        StringWriter sw = new StringWriter();
//			StringWriter data = new StringWriter();
			
			Timestamp ts = new Timestamp();
	        ts.date = new Date();
	        
	        GPIGData data = new GPIGData();
	        data.positions = new HashSet<>();
	        
	        //Edges
	        ArrayList<ArrayList<Coord>> edges = datastore.getEdges();
	        for (final ArrayList<Coord> edgeslist:edges){
	        	Poly poly = new Poly();
		        poly.coords = edgeslist;
		        WaterEdge wateredge = new WaterEdge();
		        

		        GISPosition gis = new GISPosition();
		        gis.position = poly;
		        gis.timestamp = ts;
		        gis.payload = wateredge;
		        data.positions.add(gis);
	        }
	        //TODO: Depth, flow.
	          
	        JAXBContext jaxbContext = null;
	        try {
	            jaxbContext = JAXBContext.newInstance(GPIGData.class);
	            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

	            // output pretty printed
	            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

	            jaxbMarshaller.marshal(data, sw);
	        } catch (JAXBException e) {
	            e.printStackTrace();
	        }
	        System.out.println(sw.toString());
			reply(sw.toString());
		}
	}
	
	public String getDroneData(){
		return datastore.getDronesAsJSON();
		
	}
	
	public String getScanData(LocalDateTime known_scans){
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
