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
import java.util.LinkedList;

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
	
	/**
	 * A server socket, with a thread for connecting clients and one
	 * sending alive messages to the name server. The server also
	 * starts a new thread if a client joins which it listens after
	 * messages from the client. The server also check the message
	 * and replies/forwards messages.
	 */
	//Constants for OpCodes.
	private static final int MESS = 10, QUIT = 11, JOIN = 12, CHNICK = 13;
	
	private byte[] buffer;
	
	//Sockets
	private DatagramSocket datagramSocket;//UTP socket
	private ServerSocket server;//TCP socket
	
	//Ports
	private int port, TCPport;
	
	//Lists
	
	//Uses to get the name of a socket.
	private Hashtable<Socket,String>  connectedNames;
	//A list with all connected sockets.
	private ArrayList<Socket> connectedClients;
	//A list with all MessageHandlers.
	private ArrayList<MessageHandler> SMH;
	//A queue which is only used to remove MessageHandlers from SMH list.
	private LinkedList <MessageHandler> removeQueue; 
	
    //Variables 
    private boolean running = true;
	private InetAddress address;
	private ServerConnector connector;
	private int serverId;
	private String serverName, messageName;
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
		
		removeQueue = new LinkedList<MessageHandler>();
		buffer = new byte[256];
		
		createTCP();
		if(!connect(ip)){
			System.out.println("Connection failed");
		} else {
			System.out.println(address);
			datagramSocket.connect(address, this.port);
		}
		
		setServerId(regServer());
						
		connector = 
				new ServerConnector(server);
		
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
	 * Listens after an answer from the name server, uses UDP. 
	 * 
	 * Catches IOExceptions and SocketTimeoutExceptions. 
	 * @return byte[] - The bytes received.
	 */
	
	private synchronized byte[] receive() {
		buffer = new byte[256];
		DatagramPacket packet = new DatagramPacket(buffer,buffer.length);
		while(true){
			try {
				datagramSocket.receive(packet);
				return packet.getData();
			} catch (SocketTimeoutException e) {
				System.out.println("Timeout reached!!! " + e);
				datagramSocket.close();
	     	} catch (IOException e) {
	     		e.printStackTrace();
	     	}
		}
	}

	/**
	 * Tries to send bytes over UDP to the connected name server.
	 * Also sets a timeout for answer to 5 seconds.
	 * 
	 * @param data - The bytes to send.
	 */
	private synchronized void send(byte[] data) {

		DatagramPacket packet = new DatagramPacket(data,data.length,address,port);
		try {
			datagramSocket.send(packet);
			datagramSocket.setSoTimeout(5000);
			System.out.println("message sent");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Tries to create a new datagramSocket on the given port.
	 * 
	 * @param ip - A string representation of the InetAddress
	 * @param port - The port to connect the socket to.
	 * @return true if succeed to create a datagramSocket and 
	 * 				false else.
	 */
	
	private boolean connect(String ip) {
	try {
		datagramSocket = new DatagramSocket(port);
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
	 * Registers the server to the name server.
	 * Sends a REG PDU with the servers TCP port and it's name.
	 * 
	 * @return Integer - the servers ID number. 
	 */
	
	private synchronized int regServer() {
		
		REG reg = new REG(serverName,TCPport);
		send(reg.toByteArray());
		
		byte [] message = receive();
		
		if(PDU.byteArrayToLong(message,0,1) == 100){
			System.out.println("not regged");
			send(reg.toByteArray());
			message = receive();
			
		}
		System.out.println("regged");
				
		return (int)PDU.byteArrayToLong(message, 2,4);//ID number
		
	}

	/**
	 * Creates a new thread which only sends ALIVE PDU to
	 * the name server. If the name server answer the ALIVE PDU with
	 * NOTREG it registers the server again with a REG PDU. 
	 */
	
	private void alive() {

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
					}
				}
			}
		};
		
		thread.start();
	}
	
	/**
	 * This method is called when the server thread starts.
	 * It creates a thread to send ALIVE PDU's and messageHandler
	 * thread for each client who joins the chat. 
	 * When a messageHandler has something in it's queue
	 * the server checks the message and answer it. 
	 */
	
	public void run() {

		alive();// Starting new thread

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
					
					int messageHasLength;
					
					int ca = (int)PDU.byteArrayToLong(buffer,0,1);
					if(ca!=12) {
						messageName = connectedNames.get(temp.getSocket());
					}
					
					switch(ca) {
						case(MESS)://MESS

							if(checkMessageLength(buffer)){
								int messageLength = (int)PDU.byteArrayToLong(buffer, 4, 6);
								String message = PDU.stringReader(buffer, 8,messageLength);
							
								MESS mess = new MESS(message, messageName, false);						
								sendTCPToAll(mess.toByteArray());
							} else {
								clientSentCorruptMessage(temp.getSocket());
								ULEAVE leave = new ULEAVE(messageName);
								
								connectedClients.remove(temp.getSocket());
								connectedNames.remove(temp.getSocket());
								removeQueue.add(temp);
								sendTCPToAll(leave.toByteArray());
								QUIT quit = new QUIT();
								answerSocket(temp.getSocket(),quit.toByteArray());
							}
						break;
						case(QUIT)://QUIT
							ULEAVE leave = new ULEAVE(messageName);
							connectedClients.remove(temp.getSocket());
							connectedNames.remove(temp.getSocket());
							removeQueue.add(temp);
							sendTCPToAll(leave.toByteArray());
							
							
							
							
						break;
						case(JOIN)://JOIN
							String name = readNameFromMessage(buffer);
							
							if(checkNameLength(buffer)) {
								if( checkNick(name)) {

									nickNameOccupied(temp.getSocket());
									connectedClients.remove(temp.getSocket());
									removeQueue.add(temp);
									
								} else {
									connectedNames.put(temp.getSocket(),name);
									NICKS nick = 
											new NICKS(connectedNames);
									
									answerSocket(temp.getSocket(),
											nick.toByteArray());
								
									UJOIN join = new UJOIN(name);
									sendTCPToAll(join.toByteArray());
								}
								
							} else {
								messageHasLength = 
										(int)PDU.byteArrayToLong(buffer, 1, 2);
								if(messageHasLength == 0){
									nickNameIsZero(temp.getSocket());
									connectedClients.remove(temp.getSocket());
									removeQueue.add(temp);
								} else {
							
									clientHasToLongName(temp.getSocket());
									ULEAVE uleave = new ULEAVE(messageName);
									
									connectedClients.remove(temp.getSocket());
									connectedNames.remove(temp.getSocket());
									sendTCPToAll(uleave.toByteArray());
									QUIT quit = new QUIT();
									answerSocket(temp.getSocket(),quit.toByteArray());
								}
							}
							
						break;
						case(CHNICK)://CHNICK
							String newName = readNameFromMessage(buffer);
							if(checkNameLength(buffer)) {
								if(checkNick(newName)){
									
									nickNameOccupied(temp.getSocket());
								
								} else {
									UCNICK cnick = new UCNICK(messageName,newName);
									sendTCPToAll(cnick.toByteArray());
									
									connectedNames.put(temp.getSocket(), newName);
								}
							} else {
								messageHasLength = 
										(int)PDU.byteArrayToLong(buffer, 1, 2);
								if(messageHasLength == 0){
									nickNameIsZero(temp.getSocket());
								} else {
									clientHasToLongName(temp.getSocket());
									ULEAVE uleave = new ULEAVE(messageName);
									
									connectedClients.remove(temp.getSocket());
									connectedNames.remove(temp.getSocket());
									
									sendTCPToAll(uleave.toByteArray());
									QUIT quit = new QUIT();
									answerSocket(temp.getSocket(),quit.toByteArray());
								}
							}
						break;
						default:
							QUIT quit = new QUIT();
							answerSocket(temp.getSocket(),quit.toByteArray());
							ULEAVE dleave = new ULEAVE(messageName);
							connectedClients.remove(temp.getSocket());
							connectedNames.remove(temp.getSocket());
							removeQueue.add(temp);
							sendTCPToAll(dleave.toByteArray());
						break;
					}
				}
			}
			while(!removeQueue.isEmpty()){
				SMH.remove(removeQueue.removeFirst());
			}
		}
	}
	
	/**
	 * Checks if a name already exist in the hash table. 
	 * 
	 * @param name - The name to check
	 * @return true if the name is in the list else false.
	 */
	
	private boolean checkNick(String name) {
		
		for(Socket temp : connectedNames.keySet()) {
			String names = connectedNames.get(temp);
			if(names.equals(name)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Sends a message to the given socket with the given bytes.
	 * 
	 * @param temp - The socket. 
	 * @param bytes - The bytes to send.
	 */
	
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
	
	/**
	 * Reads the name from a byte[]. Can only be used with these PDU's:
	 * JOIN, CHNICK. 
	 * @param bytes
	 * @return
	 */

	private String readNameFromMessage(byte[] bytes) {
		
		int nameLength = (int)PDU.byteArrayToLong(bytes,1,2);
		System.out.println("namelenght "+nameLength);
				
		return PDU.stringReader(bytes, 4, nameLength);

	}

	/**
	 * Sends a message with the given bytes to all connected 
	 * clients.
	 * 
	 * @param message - The bytes to send.
	 */
	
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
	
	/**
	 * 
	 * @return running - true if the server is running else false.
	 */

	public synchronized boolean getRunning(){
		return running;
	}
	
	/**
	 * 
	 * @return Integer - The servers ID.
	 */
	
	public synchronized int getServerId(){
		return serverId;
	}
	
	/**
	 * Sets servers ID to the parameter.
	 * @param id - The servers ID
	 */
	
	public synchronized void setServerId(int id){
		serverId = id;
	}
	
	/**
	 * Sending message to the client who sent Corrupt message.
	 */
	
	private void clientSentCorruptMessage(Socket socket){
		String errorMessage = "You have send a corrupt message, goodbye!";
		MESS mess = new MESS(errorMessage, "", false);
		answerSocket(socket,mess.toByteArray());
	}
	
	/**
	 * Sending a message to the client who had chosen a to long name.
	 */
	
	private void clientHasToLongName(Socket socket){
		String errorMessage = "Your nickname is to long, goodbye!";
		MESS mess = new MESS(errorMessage, "", false);
		answerSocket(socket,mess.toByteArray());
	}
	
	/**
	 * Sending a message to the client if the nickname is occupied 
	 */
	
	private void nickNameOccupied(Socket socket){
		String errorMessage = "Your nickname is occupied!";
		MESS mess = new MESS(errorMessage, "", false);
		answerSocket(socket,mess.toByteArray());
	}
	
	/**
	 * Sending a message to the client if the nickname has zero length.
	 */
	
	private void nickNameIsZero(Socket socket){
		String errorMessage = "You can't have a nickname with zero length!\n"
				+ "Try a different name.";
		MESS mess = new MESS(errorMessage, "", false);
		answerSocket(socket,mess.toByteArray());
	}
	
	/**
	 * Checks the messages length.
	 * Only works with MESS PDU
	 * 
	 * @param bytes - The message in bytes.
	 * @return Boolean - true if message is not to long else false.
	 */
	
	private boolean checkMessageLength(byte[] bytes){
		int messageHasLength = (int)PDU.byteArrayToLong(bytes, 4, 6);
		return messageHasLength <= 65535;	
	}
	
	/**
	 * Checks the name length.
	 * Only works with JOIN and CHNICK PDU.
	 * 
	 * @param bytes - The name in bytes.
	 * @return boolean - true if the name is not to long else false.
	 */
	
	private boolean checkNameLength(byte[] bytes){

		int messageHasLength = (int)PDU.byteArrayToLong(bytes, 1, 2);
		return messageHasLength <= 255 && messageHasLength != 0;	
		
	}
	
	/**
	 * Gets the server InettAddress.
	 * @return InetAddress - Servers InetAddress.
	 */
	
	public InetAddress getAddress(){
		return server.getInetAddress();
	}
	
	/**
	 * Gets the servers hash table with sockets and names.  
	 * @return Hash table - Servers hash table.
	 */
	public synchronized Hashtable<Socket, String> getNames(){
		return connectedNames;
	}
	
	@SuppressWarnings("unused")
	public static void main(String[] args){
		Server server = new Server(1365,"itchy.cs.umu.se",1337);
	}
}
