package externalPoll;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;


public class ExternalPollThread implements Runnable {
	
	private ArrayList<String> endpoints;
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		System.out.println("Starting external API polling thread");
		try {
			Scanner read = new Scanner (new File("endpoints.cfg"));
			endpoints = new ArrayList<String>();
			while (read.hasNext())
			{
			   endpoints.add(read.nextLine());
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		while(true){
			try {
				do_poll();
				Thread.sleep(500);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		
	}
	
	private void do_poll() throws IOException{
	  StringBuilder result = new StringBuilder();
      URL url = new URL("http://localhost:8081/GetDroneInfo");
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("GET");
      BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
      String line;
      while ((line = rd.readLine()) != null) {
         result.append(line);
      }
      rd.close();
      System.out.println(result.toString());
      //TODO Actually use config and XML stuff.
		
	}

}
