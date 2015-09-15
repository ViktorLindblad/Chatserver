
import java.io.*;
import java.net.*;

public class Client extends PDU implements Runnable{
	
	@SuppressWarnings("unused")
	private MulticastSocket multicastSocket;
	private Socket socket;
	
	private Thread thread;
	private int port;
	private InetAddress address;
	private GUI gui;
	private String name;
	private byte[] buffer;

	private boolean connected = true;
	private PrintWriter out;
	private BufferedReader in;
	private OutputStream outStream;
	private InputStream inStream;	
	
	//client is the name we use
	
	public Client(int port,  String ip, String name){
		thread = new Thread("A Client Thread");
		
		this.port = port;
		buffer = new byte[256];
		
		if(!connect(ip,port)){
			System.out.println("Connection failed");
		} else {
			System.out.println(address);
			try {
				multicastSocket.joinGroup(address);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		ByteSequenceBuilder BSB = new ByteSequenceBuilder();
		BSB.append(OpCode.GETLIST.value);
		
		send(BSB.toByteArray());
		System.out.println(receive());
		/*
		try{
			socket = new Socket("localhost",port);
			
			outStream = socket.getOutputStream();
			out = new PrintWriter(outStream, true);
			
			inStream = socket.getInputStream();
	        in = new BufferedReader(new InputStreamReader(inStream));
	        
		} catch(IOException e){
			e.printStackTrace();
		}
		
		thread.start(); // calls run
		*/
	}

	private boolean connect(String ip, int port){
		try {
			multicastSocket = new MulticastSocket(0);
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
	
	private String receive(){
		DatagramPacket packet = new DatagramPacket(buffer,buffer.length);
		try{
			multicastSocket.receive(packet);
		} catch (IOException e){
			e.printStackTrace();
		}
		String message = new String(packet.getData());
		return message;
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
		while(name == null){
			if(!login.getQueue().isEmpty()){
				name = login.getQueue().remove();
				out.println(name);
				out.flush();
				try{
		        	if((receiveMessage = in.readLine()) != null){
		        	}
		        } catch (IOException e){
		        	e.printStackTrace();
		        }
				
				login.getNameFromClient(name);
			}
		}*/
		gui = new GUI();

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
		GUI login = new GUI(360,360);
		String name = "";
		Client client = new Client(1337,"itchy.cs.umu.se",name);
	}



}
