
public class Runner {

	public static void main(String[] args) {
		UDPClient client = new UDPClient();
		new Thread(client).start();
		
		UDPServer server= new UDPServer();
		new Thread(server).start();
		

	}

}
