import java.io.*;
import java.net.*;
import java.util.*;

public class Server extends Thread {
	//sockets
	private DatagramSocket datagramSocket;
	private Socket socket;
	
	//Listor
	private Hashtable<Socket,String> connectedNames;
	private ArrayList<String> connectedClients;
	
	//?
	private InputStream IS;
	private Scanner scanner;
	private boolean running;
	
	public Server(int port, SocketAddress IP) throws IOException {
		super();
		connectedNames = new Hashtable<Socket,String>();
		connectedClients = new ArrayList<String> ();
		try{
			datagramSocket = new DatagramSocket(port);
			String s = "%0";
			byte[] data = s.getBytes();
			DatagramPacket DP = new DatagramPacket(data,data.length,port,IP);
			sendUDP(DP);
			
		} catch(SocketException e){
			e.printStackTrace();
		}
		
		try{
			ServerSocket server = new ServerSocket(port);
			Socket socket = server.accept();
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
		while(checkConnection()){	
			
			while(scanner.hasNextLine()){
				System.out.println(scanner.nextLine());
			}
		
		}
	}
	
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
