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
import PDU.MESS;
import PDU.NICKS;
import PDU.PDU;
import PDU.QUIT;
import PDU.REG;
import PDU.UCNICK;
import PDU.UJOIN;
import PDU.ULEAVE;

public class Server implements Runnable{
	
	private static final int MESS = 10, QUIT = 11, JOIN = 12, CHNICK = 13;
	
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
	private ArrayList<MessageHandler> SMH;
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
		SMH = new ArrayList<MessageHandler> ();
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
					
					
					if(PDU.byteArrayToLong(message, 0, 1)== 100){
						regServer();
					} else if(PDU.byteArrayToLong(message, 0, 1)==1){
					}
				}
			}
		};
		
		thread.start();
	}
	
	public void run() {

		hearthBeat();// Starting new thread

		while(getRunning()) {
			
			if(!connector.getSocketQueue().isEmpty()) {
				System.out.println("A client connected");
				Socket socket = connector.getSocketQueue().remove();
				System.out.println("adding client to list");
				connectedClients.add(socket);
				System.out.println(connectedClients.size());
				MessageHandler messageHandler = new MessageHandler(socket);
				SMH.add(messageHandler);
				System.out.println("Server starts a new thread for a server message handler");
				new Thread(messageHandler).start();
			}
			
			for(MessageHandler temp : SMH) {
				
				if(!temp.getMessageQueue().isEmpty()) {
					
					
					buffer = temp.getMessageQueue().remove();
					
					int ca = (int)PDU.byteArrayToLong(buffer,0,1);
					if(ca!=12){
						messageIndex = 0;
						for(Socket socket : connectedClients){
							
							if(socket.equals(temp.getSocket())){
								messageName = connectedNames.get(messageIndex);
							}
							messageIndex++;
						}
					}
					switch(ca) {
						case(MESS)://MESS
							for(int i=0; i<buffer.length; i++){
								System.out.println(buffer[i]);
							}
							System.out.println("-------------------------------------------message received");
							int messageLength = (int)PDU.byteArrayToLong(buffer, 4, 6);
							System.out.println("L"+messageLength);
							
							String message = PDU.stringReader(buffer, 8);

							System.out.println("m "+message);
							System.out.println("Namn "+messageName);

							Boolean isClient = false;
							
							MESS mess = new MESS(message, messageName, isClient);
							System.out.println("send: "+mess.toByteArray().length);
							sendTCPToAll(mess.toByteArray());
							
						break;
						case(QUIT)://QUIT
							ULEAVE leave = new ULEAVE(messageName);
							sendTCPToAll(leave.toByteArray());
						break;
						case(JOIN)://JOIN
							String name = readNameFromMessage(buffer);
							
							if(checkNick(name)) {
								ByteSequenceBuilder BSB = 
										new ByteSequenceBuilder();
								
								byte[] occupied = 
										BSB.append(OpCode.NICKO.value)
										.pad()
										.toByteArray();
								
								answerSocket(temp.getSocket(),occupied);
								
							} else {
								connectedNames.add(name);
								NICKS nick = 
										new NICKS(connectedNames);
								
								answerSocket(temp.getSocket(),
										nick.toByteArray());
							
								UJOIN join = new UJOIN(name);
								sendTCPToAll(join.toByteArray());
							}
						break;
						case(CHNICK)://CHNICK
							String newName = readNameFromMessage(buffer);
							if(checkNick(newName)){
								
								ByteSequenceBuilder BSB = 
										new ByteSequenceBuilder();
								
								byte[] occupied = 
										BSB.append(OpCode.NICKO.value)
										.pad()
										.toByteArray();
								
								answerSocket(temp.getSocket(),occupied);
							
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
			OutputStream DO;
		try {
			DO = temp.getOutputStream();
			DO.write(bytes.length);
			DO.write(bytes);
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
			
			OutputStream DO;
			
			try {
				DO = temp.getOutputStream();

				DO.write(message.length);
				DO.write(message);
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
