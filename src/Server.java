import java.io.*;
import java.net.*;
import java.util.*;

public class Server extends PDU implements Runnable{
	
	private byte[] buffer,alive;
	private InetAddress address;
	private ServerConnector connector;
	private int serverId;
	
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
		
		System.out.println(datagramSocket.isConnected());
		
		ByteSequenceBuilder BSB = new ByteSequenceBuilder();
		BSB.append(OpCode.REG.value);
		BSB.pad();

		send(BSB.toByteArray());
		serverId = receive();
		
		System.out.println(serverId);
		
		createTCP();
		
		connector = new ServerConnector(server);
		
		new Thread(connector).start(); 
		
		new Thread (this).start();
	}
	
	private void createTCP(){
		try {
			
			server = new ServerSocket(port);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private int receive() {
		DatagramPacket packet = new DatagramPacket(buffer,buffer.length);

		try{
			datagramSocket.receive(packet);
		} catch (IOException e){
			e.printStackTrace();
		}
		return (int)PDU.byteArrayToLong(packet.getData(),0,packet.getLength());
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
		datagramSocket = new DatagramSocket(0);
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

	public synchronized void hearthBeat() {

		Thread thread = new Thread(){
			
			public void run(){
				while(getRunning()){
					
					ByteSequenceBuilder BSB = new ByteSequenceBuilder();
					BSB.append(OpCode.ALIVE.value);
					BSB.appendInt(getServerId());
					BSB.pad();
					
					System.out.println(getServerId());
					
					send(BSB.toByteArray());
					System.out.println(receive());
					
					try {
						Thread.sleep(2000);
						
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		
		thread.start();
	}
	
	public void run() {

		String inputLine = "";

		hearthBeat();

		while(getRunning()){			
			
			if(!connector.getQueue().isEmpty()){
				System.out.println("in queue");
				Socket socket = connector.getQueue().remove();
				connectedClients.add(socket);
			}

			for(Socket  temp : connectedClients){
				// new class ServerMessageHandler e.g. Class which listens after clients message...
				try {
					System.out.println("outsream");
					outStream = temp.getOutputStream();
				} catch (IOException e) {
					e.printStackTrace();
				}
				out = new PrintWriter(outStream, true);
				
				try {
					System.out.println("instream");
					inStream = temp.getInputStream();
				} catch (IOException e) {
					e.printStackTrace();
				}
		        in = new BufferedReader(new InputStreamReader(inStream));
		        
		        try {
		        	System.out.println("tries to get input line");
					if((inputLine = in.readLine()) != null){
					} 
				} catch (IOException e) {
					e.printStackTrace();
				}
		        System.out.println("sending message to clients");
				out.println(inputLine);
				out.flush();
			}
		}
	}
	
	public byte[] toByteArray() {
		
		return null;
	}
	
	public synchronized boolean getRunning(){
		return running;
	}
	
	public synchronized int getServerId(){
		return serverId;
	}
	
	@SuppressWarnings("unused")
	public static void main(String[] args){
		Server server = new Server(1337,"itchy.cs.umu.se");
	}
}
