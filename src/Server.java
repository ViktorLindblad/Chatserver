import java.io.*;
import java.net.*;
import java.util.Hashtable;

public class Server extends ServerSocket implements Runnable {
	
	private Thread thread;
	private BufferedReader BF;
	private Hashtable<Socket,String> conectedNames;
	private boolean running;
	
	public Server(int port, int backlog, InetAddress bindAddr)
			throws IOException {
		super(port, backlog, bindAddr);
		thread = new Thread(this);
		thread.start();//calls run
	}
	
	/**
	 * 	A heart beat to show the name server it's "alive".
	 */
	
	private boolean checkConnection(){
		return running;
	}
	
	/**
	 * 	Tries to change the servers port. 
	 * @param port - the port number it will try to change to.
	 * @return true if port changed successfully and false if not
	 */
	
	public boolean changeServerPort(int port){
		return false;
	}

	public void run() {
		while(checkConnection()){	
			
		}
	}
	
	
}
