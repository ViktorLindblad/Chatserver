package Server;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;

import PDU.ALIVE;
import PDU.PDU;
import PDU.REG;

public class Server implements Runnable{
	
	private byte[] buffer,alive;
	private InetAddress address;
	private ServerConnector connector;
	private int serverId;
	private String serverName;
	private int  TCPport;
	
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

		this.TCPport = port;
		serverName = "Lindblad";
		
		connectedNames = new ArrayList<String>();
		connectedClients = new ArrayList<Socket> ();
		queue = new LinkedList<Socket>();
		messageQueue = new LinkedList<String> ();
		
		buffer = new byte[256];
		alive = new byte[256];
		
		createTCP();
		
		if(!connect(ip,1337)){
			System.out.println("Connection failed");
		} else {
			datagramSocket.connect(address, 1337);
		}
		
		setServerId(regServer());
						
		connector = new ServerConnector(server);
		
		new Thread(connector).start(); 
		
		new Thread (this).start();
	}
	
	private void createTCP(){
		try {
			
			server = new ServerSocket(TCPport);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private synchronized byte[] receive() {
		DatagramPacket packet = new DatagramPacket(buffer,buffer.length);
		
		while(true){// recieve data until timeout
	     
			try {
				datagramSocket.receive(packet);
				return packet.getData();
				
			} catch (SocketTimeoutException e) {
				// timeout exception.
				System.out.println("Timeout reached!!! " + e);
				datagramSocket.close();
	     	} catch (IOException e) {
	        e.printStackTrace();
	     	}
	    }
				
	}

	private synchronized void send(byte[] data) {

		System.out.println("sending package, length is: "+data.length);
		DatagramPacket packet = new DatagramPacket(data,data.length,address,1337);
		try {
			datagramSocket.send(packet);
			datagramSocket.setSoTimeout(1000);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean connect(String ip, int port) {
	try {
		datagramSocket = new DatagramSocket(1337);
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
	
	private synchronized int regServer() {

		REG reg = new REG(serverName,TCPport);
		
		send(reg.toByteArray());
		
		byte [] message = receive();

		
		if(PDU.byteArrayToLong(message,0,1) == 100){
			
			send(reg.toByteArray());
			message = receive();
			
		} else if(PDU.byteArrayToLong(message,0,1) == 1){
			System.out.println("Regged");
		}
				
		return (int)PDU.byteArrayToLong(message, 2,4);//ID number
		
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

	public void hearthBeat() {

		Thread thread = new Thread(){
			public void run(){
				
				long time;
				
				while(getRunning()){
					
					try {
						Thread.sleep(8000);
						
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					time = System.nanoTime() * 1000000000;
										
					ALIVE alive = 
							new ALIVE(getClients().size(),getServerId());
							
					send(alive.toByteArray());
					
					byte [] message = receive();
					
					System.out.println("PDU number: "+PDU.byteArrayToLong(message, 0, 1));
					
					if(PDU.byteArrayToLong(message, 0, 1)== 100){
						regServer();
					} else if(PDU.byteArrayToLong(message, 0, 1)==1){
						System.out.println("still regged "+time);
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
			
			if(!connector.getSocketQueue().isEmpty()){
				System.out.println("in queue");
				Socket socket = connector.getSocketQueue().remove();
				connectedClients.add(socket);
				ServerMessageHandler SMH = new ServerMessageHandler(socket);
				new Thread(SMH).start();
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
	
	public synchronized boolean getRunning(){
		return running;
	}
	
	public synchronized int getServerId(){
		return serverId;
	}
	
	public synchronized void setServerId(int id){
		serverId = id;
	}
	
	public synchronized ArrayList<Socket> getClients(){
		return connectedClients;
	}	
		
	@SuppressWarnings("unused")
	public static void main(String[] args){
		Server server = new Server(1345,"itchy.cs.umu.se");
	}
}
