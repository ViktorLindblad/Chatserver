package Server;

import java.io.DataInputStream;
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
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import PDU.GETLIST;
import PDU.JOIN;
import PDU.PDU;

public class Client implements Runnable{
	
	private static final int MESS = 10, QUIT = 11, UJOIN = 16, ULEAVE = 17,
							UCNICK = 18, NICKS = 19, NICKO = 20; 
	
	private MulticastSocket multicastSocket;
	private Socket socket;
	
	private ArrayList<String> serverNames;
	private ArrayList<Integer> serverPort;
	private ArrayList<Inet4Address> adresses;
	private ArrayList<Integer> serverClients;
	private ArrayList<String> nickNames;
	
	private int port, tcpPort, sequenceNumber, servers;
	private InetAddress address;
	private GUI gui;
	private String name = "";
	private byte[] buffer;

	private boolean running ;
	
	private OutputStream outStream;
	private InputStream inStream;
	private DataInputStream dataInput;
	
	public Client(int tcpPort,  String ip,GUI gui, int port) {

		this.port = port;
		this.tcpPort = tcpPort;
		buffer = new byte[256];
		this.gui = gui;
		servers = 0;		
		
		System.out.println("port: "+this.port);
		System.out.println("TCPport"+this.tcpPort);
		System.out.println("local "+port);
		System.out.println("localtcp "+tcpPort);
		
		if(!connect(ip)){
			System.out.println("Connection failed");
		} else {
			multicastSocket.connect(address, port);
		}
		getlist();	

		infoToClient();
		running = true;
	}
	
	private void getlist(){
		byte[] message;
		
		serverNames = new ArrayList<String>();
		serverPort = new ArrayList<Integer>();
		adresses = new ArrayList<Inet4Address>();
		serverClients = new ArrayList<Integer>();
		nickNames = new ArrayList<String>();
		
		GETLIST getList = new GETLIST();
		
		send(getList.toByteArray());
		message = receive();

		if(PDU.byteArrayToLong(message,0,1) == 4){
			
			sequenceNumber = (int)PDU.byteArrayToLong(message,1,2);
			servers = (int)PDU.byteArrayToLong(message,2,4);
			System.out.println(servers);
			int byteIndex = 4;
			int tempint;
			byte[] tempbytes;
			
			for(int i = 0; i < servers; i++){
				tempbytes = null;
				tempbytes = Arrays.copyOfRange(message,byteIndex, byteIndex+4);

				byteIndex +=4;

				try {
					adresses.add((Inet4Address)Inet4Address.getByAddress(tempbytes));
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
				
				tempint = (int)PDU.byteArrayToLong(message,byteIndex,byteIndex+2);

				byteIndex += 2;
				serverPort.add(tempint);
				
				tempint =  (int)PDU.byteArrayToLong(message,byteIndex,byteIndex+1);

				byteIndex += 1;
				serverClients.add(tempint);
				
				tempint = (int)PDU.byteArrayToLong(message,byteIndex,byteIndex+1);

				byteIndex += 1;
				
				tempbytes = null;
				tempbytes = Arrays.copyOfRange(message,byteIndex, byteIndex+tempint);
				String server = PDU.bytaArrayToString(tempbytes,tempint);
				serverNames.add(server);

				if(tempint%4==0){
					byteIndex += tempint;
					
				} else if(tempint%4==1){
					byteIndex += tempint+3;
			
				} else if(tempint%4==2){
					byteIndex += tempint+2;
				
				} else if(tempint%4==3){
					byteIndex += tempint+1;
				
				}
			}
		}
	}
	
	private void infoToClient(){
		int index = 0;
		gui.getStringFromClient("Sequence number from Slist is: "+
		sequenceNumber);
		
		if(!serverNames.isEmpty()){
			for(String temp : serverNames){
				
				index++;
				gui.getStringFromClient("Server number: "+index);
				index--;
				
				gui.getStringFromClient("Server name: "+temp);
				gui.getStringFromClient("Address: "+adresses.get(index));
				gui.getStringFromClient("Port: "+serverPort.get(index));
				gui.getStringFromClient("Clients: "+serverClients.get(index)+"\n");
				index++;
				
			}
		} else{
			gui.getStringFromClient("No servers at this time");

		}
		
		gui.getStringFromClient("Please choose server to start chatting or update: \n");
		
	}
	

	
	
	private void receiveTCP() {
		long length;
		byte temp;
		buffer = null;
		
		try {
			temp = dataInput.readByte();
			length = ((long) temp) & 0xff;
			
			buffer = new byte[(int)length];
			buffer = PDU.readExactly(inStream, (int)length);			
		} catch (IOException e) {
			e.printStackTrace();
		}   
	}
	
	private void sendTCP(byte[] bytes) {
		
		try {
			outStream.write(bytes.length);
			outStream.write(bytes);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private boolean connectToTCP(int tcpPort,Inet4Address ip) {

		try{
			
			socket = new Socket("Localhost",tcpPort);
			outStream = socket.getOutputStream();
			
			inStream = socket.getInputStream();

			dataInput = new DataInputStream(inStream);
			gui.setConnected(true);
			
			gui.getStringFromClient("Sending request to join...");
			
		} catch(IOException e){
			e.printStackTrace();
		}
		
		JOIN join = new JOIN(name);		
		
		sendTCP(join.toByteArray());

		receiveTCP();
		if(PDU.byteArrayToLong(buffer, 0, 1)==20){
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return true;
		} else {
			checkMessage(buffer);
			gui.setSocket(socket);
			return false;
		}
		
	}

	private boolean connect(String ip){
		try {
			address = InetAddress.getByName(ip);
			multicastSocket = new MulticastSocket(44444);
			System.out.println(address);
		}	catch (SocketException e){
			e.printStackTrace();
			return false;
		}	catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private byte[] receive(){
		DatagramPacket packet = new DatagramPacket(buffer,buffer.length);
	while(true){
		try{
			
			multicastSocket.receive(packet);
			return packet.getData();
			
		} catch (SocketTimeoutException e){
			System.out.println("Time out reached!");
			e.printStackTrace();
		} catch (IOException e){
			e.printStackTrace();
		}
	}

		
	}
	
	private void send(byte[] data){
		DatagramPacket packet = new DatagramPacket(data,data.length,address,port);
		try {
			multicastSocket.send(packet);
			multicastSocket.setSoTimeout(5000);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void run() {
		String message;
		int server=0;

		
		while(running){
		
			if(gui.getUpdate()) {
				getlist();
				infoToClient();
				gui.setUpdate(false);
			}

			if(!gui.getQueue().isEmpty()) {
				message = gui.getQueue().removeFirst();
				if(message != ""){
					server = Integer.parseInt(message);
				}
			}
			if(server != 0 && server <= serverNames.size() ) {
				do {
					chooseNickName();
				
					gui.getStringFromClient("Your nickname is: "+name);
				
				} while(connectToTCP(serverPort.get(server-1),
								adresses.get(server-1)));
				
				server = 0;
			}
			while(gui.getConnected()) {
				receiveTCP();
				checkMessage(buffer);
			}
		}
	}
	
	private void checkMessage(byte[] bytes){
		int ca = (int)PDU.byteArrayToLong(bytes, 0, 1);
		int length;
		int time;
		String name = "";
		
		switch(ca){
			case(MESS):
				int nameLength = (int)PDU.byteArrayToLong(bytes, 2, 3);
				length = (int)PDU.byteArrayToLong(bytes, 4, 6);

				time = (int)PDU.byteArrayToLong(bytes, 8, 12);
				String message = PDU.stringReader(bytes, 12,length);
				if(length % 4 != 0) {
					length += 4 - ( length % 4);
				}
				String messname = PDU.stringReader(bytes, 12+length,nameLength);
				
				SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");    
				Date resultdate = new Date(time);
				
				String mess = sdf.format(resultdate)+": "+messname+" said: "+message;
				gui.getStringFromClient(mess);
				
			break;
			
			case(QUIT):
				gui.getStringFromClient("Server shutdown!");
				
			break;
			
			case(UJOIN):
				
				length = (int)PDU.byteArrayToLong(bytes, 1, 2);
				time = (int) PDU.byteArrayToLong(bytes, 4, 8);
				
				name = PDU.stringReader(bytes, 8, length);
				
				boolean don = true;
				for(String temp : nickNames){
					if(temp.equals(name)){
						don = false;
					}
				}
				if(don){
					nickNames.add(name);
					gui.getNameFromClient(nickNames);
				}
				
				
				gui.getStringFromClient(name+" joined chatroom at: " +time);
				
			break;
			
			case(ULEAVE):
				length = (int)PDU.byteArrayToLong(bytes, 1, 2);
			
				name = PDU.stringReader(bytes, 8, length);
			
				for(int i = 0; i < nickNames.size(); i++ ){
					if(nickNames.get(i).equals(name)){
						nickNames.remove(i);
					}
				}
				gui.getNameFromClient(nickNames);
				gui.getStringFromClient(name+" leaved the chatroom");
			break;
			
			case(UCNICK):
				length = (int)PDU.byteArrayToLong(bytes, 1, 2);
				int secondLength = (int)PDU.byteArrayToLong(bytes, 2, 3);
				time = (int)PDU.byteArrayToLong(bytes, 4, 8);
				
				name = PDU.stringReader(bytes, 8, length);
				if(length%4!=0){
					length += 4 - (length % 4);
				}
				String name2 = PDU.stringReader
							(bytes, 8+length, secondLength);

		
			for(int i = 0; i < nickNames.size(); i++ ){
				if(nickNames.get(i).equals(name)){
					nickNames.set(i, name2);
					gui.getNameFromClient(nickNames);
				}
			}
			String changeNick = "Time: "+String.valueOf(time)
					+"Old nick: "+name+"New nick: "+name2;
			
			gui.getStringFromClient(changeNick);
			
			
			break;
			
			case(NICKS):
				length = (int)PDU.byteArrayToLong(bytes, 1, 2);
				int index = 4;
				boolean condition;
				System.out.println("NICKS " +length);
				for(int i = 0; i < length; i++){
					condition = true;
					name = "";
					do{
						byte[] tempbyte = Arrays.copyOfRange
								(bytes, index, index+1);
						
						String character = PDU.bytaArrayToString
								(tempbyte, 1);
						
						if(character.equals("\0")){
							condition = false;
						} else {
							name += character;
						}
						
						index++;
					}while(condition);
					
					boolean dont = true;
					
					for(String temp : nickNames){
						
						if(name.equals(temp)){
							dont = false;
						}
					}

					if(dont){
						nickNames.add(name);
					}
				}

				gui.getNameFromClient(nickNames);

			break;
			
			case(NICKO):
				gui.getStringFromClient("Nickname occupied! Please try again \n");
			break;
			
			default:
			break;
		}
	}
	
	public boolean isNumber(Object o){
	        boolean isNumber = true;

	        for( byte b : o.toString().getBytes() ){
	            char c = (char)b;
	            if(!Character.isDigit(c)) {
	                isNumber = false;
	            }
	        }

	        return isNumber;
	   }
	
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
	
	@SuppressWarnings("unused")
	public static void main(String[] args){
		Random random = new Random();
		GUI client = new GUI(4400+random.nextInt(100));
		
		//Client client = new Client(1337,"itchy.cs.umu.se");
	}



}