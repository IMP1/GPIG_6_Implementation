package broadcast;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

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
    		socket.joinGroup(groupAddress);
    		socket.send(packet);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
