package frontendserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import datastore.Datastore;

public class FrontendServer implements Runnable {
	
	private final int port;
	private Datastore datastore;
	private ServerSocket serverSocket;

	public FrontendServer(int port, Datastore datastore) {
		this.port = port; 
		this.datastore = datastore;
	}

	@Override
	public void run() {
		System.out.println("Frontend server starting on port "+port);
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		while(true){
			Socket clientSocket;
			try {
				clientSocket = serverSocket.accept();
				FrontendServerHandler handler = new FrontendServerHandler(clientSocket, datastore);
				new Thread(handler).start();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}

	}
}
