package Server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;

import PDU.GETLIST;
import PDU.JOIN;
import PDU.PDU;

public class Client implements Runnable {
	
	//Constants for OpCodes
	private static final int MESS = 10, QUIT = 11, UJOIN = 16, 
							ULEAVE = 17, UCNICK = 18, NICKS = 19;
	//Sockets
	private MulticastSocket multicastSocket;//UDP socket
	private Socket socket;//TCP socket
		
	//Lists
	private LinkedList<byte[]> messageQueue;
	//A list with all server names.
	private ArrayList<String> serverNames;
	//A list with all server ports.
	private ArrayList<Integer> serverPort;
	//A list with all server addresses.
	private ArrayList<Inet4Address> adresses;
	//A list with number of clients in a server.
	private ArrayList<Integer> serverClients;

	
	private OutputStream outStream;
	private InputStream inStream;
	
	private int UDPport, port, sequenceNumber, servers;
	private InetAddress address;
	private GUI gui;
	private String name = "";
	private byte[] buffer;
	private boolean running;
	
	/**
	 * Creates a new Client.
	 * 
	 * @param ip - String representation of to the address to server.
	 * @param gui - The gui to this client.
	 * @param port - The UDP port to the name server.
	 */
	
	public Client(String ip, GUI gui, int UDPport, int port) {

		this.UDPport = UDPport;
		this.port = port;
		buffer = new byte[65535];
		this.gui = gui;
		servers = 0;		
		
		messageQueue = new LinkedList<byte[]>();
		
		if(!connect(ip)) {
			System.out.println("Connection failed");
		} else {
			multicastSocket.connect(address, port);
		}
		
		getlist();	

		infoToClient();
		running = true;
	}
	
	/**
	 * Sends GETLIST PDU to the name server and receives the answer.
	 * It also checks the message and save information into the lists. 
	 */
	
	private void getlist() {
		byte[] message;
		
		serverNames = new ArrayList<String>();
		serverPort = new ArrayList<Integer>();
		adresses = new ArrayList<Inet4Address>();
		serverClients = new ArrayList<Integer>();
		
		GETLIST getList = new GETLIST();
		
		send(getList.toByteArray());
		message = receive();

		if(PDU.byteArrayToLong(message,0,1) == 4) {
			
			sequenceNumber = (int)PDU.byteArrayToLong(message,1,2);
			servers = (int)PDU.byteArrayToLong(message,2,4);
			int byteIndex = 4;
			int tempint;
			byte[] tempbytes;
			
			for(int i = 0; i < servers; i++) {
				tempbytes = null;
				tempbytes = Arrays.copyOfRange(message,byteIndex,
													byteIndex+4);

				byteIndex +=4;

				try {
					adresses.add((Inet4Address)Inet4Address
									.getByAddress(tempbytes));
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
				
				tempint = (int)PDU.byteArrayToLong(message,
											byteIndex,byteIndex+2);

				byteIndex += 2;
				serverPort.add(tempint);
				
				tempint =  (int)PDU.byteArrayToLong(message,
											byteIndex,byteIndex+1);

				byteIndex += 1;
				serverClients.add(tempint);
				
				tempint = (int)PDU.byteArrayToLong(message,
											byteIndex,byteIndex+1);

				byteIndex += 1;
				
				tempbytes = null;
				tempbytes = Arrays.copyOfRange(message,
											byteIndex, byteIndex+tempint);
				
				String server = PDU.bytaArrayToString
											(tempbytes,tempint);
				serverNames.add(server);

				if(tempint%4==0) {
					byteIndex += tempint;
					
				} else if(tempint%4==1) {
					byteIndex += tempint+3;
			
				} else if(tempint%4==2) {
					byteIndex += tempint+2;
				
				} else if(tempint%4==3) {
					byteIndex += tempint+1;
				
				}
			}
		}
	}
	
	/**
	 * Prints out information given from SLIST to the GUI.
	 */
	
	private void infoToClient() {
		int index = 0;
		gui.getStringFromClient("Sequence number from Slist is: "+
		sequenceNumber);
		
		if(!serverNames.isEmpty()) {
			for(String temp : serverNames) {
				
				index++;
				gui.getStringFromClient("Server number: "+index);
				index--;
				
				gui.getStringFromClient("Server name: "+temp);
				gui.getStringFromClient("Address: "+adresses.get(index));
				gui.getStringFromClient("Port: "+serverPort.get(index));
				gui.getStringFromClient
								("Clients: "+serverClients.get(index)
														+"\n");
				index++;
				
			}
		} else {
			gui.getStringFromClient("No servers at this time");

		}
		
		gui.getStringFromClient
			("Please choose server to start chatting or update: \n");
		
	}
	
	/**
	 * Listens after messages from the server.
	 * The message is saved into the buffer field which is a byte[]. 
	 */
	
	private void receiveTCP() {

		try {
			int len;
			byte[] tempbuffer = new byte[0];
			buffer = new byte[65535];
			
			len = inStream.read(buffer);
			if(len < 0){
				return;
			} else {
				tempbuffer = new byte[len];

				for(int i = 0; i < len; i++ ) {
					tempbuffer[i] = buffer[i];
				}
				
				int PDUlength = checkReceivedMessage(tempbuffer);

				if(PDUlength == tempbuffer.length) {
					messageQueue.add(tempbuffer);
				}
				
				while((PDUlength - tempbuffer.length) != 0) {
					
					if(PDUlength - tempbuffer.length < 0) {
						
						addNextMessage(tempbuffer,PDUlength);
						tempbuffer = Arrays.copyOfRange(tempbuffer,
										PDUlength, tempbuffer.length);
						
						PDUlength = checkReceivedMessage(tempbuffer);
						
					} else if(PDUlength - tempbuffer.length > 0) {
						tempbuffer = waitForBytes(tempbuffer, PDUlength);
						PDUlength = checkReceivedMessage(tempbuffer);
					}
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
			
	}
	
	private int calculatePads(int length) {
		if(length % 4 == 0){
			return 0;
		} else {
			return 4 - (length % 4 );
		}
	}
	
	private int checkReceivedMessage(byte[] bytes) {
		int op = (int)PDU.byteArrayToLong(bytes, 0, 1);
		
		int nameLength;
		int messLength;
		int sequenceLength = 0;
		int pads=0;
		
		switch(op){
		
		case(MESS):
			
			nameLength = (int)PDU.byteArrayToLong(bytes, 2, 3);
			pads += calculatePads(nameLength);
			messLength = (int)PDU.byteArrayToLong(bytes, 4, 6);
			pads += calculatePads(messLength);
			
			sequenceLength = 12 + nameLength + messLength+pads;
			
			break;
		case(QUIT):
			
			sequenceLength = 4;
			
			break;
		case(UJOIN):
			
			nameLength = (int)PDU.byteArrayToLong(bytes, 1, 2);
			pads += calculatePads(nameLength);
		
			sequenceLength = 8 + nameLength + pads;
						
			break;
		case(ULEAVE):
			
			nameLength = (int)PDU.byteArrayToLong(bytes, 1, 2);
			pads += calculatePads(nameLength);
		
			sequenceLength = 8 + nameLength + pads;
			
			break;
		case(UCNICK):
			
			nameLength = (int)PDU.byteArrayToLong(bytes, 1, 2);
			pads += calculatePads(nameLength);

			int second = (int)PDU.byteArrayToLong(bytes, 2, 3);
			pads += calculatePads(second);

			
			sequenceLength = 8 + nameLength + second + pads;
			
			break;
		case(NICKS):
			
			nameLength = (int)PDU.byteArrayToLong(bytes, 2, 4);
			pads += calculatePads(nameLength);

			
			sequenceLength = 4 + nameLength + pads;
		
			break;
		default:
			sequenceLength = bytes.length;
			break;
		}
		return sequenceLength;

	}
	
	private void addNextMessage(byte[] bytes, int PDUlength) {
		messageQueue.add(Arrays.copyOfRange(bytes, 0, PDUlength));
	}


	private byte[] waitForBytes(byte[] bytes, int PDUlength) {
		byte[] tempbuffer = new byte[0];
		
		
		int len,lastByte;
		int missingBytes = PDUlength - bytes.length;
		byte[] returnBytes = new byte[missingBytes + bytes.length];
		lastByte = bytes.length;
		for(int j = 0; j < bytes.length; j++) {
			returnBytes[j] = bytes[j];
		}
		
		do {
			try {
				buffer = new byte[65535];
				len = inStream.read(buffer,0,PDUlength-lastByte);
				tempbuffer = new byte[len];
				
				for(int i = 0; i < len; i++ ) {
					tempbuffer[i] = buffer[i];
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			if(tempbuffer.length < missingBytes) {
				
				for(int i = 0; i < tempbuffer.length; i++) {
					returnBytes[lastByte+i] = tempbuffer[i];
				}
				lastByte += tempbuffer.length;
				missingBytes -= tempbuffer.length;
			} else {
				for(int i = 0; i < missingBytes; i++) {
					returnBytes[lastByte+i] = tempbuffer[i];
				}
				lastByte += missingBytes;
			}
		} while(lastByte < PDUlength);
			messageQueue.add(returnBytes);
			return Arrays.copyOfRange(tempbuffer, missingBytes, tempbuffer.length);
			
	}

	/**
	 * Sends a message over TCP.
	 * @param bytes - The message to send in bytes.
	 */
	
	private void sendTCP(byte[] bytes) {
		
		try {
			outStream.write(bytes);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Connect the clients socket to the server with the given
	 * port and Inet4Address.
	 * 
	 * @param tcpPort - The port to the server.
	 * @param ip - The address to the server.
	 * 
	 * @return  boolean - True if the client succeed to connect to the
	 * server else false.
	 */
	
	private boolean connectToTCP(int tcpPort,Inet4Address ip) {

		try{
			socket = new Socket("localhost",tcpPort);
			outStream = socket.getOutputStream();
			
			inStream = socket.getInputStream();

			gui.setConnected(true);
			
			gui.getStringFromClient("Sending request to join...");
			
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		JOIN join = new JOIN(name);		
		
		sendTCP(join.toByteArray());

		receiveTCP();
		
		if(PDU.byteArrayToLong(buffer, 0, 1) == 11) {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return true;
		} else {
			if(PDU.byteArrayToLong(buffer, 0, 1) == 10 &&
					checkIfOccupied()){
				
				return false;
			} else {
				gui.setSocket(socket);
				return true;
			}
		}
		
	}
	
	private boolean checkIfOccupied() {
		int server = (int)PDU.byteArrayToLong(buffer, 2, 3);
		int messLength = (int)PDU.byteArrayToLong(buffer, 4, 6);
		if(server==0){
			String message = PDU.stringReader(buffer, 12, messLength);
			return message.equals("Your nickname is occupied!");
		} else {
			return false;
		}
	}

	/**
	 * Connect to the name server with the given IP address 
	 * and creates a new multicastSocket with the field port.
	 * @param ip - The String representation of the address.
	 * 
	 * @return boolean - True if it succeed else false.
	 */

	private boolean connect(String ip) {
		try {
			address = InetAddress.getByName(ip);
			multicastSocket = new MulticastSocket(UDPport);
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
	 * Listens after messages in UDP from the name server.
	 * 
	 * @return byte[] - The message in bytes.
	 */
	
	private byte[] receive() {
		DatagramPacket packet = new DatagramPacket(buffer,buffer.length);
		while(true) {
			try {
				
				multicastSocket.receive(packet);
				return packet.getData();
				
			} catch (SocketTimeoutException e) {
				System.out.println("Time out reached!");
				e.printStackTrace();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}	
	}
	
	/**
	 * Sends a message over UDP.
	 * 
	 * @param data - The message to send in bytes.
	 */
	
	private void send(byte[] data) {
		DatagramPacket packet = new DatagramPacket
										(data,data.length,address,port);
		try {
			multicastSocket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * This method is called when the client thread starts.
	 * It will try to connect to a name server and when it
	 * is connected to a server it will only listens after messages
	 * from the server and check the message.
	 */
	
	public void run() {
		String message;
		int server=0;

		
		while(running) {
		
			if(gui.getUpdate()) {
				getlist();
				infoToClient();
				gui.setUpdate(false);
			}

			if(!gui.getQueue().isEmpty()) {
				message = gui.getQueue().removeFirst();
				if(isNumber(message)) {
					server = Integer.parseInt(message);
				}
			}
			
			if(server != 0 && server <= serverNames.size() ) {
				do {
					chooseNickName();
				
					gui.getStringFromClient("Your nickname is: "+name);
				
				} while(!connectToTCP(serverPort.get(server-1),
								adresses.get(server-1)));
				
				server = 0;
			}
			while(gui.getConnected()) {
				receiveTCP();
			}
		}
	}
	

	
	/**
	 * Closes the clients socket, outputStream, dataInput and
	 * inputStream.
	 */
	
	public synchronized void closeClientsSocket() {

		multicastSocket.close();
		try {
			outStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			inStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Checks if the given object is a number.
	 * 
	 * @param o - The object to check.
	 * 
	 * @return boolean - True if it is a number else false.
	 */
	
	private boolean isNumber(Object o){
	        boolean isNumber = true;

	        for( byte b : o.toString().getBytes() ){
	            char c = (char)b;
	            if(!Character.isDigit(c)) {
	                isNumber = false;
	            }
	        }

	        return isNumber;
	   }
	
	/**
	 * Lets the user chose a nickname, will wait until a nick
	 * name is chosen.
	 */
	
	private void chooseNickName(){
		gui.getStringFromClient("Chose your nickname!");
		
		boolean choosen = false;
		do{
			if(!gui.getQueue().isEmpty()){
				name = gui.getQueue().removeFirst();
				choosen = true;
			}
		}while(!choosen);
	}
	
	public synchronized LinkedList<byte[]> getMessageQueue(){
		return messageQueue;
	}
	
	@SuppressWarnings("unused")
	public static void main(String[] args){
		Random random = new Random();
		GUI client = new GUI(44400+random.nextInt(1000));
	}
}