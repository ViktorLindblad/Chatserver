package Server;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;

import PDU.PDU;

public class Client implements Runnable{
	
	private MulticastSocket multicastSocket;
	private Socket socket;
	
	private ArrayList<String> serverNames;
	private ArrayList<Integer> serverPort;
	private ArrayList<Inet4Address> adresses;

	
	private DataInputStream dataInput;

	private Thread thread;
	private int port, sequenceNumber, servers;
	private InetAddress address;
	private GUI gui,login;
	private String name = "";
	private byte[] buffer;

	private boolean connected = true;
	private PrintWriter out;
	private BufferedReader in;
	private OutputStream outStream;
	private InputStream inStream;
	private DataOutputStream dataOutput;
	
	//client is the name we use
	
	public Client(int port,  String ip) {
		thread = new Thread("A Client Thread");
		//login = new GUI(360,360);
		this.port = port;
		buffer = new byte[256];
		gui = new GUI();
		
		servers = 0;
		
		serverNames = new ArrayList<String>();
		serverPort = new ArrayList<Integer>();

		adresses = new ArrayList<Inet4Address>();
		
		if(!connect(ip,port)){
			System.out.println("Connection failed");
		} else {
			multicastSocket.connect(address, port);
		}
				
		byte[] message;
		
		

		byte[] getlist = new ByteSequenceBuilder(OpCode.GETLIST.value).pad()
				.toByteArray();
		
		send(getlist);
		message = receive();

		if(PDU.byteArrayToLong(message,0,1) == 4){
			
			sequenceNumber = (int)PDU.byteArrayToLong(message,1,2);
			servers = (int)PDU.byteArrayToLong(message,2,4);
			int byteIndex = 4;
			int tempint;
			byte[] tempbytes;
			
			for(int i = 0; i < servers; i++){
				tempbytes = null;
				tempbytes = Arrays.copyOfRange(message,byteIndex, byteIndex+4);

				byteIndex +=4;

				try {
					adresses.add((Inet4Address) Inet4Address.getByAddress(tempbytes));
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
				
				tempint = (int)PDU.byteArrayToLong(message,byteIndex,byteIndex+2);

				byteIndex += 2;
				serverPort.add(tempint);
				
				tempint =  (int)PDU.byteArrayToLong(message,byteIndex,byteIndex+1);

				byteIndex += 1;
				serverPort.add(tempint);
				
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
			
		infoToClient();
			/*
			for(String temp : serverNames){
				System.out.println("serverName: "+temp);
			}
			for(Inet4Address temp : adresses){
				System.out.println(temp);
			}
			for(int temp : serverPort){
				System.out.println(temp);
			}*/
		}
		thread.start(); // calls run

	}
	
	private void infoToClient(){
		int index = 0;
		int intIndex = 0;
		if(!serverNames.isEmpty()){
			for(String temp : serverNames){
				index++;
				gui.getStringFromClient("Server number: "+index);
				index--;
				gui.getStringFromClient("Server name: "+serverNames.get(index));
				gui.getStringFromClient("Address: "+adresses.get(index));
				gui.getStringFromClient("Port: "+serverPort.get(intIndex));
				intIndex++;
				gui.getStringFromClient("Clients: "+serverPort.get(intIndex)+"\n");
				index++;
				intIndex++;
			}
		} else{
			gui.getStringFromClient("No servers at this time");

		}
		gui.getStringFromClient("Please choose server to start chatting: ");
		
		getChoiceFromGUI();
	}
	
	private void getChoiceFromGUI(){
		do{
			if(!gui.getQueue().isEmpty()) {

			}
			
		}while(gui.getQueue().isEmpty());
	}
	
	
	private byte[] receiveTCP() {

		byte[] message = null;
		
		try {
			int length = dataInput.readInt();
			if(length > 0){
				message = new byte[length];
				dataInput.readFully(message, 0, message.length);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return message;
	}
	
	private void sendTCP(byte[] bytes) {
		
		try {
			dataOutput.writeInt(bytes.length);
			dataOutput.write(bytes);
			dataOutput.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private void connectToTCP(int port,Inet4Address ip) {
		
		try{
			socket = new Socket(ip,port);
			outStream = socket.getOutputStream();
			out = new PrintWriter(outStream, true);
			
			inStream = socket.getInputStream();
	        in = new BufferedReader(new InputStreamReader(inStream));
			dataInput = new DataInputStream(inStream);
			dataOutput = new DataOutputStream(outStream);

		} catch(IOException e){
			e.printStackTrace();
		}
		
	}

	private boolean connect(String ip, int port){
		try {
			multicastSocket = new MulticastSocket(1);
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
	
	private byte[] receive(){
		DatagramPacket packet = new DatagramPacket(buffer,buffer.length);
		try{
			multicastSocket.receive(packet);
		} catch (IOException e){
			e.printStackTrace();
		}

		return packet.getData();
	}
	
	private void send(byte[] data){
		DatagramPacket packet = new DatagramPacket(data,data.length,address,port);
		try {
			multicastSocket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void run(){
	    String receiveMessage = "";
		
	    /*
		while(name.equals("")){
			
			if(!login.getQueue().isEmpty()){
				
				name = login.getQueue().remove();
			}
		}*/
		

		while(connected){
			
	        if(!gui.getQueue().isEmpty()){
				out.println(gui.getQueue().remove());
		        out.flush();
			} else {
				out.println("");
				out.flush();
			}

	        try{
	        	if((receiveMessage = in.readLine()) != null){
	        	}
	        } catch (IOException e){
	        	e.printStackTrace();
	        }
	        
	        if(receiveMessage.length() > 0){
        		gui.getStringFromClient(receiveMessage);
	        }
	        
		}
	}
	
	@SuppressWarnings("unused")
	public static void main(String[] args){
		
		Client client = new Client(1337,"itchy.cs.umu.se");
	}



}
