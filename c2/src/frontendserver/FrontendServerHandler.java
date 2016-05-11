package frontendserver;
import java.io.IOException;
import java.net.Socket;

import datastore.Datastore;
import handler.Handler;

public class FrontendServerHandler extends Handler{

	public FrontendServerHandler(Socket socket, Datastore datastore) throws IOException {
		super(socket, datastore);
	}

	@Override
	public void run() {
		System.out.println("Frontend Server Request Received, handler started");
		writer.println("YoYo");
		try {
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
