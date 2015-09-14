import java.io.*;
import java.net.*;
import java.util.*;

public class Server extends Thread {
	
	//sockets
	private DatagramSocket datagramSocket;
	private Socket socket;
	
	//port
	private int port;
	
	//Lists
	private Hashtable<String,Socket> connectedNames;
	private ArrayList<String> connectedClients;
	private LinkedList<Socket> queue;

	
	//input & output
	private PrintWriter out;
    private BufferedReader in;
    private OutputStream outStream;
    private InputStream inStream;
	
    //variables 
    private boolean running = true;
	
	public Server(int port){
		super("Server");
		this.port = port;
		connectedNames = new Hashtable<String,Socket>();
		connectedClients = new ArrayList<String> ();
		queue = new LinkedList<Socket>();

		ServerSocket server = null;
		try {
			
			server = new ServerSocket(port);
			socket = server.accept();

			outStream = socket.getOutputStream();
			out = new PrintWriter(outStream, true);
			
			inStream = socket.getInputStream();
	        in = new BufferedReader(new InputStreamReader(inStream));

		} catch (IOException e) {
			e.printStackTrace();
		}

		start();//calls run
		
	}
	
	public Server(int port, SocketAddress IP) throws IOException {
		super("Server");
		this.port = port;
		connectedNames = new Hashtable<String,Socket>();
		connectedClients = new ArrayList<String> ();
		try{
			datagramSocket = new DatagramSocket(port);
		} catch(SocketException e){
			e.printStackTrace();
		}
		String s = "%0";
		byte[] data = s.getBytes();
		DatagramPacket DP = new DatagramPacket(data,data.length,port,IP);
		datagramSocket.send(DP);
		try{
			ServerSocket server = new ServerSocket(port);
			System.out.println("before socket.accept()");
			
			socket = server.accept();
			
			System.out.println("input initiate");
			out = new PrintWriter(socket.getOutputStream(), true);
	        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	        System.out.println("after input");
		} catch (SocketException e){
			e.printStackTrace();
		}
		
		connectedNames.put("server",socket);

		start();//calls run
		
	}
	
	/**
	 * 	A heart beat to show the name server it's "alive".
	 */
	
	private void checkConnection(){
		//Integer i = Integer.parseInt("0");
		String s = "0";
		byte[] data = s.getBytes();
		DatagramPacket ping = new DatagramPacket(data,data.length,
				datagramSocket.getLocalPort(),datagramSocket.getRemoteSocketAddress());
		try{
			datagramSocket.send(ping);
		} catch(SocketException e){
			System.out.println("Socket exception");
			e.printStackTrace();
		} catch(IOException e){
			System.out.println("IO exception");
			e.printStackTrace();
		}
	}
	
	/**
	 * 	Tries to change the servers port. 
	 * @param port - the port number it will try to change to.
	 * @return true if port changed successfully and false if not
	 */
	
	public boolean changeServerPort(int port){
		boolean condition = false;
		this.port = port;
		try{
			ServerSocket server = new ServerSocket(port);
			socket = server.accept();
			condition = true;
		} catch(SocketException e){
			System.out.println("Socket");
			e.printStackTrace();
		} catch(IOException e ){
			System.out.println("IO");
			e.printStackTrace();
		}
		return condition;
	}
	
	public boolean addSocketToList(Socket socket, String str){
		connectedNames.put(str, socket);
		
		for(String string : connectedNames.keySet()){
			out.println(string);
			out.flush();
		}
		
		return connectedClients.add(str);
	}
	
	public void removeSocketFromList(String str){
		connectedNames.remove(str);
		int index = 0;
		for(String string : connectedClients){
			index++;
			if(string.equals(str)){
				connectedClients.remove(index);
				return;
			}
		}
	}

	public void run() {
		System.out.println("Server is running...");
		while(running){

			/*
			checkConnection();
			DatagramPacket p = null;
			
			try {
				datagramSocket.receive(p);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			String str = p.toString();
			Integer message = Integer.parseInt(str);
			
			if(message.intValue()==1){
				running = true;
			} else {
				running = false;
			}
			*/

			String inputLine;
			
			try {
				if((inputLine = in.readLine()) != null){
					System.out.println(inputLine);
					out.println(inputLine);
					out.flush();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		
		}
		
	}
	
	public static void main(String[] args){
		Server server = new Server(45);
	}
}
