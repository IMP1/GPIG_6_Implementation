package externalPoll;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import gpig.all.schema.DataType;
import gpig.all.schema.GPIGData;
import gpig.all.schema.Point;
import gpig.all.schema.Polar;
import gpig.all.schema.Position;

public class ExternalPollThread implements Runnable {

	private ArrayList<String> endpoints;

	@Override
	public void run() {
		// TODO Auto-generated method stub
		System.out.println("Starting external API polling thread");
		try {
			Scanner read = new Scanner(new File("endpoints.cfg"));
			endpoints = new ArrayList<String>();
			while (read.hasNext()) {
				endpoints.add(read.nextLine());
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		while (true) {
			try {
				do_poll();
				Thread.sleep(2000);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	private void do_poll() throws IOException {
		StringBuilder result = new StringBuilder();
		URL url = new URL(endpoints.get(0));
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.addRequestProperty("User-Agent", "Mozilla/4.76");
		conn.setRequestMethod("GET");
		BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String line;
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}
		rd.close();
		JAXBContext jc;
		try {
			jc = JAXBContext.newInstance(GPIGData.class);
			Unmarshaller u = jc.createUnmarshaller();
			GPIGData data = (GPIGData) u.unmarshal(new StringReader(result.toString()));
			Position position = data.positions.iterator().next().position;
			if (position instanceof Point) {
				Point point = (Point) position;
			}
			System.out.println(data.positions.iterator().next().position);
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
