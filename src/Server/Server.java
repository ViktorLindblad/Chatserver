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

public class Server implements Runnable {
	
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
	private DatagramSocket datagramSocket;//UDP socket
	private ServerSocket server;//TCP socket
	
	//Ports
	private int port, TCPport;
	
	//Lists
	
	//Uses to get the name of a socket.
	private Hashtable<Socket,String>  connectedNames;
	//A list with all MessageHandlers.
	private ArrayList<MessageHandler> SMH;
	//A queue which is only used to remove MessageHandlers from SMH list.
	private LinkedList <MessageHandler> removeQueue; 
	//A queue which is only used to remove Socket from the lists.
	private LinkedList <Socket> removeSocketQueue; 

	
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
    
	public Server(int TCPport,String ip, int port) {

		this.TCPport = TCPport;
		this.port = port;
		serverName = "Lindblad's test server";
		
		connectedNames = new Hashtable<Socket,String>();
		SMH = new ArrayList<MessageHandler> ();
		
		removeQueue = new LinkedList<MessageHandler>();
		removeSocketQueue = new LinkedList<Socket>();

		buffer = new byte[65535];
		
		createTCP();
		if(!connect(ip)) {
			System.out.println("Connection failed");
		} else {

			datagramSocket.connect(address, this.port);
		}
		
		setServerId(regServer());
						
		connector = 
				new ServerConnector(server,this);
		
		new Thread(connector).start(); 
		
		new Thread (this).start();
	}
	
	/**
	 * Tries to create a new server socket on the given TCP-port. 
	 */
	private void createTCP() {
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
		while(true) {
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

		DatagramPacket packet = new DatagramPacket
									(data,data.length,address,port);
		try {
			datagramSocket.send(packet);
			datagramSocket.setSoTimeout(5000);
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
		}	catch (SocketException e) {
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
	 * Sends a REG PDU with the servers TCP port and it's name
	 * over UDP.
	 * 
	 * @return Integer - the servers ID number. 
	 */
	
	private synchronized int regServer() {
		
		REG reg = new REG(serverName,TCPport);
		send(reg.toByteArray());
		
		byte [] message = receive();
		
		if(PDU.byteArrayToLong(message,0,1) == 100) {
			send(reg.toByteArray());
			message = receive();
			
		}
				
		return (int)PDU.byteArrayToLong(message, 2,4);//ID number
		
	}

	/**
	 * Creates a new thread which only sends ALIVE PDU to
	 * the name server. If the name server answer the ALIVE PDU with
	 * NOTREG it registers the server again with a REG PDU. 
	 */
	
	private void alive() {

		Thread thread = new Thread() {
			public void run() {
								
				while(getRunning()) {
					
					try {
						Thread.sleep(8000);
						
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
															
					ALIVE alive = 
							new ALIVE(SMH.size(),getServerId());
							
					send(alive.toByteArray());
					
					byte [] message = receive();
					
					
					if(PDU.byteArrayToLong(message, 0, 1)== 100) {
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
				
				Socket socket = connector
								.getSocketQueue().remove();
				
				MessageHandler messageHandler = new 
											MessageHandler(socket);
				SMH.add(messageHandler);
			}

			for(MessageHandler temp : SMH) {
				if(!temp.getMessageQueue().isEmpty()) {
					
					buffer = temp.getMessageQueue().remove();

					int ca = (int)PDU.byteArrayToLong(buffer,0,1);
					if(ca!=12) {
						messageName = connectedNames
											.get(temp.getSocket());
						if(messageName == null ) {
							messageName = "Corrupt";
						}
					}
				
					QUIT quit = new QUIT();
					switch(ca) {
						case(MESS):
							
							int messageLength = (int)PDU
									.byteArrayToLong
									(buffer, 4, 6);
							
							String message = PDU
									.stringReader
									(buffer, 12,messageLength);
						
							MESS mess = new MESS
									(message, messageName, false);
							
							sendTCPToAll(mess.toByteArray());
	
						break;
						case(QUIT):
							answerSocket(temp.getSocket(),
									quit.toByteArray());
						
							closeSocket(temp.getSocket());
							ULEAVE leave = new ULEAVE(messageName);
							
							connectedNames.remove(temp.getSocket());
							removeQueue.add(temp);
							
							sendTCPToAll(leave.toByteArray());
							
							
							
							
						break;
						case(JOIN):
							String name = readNameFromMessage(buffer);
							
							if( checkNick(name)) {//Name is occupied.

								nickNameOccupied(temp.getSocket());
								
							} else {//Successfully join
								if(name.length()==0){
									clientSentCorruptMessage(temp.getSocket());
									removeQueue.add(temp);
								}
								connectedNames.put
											(temp.getSocket(),name);
								temp.setHasJoin(true);
								
								NICKS nick = 
										new NICKS(connectedNames);
								
								answerSocket(temp.getSocket(),
										nick.toByteArray());
							
								UJOIN join = new UJOIN(name);
								sendTCPToAll(join.toByteArray());
							}

							
						break;
						case(CHNICK):
							String newName = readNameFromMessage(buffer);
							if(checkNick(newName)) {
								
								nickNameOccupied(temp.getSocket());
							
							} else {
								if(newName.length()==0){
									clientSentCorruptMessage(temp.getSocket());
									removeQueue.add(temp);
								}
								UCNICK cnick = new 
										UCNICK(messageName,newName);
								sendTCPToAll(cnick.toByteArray());
								
								connectedNames
									.put(temp.getSocket(), newName);
							}
	
						break;
						default:
							clientSentCorruptMessage(temp.getSocket());
							removeQueue.add(temp);
							if(messageName!=null){
								ULEAVE dleave = new ULEAVE(messageName);
								
								connectedNames.remove(temp.getSocket());
								
								sendTCPToAll(dleave.toByteArray());
							}
						break;
					}
				}
			}
			while(!removeQueue.isEmpty()) {
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
			OutputStream output;
		try {
			output = temp.getOutputStream();
			output.write(bytes);
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
				
		return PDU.stringReader(bytes, 4, nameLength);

	}

	/**
	 * Sends a message with the given bytes to all connected 
	 * clients.
	 * 
	 * @param message - The bytes to send.
	 */
	
	public synchronized void sendTCPToAll(byte[] message) {
		for(MessageHandler temp : SMH) {
			
			if(connectedNames.get(temp.getSocket())!=null) {
				OutputStream output;
				try {
					output = temp.getSocket().getOutputStream();
					output.write(message);
				} catch (IOException e1) {
				
					e1.printStackTrace();
					removeSocketQueue.add(temp.getSocket());
					removeQueue.add(temp);
				}
			}
		}
	}
	
	/**
	 * Gets the condition of  running.
	 * @return running - true if the server is running else false.
	 */

	public synchronized boolean getRunning() {
		return running;
	}
	
	/**
	 * Gets the servers ID.
	 * @return Integer - The servers ID.
	 */
	
	public synchronized int getServerId() {
		return serverId;
	}
	
	/**
	 * Sets servers ID to the parameter.
	 * @param id - The servers ID
	 */
	
	public synchronized void setServerId(int id) {
		serverId = id;
	}
	
	/**
	 * Sending message to the client who sent Corrupt message.
	 */
	
	private void clientSentCorruptMessage(Socket socket) {
		String name = connectedNames.get(socket);
		if(name!=null) {
			String errorMessage = 
					name+" have send a corrupt message, goodbye!";
			MESS mess = new MESS(errorMessage, "", false);
			
			sendTCPToAll(mess.toByteArray());
		}
		QUIT quit = new QUIT();
		answerSocket(socket,quit.toByteArray());
		
		
	}

	/**
	 * Sending a message to the client if the nickname is occupied 
	 */
	
	private void nickNameOccupied(Socket socket) {
		String errorMessage = "Your nickname is occupied!";
		MESS mess = new MESS(errorMessage, "", false);
		answerSocket(socket,mess.toByteArray());
	}
	
	/**
	 * Gets the server InettAddress.
	 * 
	 * @return InetAddress - Servers InetAddress.
	 */
	
	public InetAddress getAddress() {
		return server.getInetAddress();
	}
	
	public synchronized ArrayList<MessageHandler> getSMH(){
		return SMH;
	}
	

	public synchronized void toManyClientsOnline(Socket temp) {
		String errorMessage 
			= "Sorry, it's to many client online at the moment";
		
		MESS mess = new MESS(errorMessage, "", false);
		answerSocket(temp,mess.toByteArray());
	}

	private void closeSocket(Socket socket){
		try {
			socket.getInputStream().close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		Server server = new Server(1365,"itchy.cs.umu.se",1337);
	}

}
