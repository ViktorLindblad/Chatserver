
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;

public class Client extends PDU implements Runnable{
	
	private MulticastSocket multicastSocket;
	private Socket socket;
	
	private ArrayList<String> serverNames;
	private ArrayList<Integer> serverPort;
	private ArrayList<InetAddress> adresses;


	private Thread thread;
	private int port, sequenceNumber;
	private InetAddress address;
	private GUI gui,login;
	private String name = "";
	private byte[] buffer;

	private boolean connected = true;
	private PrintWriter out;
	private BufferedReader in;
	private OutputStream outStream;
	private InputStream inStream;	
	
	//client is the name we use
	
	public Client(int port,  String ip) {
		thread = new Thread("A Client Thread");
		login = new GUI(360,360);
		this.port = port;
		buffer = new byte[256];
		gui = new GUI();
		
		serverNames = new ArrayList<String>();
		serverPort = new ArrayList<Integer>();

		adresses = new ArrayList<InetAddress>();
		
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
			int servers = (int)PDU.byteArrayToLong(message,2,4);
			
			int adress = 4;
			byte[] tempstring;
			System.out.println(servers);
			for(int i = 0; i < servers; i++){
				
				tempstring = Arrays.copyOfRange(message,adress, adress+2);
				String server = new String(tempstring);
				adress+=2;
				tempstring = Arrays.copyOfRange(message,adress, adress+2);
				server += new String(tempstring);
				System.out.println(server);
				adress+=2;
				try {
					adresses.add(InetAddress.getByName(server));
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
				System.out.println(adress);
				
				int tempint = (int)PDU.byteArrayToLong(message,adress,adress+2);
				adress += 2;
				serverPort.add(tempint);
				
				tempint =  (int)PDU.byteArrayToLong(message,adress,adress+1);
				adress += 1;
				serverPort.add(tempint);
				
				tempint = (int)PDU.byteArrayToLong(message,adress,adress+1);
				adress += 1;
				tempstring = Arrays.copyOfRange(message,adress, adress+2);
				adress+=2;
				server = new String(tempstring);
				tempstring = Arrays.copyOfRange(message, adress, adress+2);
				server += new String(tempstring);
				serverNames.add(server);
			}
			for(String temp : serverNames){
				System.out.println("serverName: "+temp);
			}
			
			
		}
		
		try{
			socket = new Socket("localhost",111);
			outStream = socket.getOutputStream();
			out = new PrintWriter(outStream, true);
			
			inStream = socket.getInputStream();
	        in = new BufferedReader(new InputStreamReader(inStream));
	        
		} catch(IOException e){
			e.printStackTrace();
		}
		
		thread.start(); // calls run
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
	
	public byte[] toByteArray() {
		return buffer;
	}

	
	@SuppressWarnings("unused")
	public static void main(String[] args){

		
		Client client = new Client(1337,"itchy.cs.umu.se");
	}



}
