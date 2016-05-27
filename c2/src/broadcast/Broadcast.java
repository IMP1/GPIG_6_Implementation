package broadcast;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class Broadcast {
	private static InetAddress groupAddress;
	private static MulticastSocket socket;
	public static void broadcast(String message){
		byte[] sendData = new byte[1024]; 
		sendData = message.getBytes();
		try {
			groupAddress = InetAddress.getByName(network.Message.MESH_GROUP_ADDRESS);
			DatagramPacket packet = new DatagramPacket(sendData, sendData.length, groupAddress, network.Message.MESH_PORT); 
			socket = new MulticastSocket(network.Message.MESH_PORT);
			Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();
			NetworkInterface eth0 = null;
			while (enumeration.hasMoreElements()) {
			    eth0 = enumeration.nextElement();
			    if (eth0.getName().equals("p3p1")) {
			        //there is probably a better way to find ethernet interface
			    	
			    	// We are in the hardware lab
					socket.setNetworkInterface(eth0);
			        break;
			    }
			}

			socket.setTimeToLive(2);
    		socket.joinGroup(groupAddress);
    		socket.send(packet);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
