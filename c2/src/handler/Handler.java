package handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import datastore.Datastore;

public abstract class Handler implements Runnable {
	
	protected Socket socket;
	protected Datastore datastore;
	protected BufferedReader reader;
	protected PrintWriter writer;

	public Handler(Socket socket, Datastore datastore) throws IOException {
		this.socket = socket;
		this.datastore = datastore;
		this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		this.writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
	}

}
