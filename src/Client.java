
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.MulticastSocket;
import java.net.Socket;
import java.net.SocketAddress;





public class Client extends Thread {
	
	@SuppressWarnings("unused")
	private MulticastSocket multicastSocket;
	private Socket socket;
	
	private GUI gui;
	private GUI login;

	private boolean connected = true;
	private PrintWriter out;
	private BufferedReader in;
	private OutputStream outStream;
	private InputStream inStream;	
	
	//client is the name we use
	
	public Client(int port){
		super("a Client Thread");
						
		login = new GUI(this,120,120);
		gui = new GUI(this);
		
		try{
			socket = new Socket("localhost",port);
			
			outStream = socket.getOutputStream();
			out = new PrintWriter(outStream, true);
			
			inStream = socket.getInputStream();
	        in = new BufferedReader(new InputStreamReader(inStream));
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
	
		//Skicka namn till servern
		while(connected){
	        String receiveMessage = "";

	        if(!gui.getQueue().isEmpty()){
				out.print(gui.getQueue().remove());
		        out.flush();
			} else {
				out.print("");
				out.flush();
			}

	        try{
	        	if((receiveMessage = in.readLine()) != null){
	        	}
	        } catch (IOException e){
	        	e.printStackTrace();
	        }
	        
	        System.out.println(receiveMessage);
	        
	        if(receiveMessage != ""){
	        	System.out.print("sant");
        		gui.getStringFromClient(receiveMessage);
	        }
	        
		}
	}
	
	@SuppressWarnings("unused")
	public static void main(String[] args){
		
		Client client = new Client(45);
	}
}
