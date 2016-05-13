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
			System.out.println(message);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		writer.println("YoYo");
		try {
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
