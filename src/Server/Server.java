package Server;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;

import PDU.ALIVE;
import PDU.NICKS;
import PDU.PDU;
import PDU.QUIT;
import PDU.REG;
import PDU.UCNICK;
import PDU.UJOIN;
import PDU.ULEAVE;

public class Server implements Runnable{
	
	private byte[] buffer,alive;
	private InetAddress address;
	private ServerConnector connector;
	private int serverId, messageIndex;
	private String serverName, messageName;
	
	
	//sockets
	private DatagramSocket datagramSocket;
	private ServerSocket server;
	private Socket socket;
	
	//port
	private int port, TCPport;
	
	//Lists
	private ArrayList<String> connectedNames;
	private ArrayList<Socket> connectedClients;
	private ArrayList<ServerMessageHandler> SMH;
	//input & output
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
		SMH = new ArrayList<ServerMessageHandler> ();
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

		while(getRunning()) {			
			for(String temp : connectedNames) {
				System.out.println("Connected with nick: "+temp);
			}
			if(!connector.getSocketQueue().isEmpty()) {
				System.out.println("A client connected");
				Socket socket = connector.getSocketQueue().remove();
				connectedClients.add(socket);
				ServerMessageHandler messageHandler = new ServerMessageHandler(socket);
				SMH.add(messageHandler);
				System.out.println("Server starts a new thread for a server message handler");
				new Thread(messageHandler).start();
			}
			for(ServerMessageHandler temp : SMH) {
				
				if(!temp.getMessageQueue().isEmpty()) {
					messageIndex = 0;
					for(Socket socket : connectedClients){
						messageIndex++;
						if(socket.equals(temp.getSocket())){
							messageName = connectedNames.get(messageIndex);
						}
					}
					
					buffer = temp.getMessageQueue().remove();
					
					int ca = (int)PDU.byteArrayToLong(buffer,0,1);
					
					switch(ca) {
						case(10)://MESS
							
						break;
						case(11)://QUIT
							ULEAVE leave = new ULEAVE(messageName);
							sendTCPToAll(leave.toByteArray());
						break;
						case(12)://JOIN
							String name = readNameFromMessage(buffer);
							
							if(checkNick(name)) {
								ByteSequenceBuilder BSB = 
										new ByteSequenceBuilder();
								
								byte[] message = 
										BSB.append(OpCode.NICKO.value)
										.pad()
										.toByteArray();
								
								answerSocket(temp.getSocket(),message);
								
							} else {
							
								NICKS nick = 
										new NICKS(connectedNames);
								
								answerSocket(temp.getSocket(),
										nick.toByteArray());
							
								UJOIN join = new UJOIN(name);
								sendTCPToAll(join.toByteArray());
							}
						break;
						case(13)://CHNICK
							String newName = readNameFromMessage(buffer);
							if(checkNick(newName)){
								
								ByteSequenceBuilder BSB = 
										new ByteSequenceBuilder();
								
								byte[] message = 
										BSB.append(OpCode.NICKO.value)
										.pad()
										.toByteArray();
								
								answerSocket(temp.getSocket(),message);
							
							} else {
								UCNICK cnick = new UCNICK(messageName,newName);
								sendTCPToAll(cnick.toByteArray());
								
								connectedNames.set(messageIndex, newName);
							}
						break;
						default:
							QUIT quit = new QUIT();
							answerSocket(temp.getSocket(),quit.toByteArray());
						break;
					}
				}
			}
		}
	}
	
	private boolean checkNick(String name) {
		
		for(String names : connectedNames) {
			if(names.equals(name)) {
				return true;
			}
		}
		return false;
	}

	private void answerSocket(Socket temp, byte[] bytes) {
			DataOutputStream DO;
		try {
			DO = new DataOutputStream(temp.getOutputStream());
			DO.write(bytes);
			DO.flush();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	private String readNameFromMessage(byte[] bytes) {
		
		int nameLength = (int)PDU.byteArrayToLong(bytes,1,2);
		
		byte[] tempbytes = Arrays.copyOfRange(bytes,4, 4+nameLength);
		
		return PDU.bytaArrayToString(tempbytes, nameLength);

	}

	private void sendTCPToAll(byte[] message) {
		for(Socket temp : connectedClients){
			
			DataOutputStream DO;
			
			try {
				DO = new DataOutputStream(temp.getOutputStream());
				DO.write(message);
				DO.flush();
			} catch (IOException e1) {
				e1.printStackTrace();
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
