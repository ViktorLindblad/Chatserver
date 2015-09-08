import java.io.*;
import java.net.*;



public class Client extends Thread {
	
	private MulticastSocket multicastSocket;
	private Socket socket;
	
	private boolean connected = true;
	private PrintWriter out;
	private BufferedReader in;
	private BufferedReader keyReader;
	private OutputStream outStream;
	private InputStream inStream;
	
	//client is the name we use
	
	public Client(int port){
		super("a Client Thread");
		try{
			socket = new Socket("localhost",port);
			
			keyReader = new BufferedReader(new InputStreamReader(System.in));
			
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
		int i = 0;
		while(connected){
			System.out.println(i);
			i++;
	        String sendMessage, receiveMessage;
	 
	        try {
				sendMessage = keyReader.readLine();
		        out.println(sendMessage);
		        out.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
	 
	        try{
	        	if((receiveMessage = in.readLine()) != null){
	        		System.out.println(receiveMessage);
	        	}
	        } catch (IOException e){
	        	e.printStackTrace();
	        }
	        
	        
		}
	}
	
	public static void main(String[] args){
		Client client = new Client(45);
	}
	
}
