package frontendserver;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import datastore.Datastore;

public class FrontendServerHandler implements Runnable{
	
	protected Socket socket;
	protected Datastore datastore;
	protected BufferedReader reader;
	protected PrintWriter writer;
	
	public FrontendServerHandler(Socket socket, Datastore datastore) throws IOException {
		this.socket = socket;
		this.datastore = datastore;
		this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		this.writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
	}

	@Override
	public void run() {
		System.out.println("Frontend Server Request Received, handler started");
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
		if(request.contains("drones")){
			String data = getDroneData();
			reply(data);
		}
		if(request.contains("AssignSearchAreas")){
			//TODO - Clever things.
		}
		if(request.contains("ClearSearchAreas")){
			//TODO - More clever things.
		}
		if(request.contains("RecallUnits")){
			//TODO - GET TO THE CHOPPA.
		}
	}
	
	public String getDroneData(){
		return datastore.getDronesAsJSON();
		
	}
	public void reply(String content){
		writer.println("HTTP/1.1 200 OK");
		writer.println("Access-Control-Allow-Origin:*");
		writer.println("Content-Type: text/html");
		writer.println();
		writer.println(content);
	}

}
