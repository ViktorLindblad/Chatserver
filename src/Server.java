import java.io.*;
import java.net.*;
import java.util.*;

public class Server extends PDU implements Runnable{
	
	private byte[] buffer,alive;
	private InetAddress address;
	private ServerConnector connector;
	
	//sockets
	private DatagramSocket datagramSocket;
	private Socket socket;
	private ServerSocket server;
	
	//port
	private int port;
	
	//Lists
	private ArrayList<String> connectedNames;
	private ArrayList<Socket> connectedClients;
	private LinkedList<Socket> queue;
	private LinkedList<String> messageQueue;

	
	//input & output
	private PrintWriter out;
    private BufferedReader in;
    private OutputStream outStream;
    private InputStream inStream;
	
    //variables 
    private boolean running = true;
	private boolean noName = false;
	
	public Server(int port,String ip){

		this.port = port;
		
		connectedNames = new ArrayList<String>();
		connectedClients = new ArrayList<Socket> ();
		queue = new LinkedList<Socket>();
		messageQueue = new LinkedList<String> ();
		
		this.port = port;
		buffer = new byte[256];
		alive = new byte[256];
		
		if(!connect(ip,port)){
			System.out.println("Connection failed");
		} else {
			datagramSocket.connect(address, port);
		}
		
		
		ByteSequenceBuilder BSB = new ByteSequenceBuilder();
		BSB.append(OpCode.REG.value);
		BSB.pad();

		send(BSB.toByteArray());
		receive();
		
		createTCP();
		
		connector = new ServerConnector(server);
		
		new Thread(connector).start(); 
		
		new Thread (this).start();
	}
	
	private void createTCP(){
		try {
			
			server = new ServerSocket(5);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private String receive() {
		DatagramPacket packet = new DatagramPacket(buffer,buffer.length);

		try{
			datagramSocket.receive(packet);
		} catch (IOException e){
			e.printStackTrace();
		}
		String message = new String(packet.getData());

		return message;
	}

	private void send(byte[] data) {

		DatagramPacket packet = new DatagramPacket(data,data.length,address,port);
		try {
			datagramSocket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean connect(String ip, int port) {
	try {
		datagramSocket = new DatagramSocket(5);
		address = InetAddress.getByName(ip);
		}	catch (SocketException e){
		e.printStackTrace();
		return false;
		}	catch (IOException e) {
		e.printStackTrace();
		return false;
		}
		return true;
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
			server.close();
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
	/*
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
	}*/

	public void run() {

		String inputLine = "";

		ByteSequenceBuilder BSB = new ByteSequenceBuilder();
		BSB.append(OpCode.ALIVE.value);
		BSB.pad();

		alive = BSB.toByteArray();

		while(running){
			/*
			send(alive);
			receive();
			*/
			if(!connector.getQueue().isEmpty()){
				Socket socket = connector.getQueue().remove();
				connectedClients.add(socket);
			}
			for(Socket  temp : connectedClients){
				
				try {
					outStream = temp.getOutputStream();
				} catch (IOException e) {
					e.printStackTrace();
				}
				out = new PrintWriter(outStream, true);
				
				try {
					inStream = temp.getInputStream();
				} catch (IOException e) {
					e.printStackTrace();
				}
		        in = new BufferedReader(new InputStreamReader(inStream));
			}
			
			//move upp in other for loop.
			if(!connectedClients.isEmpty()){
				for(Socket temp : connectedClients){
					
					try {
						if((inputLine = in.readLine()) != null){
						} 
					} catch (IOException e) {
						e.printStackTrace();
					}
				
					out.println(inputLine);
					out.flush();
				}
			}
		}
	}
	
	public byte[] toByteArray() {
		
		return null;
	}
	
	@SuppressWarnings("unused")
	public static void main(String[] args){
		Server server = new Server(1337,"itchy.cs.umu.se");
	}
}
