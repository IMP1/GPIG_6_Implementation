package drones.sensors;

import java.io.FileReader;
import java.io.IOException;

import au.com.bytecode.opencsv.*;


/**
 * Sensor interface.
 * All static methods as these will eventually be piped out to hardware.
 * For now, GPS is updated by the Navigation thread and sonar/depth/flow data 
 * are returned as absolute values from a pre-defined data set based on location.
 * @author Anthony Williams and Martin Higgs
 */
public abstract class SensorInterface {
	public static double getGPSLatitude() {
		return 53.957184;
	}
	
	public static double getGPSLongitude() {
		return -1.078302;
	}
	
	// TODO: Set GPS via 'Navigation'

	// TODO: Create and read in pre-defined sonar, depth and flow data
	public static double[] getDataForPoint(double lat, double lon){
		CSVReader reader = null;
		double[] output = new double[366];
		
		try{
			reader = new CSVReader(new FileReader("../sonar.csv"));
		}
		catch (Exception e){
			// ohnoes
			System.out.println(e.getMessage());
		}
		String [] line;
		try {
			while ((line = reader.readNext()) != null){
				if(Double.parseDouble(line[0]) == lat && Double.parseDouble(line[1]) == lon){
					System.out.println("Hurray");
					System.out.println("Depth: " + line[2] + " Flow: " + line [3]);
					for (int i = 4; i < line.length; i++){
						
						System.out.print(line[i] + ",");
					}
					System.out.println();
					
					for (int i = 0 ; i < line.length; i++){
						output[i] = Double.parseDouble(line[i]);
					}
					break;
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		
		return output;
	}
}
