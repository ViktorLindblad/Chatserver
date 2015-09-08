import java.io.*;
import java.net.*;



public class Client extends Thread {
	
	private MulticastSocket multicastSocket;
	private Socket socket;
	
	private boolean connected = true;
	private PrintWriter out;
	private BufferedReader in;
	
	//client is the name we use
	
	public Client(int port){
		super("a Client Thread");
		try{
			socket = new Socket("localhost",port);
			out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch(IOException e){
			e.printStackTrace();
		}
		
		start(); // calls run
	}
	
	public Client(int port, SocketAddress IP){
		super("a Client Thread");
	try {
		multicastSocket = new MulticastSocket(port);
	} catch (IOException e) {
		e.printStackTrace();
		}
	}
	
	public void run(){
		while(connected){
			BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
	        String fromServer = null;
	        String fromUser = null;
	 
	        try{
	        	fromServer = in.readLine();
	        } catch(IOException e ){
	        	e.printStackTrace();
	        }
	        
	        while (fromServer != null) {
	            System.out.print("---->");
	            if (fromServer.equals("Bye.")){
	            	break;
	            }
	             
	            try {
					fromUser = stdIn.readLine();
				} catch (IOException e) {
					e.printStackTrace();
				}
	            if (fromUser != null){
	                out.println(fromUser);
	            }
	        }
		}
	}
	
	public static void main(String[] args){
		Client client = new Client(45);
	}
	
}
