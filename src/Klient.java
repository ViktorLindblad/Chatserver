import java.io.IOException;
import java.net.*;



public class Klient extends Thread {
	
	private MulticastSocket multicastSocket;
	private Socket socket;
	
	public Klient(int port, SocketAddress IP){
		super();
	try {
		multicastSocket = new MulticastSocket(port);
	} catch (IOException e) {
		e.printStackTrace();
		}
	}
}
