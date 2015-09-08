import java.io.*;
import java.net.*;
import java.util.*;

public class Server extends Thread {
	//sockets
	private DatagramSocket datagramSocket;
	private Socket socket;
	
	//port
	private int port;
	
	//Listor
	private Hashtable<Socket,String> connectedNames;
	private ArrayList<String> connectedClients;
	
	//?
	private InputStream IS;
	private Scanner scanner;
	private boolean running;
	
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
		sendUDP(DP);
		try{
			ServerSocket server = new ServerSocket(port);
			socket = server.accept();
		} catch (SocketException e){
			e.printStackTrace();
		}
		
		connectedNames.put(socket, "server");

		IS = socket.getInputStream();
		scanner = new Scanner(IS);
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
		while(running){	
			
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
			
			//Send TCP 
			 
			//Receive TCP
			
			while(scanner.hasNextLine()){
				System.out.println(scanner.nextLine());
			}
		
		}
		
	}
	
	
	//Behövs ej...
	public void sendUDP(DatagramPacket DP)  throws IOException {
		datagramSocket.send(DP);
	}
	
	public void recieveUDP(DatagramPacket DP) throws IOException{
		datagramSocket.receive(DP);
	}
	
	
	//lite fel...
	public void sendTCP() throws IOException{
		socket.getInputStream();
	}
	
	public void recieveTCP() throws IOException{
		socket.getOutputStream();
	}
}
