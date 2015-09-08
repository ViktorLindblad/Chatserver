import java.io.IOException;
import java.net.*;



public class Klient extends Thread {
	
	private MulticastSocket multicastSocket;
	private Socket socket;
	
	public Klient(int port, SocketAddress IP){
		super("a Client Thread");
	try {
		multicastSocket = new MulticastSocket(port);
	} catch (IOException e) {
		e.printStackTrace();
		}
	}
}
