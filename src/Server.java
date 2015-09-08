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
	private Hashtable<Socket,String> connectedNames;
	private ArrayList<String> connectedClients;
	
	//input & output
	private PrintWriter out;
    private BufferedReader in;
	
    //variables 
    private boolean running;
	
	public Server(int port){
		super("Server");
		this.port = port;
		connectedNames = new Hashtable<Socket,String>();
		connectedClients = new ArrayList<String> ();

		ServerSocket server = null;
		try {
			
			server = new ServerSocket(port);
			socket = server.accept();

			out = new PrintWriter(socket.getOutputStream(), true);
	        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}

		start();//calls run
		
	}
	
	public Server(int port, SocketAddress IP) throws IOException {
		super("Server");
		this.port = port;
		connectedNames = new Hashtable<Socket,String>();
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
		
		connectedNames.put(socket, "server");

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
		connectedNames.put(socket,str);
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
		System.out.println("Server is running");
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
				while((inputLine = in.readLine()) != null){
					out.println(inputLine);
				}
				out.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		
		}
		
	}
	
	public static void main(String[] args){
		Server server = new Server(45);
	}
}
