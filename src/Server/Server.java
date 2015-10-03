package Server;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Hashtable;

import PDU.ALIVE;
import PDU.MESS;
import PDU.NICKS;
import PDU.PDU;
import PDU.REG;
import PDU.UCNICK;
import PDU.UJOIN;
import PDU.ULEAVE;

public class Server implements Runnable{
	
	/**
	 * A server socket, with a thread for connecting clients and one
	 * sending alive messages to the name server. The server also
	 * starts a new thread if a client joins which it listens after
	 * messages from the client. The server also check the message
	 * and replies/forwards messages.
	 */
	
	private static final int MESS = 10, QUIT = 11, JOIN = 12, CHNICK = 13;
	
	private byte[] buffer;
	private InetAddress address;
	private ServerConnector connector;
	private int serverId;
	private String serverName, messageName;
	
	//sockets
	private DatagramSocket datagramSocket;
	private ServerSocket server;
	
	//port
	private int port, TCPport;
	
	//Lists
	private Hashtable<Socket,String>  connectedNames;
	private ArrayList<Socket> connectedClients;
	private ArrayList<MessageHandler> SMH;
	
    //variables 
    private boolean running = true;
	
    /**
     * Creates a new server and register itself to the name server, it also
     * starts a thread which listens after clients to join the server.  
     * 
     * @param TCPport - port where the server accepts clients.
     * @param ip - The address to name server.
     * @param port - The port to name server.
     */
    
	public Server(int TCPport,String ip, int port){

		this.TCPport = TCPport;
		this.port = port;
		serverName = "Lindblad";
		
		connectedNames = new Hashtable<Socket,String>();
		connectedClients = new ArrayList<Socket> ();
		SMH = new ArrayList<MessageHandler> ();
		buffer = new byte[256];
		
		createTCP();
		
		if(!connect(ip,this.port)){
			System.out.println("Connection failed");
		} else {
			datagramSocket.connect(address, 1337);
		}
		
		setServerId(regServer());
						
		connector = 
				new ServerConnector(server,this.TCPport,
						server.getInetAddress().getHostName());
		
		new Thread(connector).start(); 
		
		new Thread (this).start();
	}
	
	/**
	 * Tries to create a new server socket on the given TCP-port. 
	 */
	private void createTCP(){
		try {		
			server = new ServerSocket(TCPport);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Listens after an answer from the name server, uses UTP. 
	 * 
	 * Catches IOExceptions and SocketTimeoutExceptions. 
	 * @return byte[] - The bytes received.
	 */
	
	private byte[] receive() {
		buffer = new byte[256];
		DatagramPacket packet = new DatagramPacket(buffer,buffer.length);
		
		try {
			datagramSocket.receive(packet);
			
		} catch (SocketTimeoutException e) {
			System.out.println("Timeout reached!!! " + e);
			datagramSocket.close();
     	} catch (IOException e) {
     		e.printStackTrace();
     	}
		
		return packet.getData();
	
	}

	private synchronized void send(byte[] data) {

		System.out.println("sending package, length is: "+data.length);
		DatagramPacket packet = new DatagramPacket(data,data.length,address,1337);
		try {
			datagramSocket.send(packet);
			//datagramSocket.setSoTimeout(5000);
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

	private void hearthBeat() {

		Thread thread = new Thread(){
			public void run(){
								
				while(getRunning()){
					
					try {
						Thread.sleep(8000);
						
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
															
					ALIVE alive = 
							new ALIVE(connectedClients.size(),getServerId());
							
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
	
	public void checkMessage() {

		Thread thread = new Thread(){
			public void run(){
								
				while(getRunning()){

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
				
				connectedClients.add(socket);
				MessageHandler messageHandler = new MessageHandler(socket);
				SMH.add(messageHandler);
			}

			for(MessageHandler temp : SMH) {
				if(!temp.getMessageQueue().isEmpty()) {

					buffer = temp.getMessageQueue().remove();
					
					int ca = (int)PDU.byteArrayToLong(buffer,0,1);
					if(ca!=12) {
						messageName = connectedNames.get(temp.getSocket());
					}
					
					switch(ca) {
						case(MESS)://MESS
							for(int i=0; i<buffer.length; i++){
								System.out.println(buffer[i]);
							}
							if(checkMessageLength(buffer)){
								temp.setReceivedCorrectMessage(true);
								int messageLength = (int)PDU.byteArrayToLong(buffer, 4, 6);
								String message = PDU.stringReader(buffer, 8,messageLength);
							
								MESS mess = new MESS(message, messageName, false);						
								sendTCPToAll(mess.toByteArray());
							} else {
								clientSentCorruptMessage();
								ULEAVE leave = new ULEAVE(messageName);
								
								connectedClients.remove(temp.getSocket());
								connectedNames.remove(temp.getSocket());
								sendTCPToAll(leave.toByteArray());
								temp.setReceivedCorrectMessage(false);
							}
						break;
						case(QUIT)://QUIT
							temp.setReceivedCorrectMessage(false);
							ULEAVE leave = new ULEAVE(messageName);
							connectedClients.remove(temp.getSocket());
							connectedNames.remove(temp.getSocket());
							
							sendTCPToAll(leave.toByteArray());
							
						break;
						case(JOIN)://JOIN
							String name = readNameFromMessage(buffer);
							
							if(checkNameLength(buffer)) {
								temp.setReceivedCorrectMessage(true);
								if( checkNick(name)) {
									ByteSequenceBuilder BSB = 
											new ByteSequenceBuilder();
									
									byte[] occupied = 
											BSB.append(OpCode.NICKO.value)
											.pad()
											.toByteArray();
									
									answerSocket(temp.getSocket(),occupied);
								} else {
									temp.setReceivedCorrectMessage(true);
									connectedNames.put(temp.getSocket(),name);
									NICKS nick = 
											new NICKS(connectedNames);
									
									answerSocket(temp.getSocket(),
											nick.toByteArray());
								
									UJOIN join = new UJOIN(name);
									sendTCPToAll(join.toByteArray());
								}
								
							} else {
								clientHasToLongName();
								ULEAVE uleave = new ULEAVE(messageName);
								
								connectedClients.remove(temp.getSocket());
								connectedNames.remove(temp.getSocket());
								
								sendTCPToAll(uleave.toByteArray());
								temp.setReceivedCorrectMessage(false);
							}
							
						break;
						case(CHNICK)://CHNICK
							String newName = readNameFromMessage(buffer);
							if(checkNameLength(buffer)) {
								if(checkNick(newName)){
									temp.setReceivedCorrectMessage(true);
									ByteSequenceBuilder BSB = 
											new ByteSequenceBuilder();
									
									byte[] occupied = 
											BSB.append(OpCode.NICKO.value)
											.pad()
											.toByteArray();
									
									answerSocket(temp.getSocket(),occupied);
								
								} else {
									temp.setReceivedCorrectMessage(true);
									UCNICK cnick = new UCNICK(messageName,newName);
									System.out.println("sending UCNICK");
									sendTCPToAll(cnick.toByteArray());
									
									connectedNames.put(temp.getSocket(), newName);
								}
							} else {
								clientHasToLongName();
								ULEAVE uleave = new ULEAVE(messageName);
								
								connectedClients.remove(temp.getSocket());
								connectedNames.remove(temp.getSocket());
								
								sendTCPToAll(uleave.toByteArray());
								temp.setReceivedCorrectMessage(false);
							}
						break;
						default:
							temp.setReceivedCorrectMessage(false);
						break;
					}
				}
			}
		}
	}
	
	private boolean checkNick(String name) {
		
		for(Socket temp : connectedNames.keySet()) {
			String names = connectedNames.get(temp);
			if(names.equals(name)) {
				return true;
			}
		}
		return false;
	}

	public synchronized void answerSocket(Socket temp, byte[] bytes) {
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
		System.out.println("namelenght "+nameLength);
				
		return PDU.stringReader(bytes, 4, nameLength);

	}

	public synchronized void sendTCPToAll(byte[] message) {
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
	
	private void clientSentCorruptMessage(){
		String errorMessage = "##You have send a corrupt message, goodbye!##";
		MESS mess = new MESS(errorMessage, serverName, false);
		sendTCPToAll(mess.toByteArray());
	}
	
	private void clientHasToLongName(){
		String errorMessage = "##Your nickname is to long, goodbye!##";
		MESS mess = new MESS(errorMessage, serverName, false);
		sendTCPToAll(mess.toByteArray());
	}
	
	
	private boolean checkMessageLength(byte[] bytes){
		int messageHasLength = (int)PDU.byteArrayToLong(bytes, 4, 6);
		return messageHasLength <= 65535;	
	}
	
	private boolean checkNameLength(byte[] bytes){

		int messageHasLength = (int)PDU.byteArrayToLong(bytes, 1, 2);
		return messageHasLength <= 255;	
		
	}
	
	public InetAddress getAddress(){
		return server.getInetAddress();
	}
	
	public synchronized Hashtable<Socket, String> getNames(){
		return connectedNames;
	}
	
	
	/**
	 * 	Tries to change the servers port. 
	 * @param port - the port number it will try to change to.
	 * @return true if port changed successfully and false if not
	 */
	/*
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
	}*/
	
	@SuppressWarnings("unused")
	public static void main(String[] args){
		Server server = new Server(1345,"itchy.cs.umu.se",1337);
	}
}
