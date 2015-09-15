
import java.io.*;
import java.net.*;

public class Client extends PDU implements Runnable{
	
	private MulticastSocket multicastSocket;
	private Socket socket;
	
	private Thread thread;
	private int port;
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

		
		if(!connect(ip,port)){
			System.out.println("Connection failed");
		} else {
			System.out.println(address);
			multicastSocket.connect(address, port);
		}
		
		System.out.println(multicastSocket.isConnected());
		
		ByteSequenceBuilder BSB = new ByteSequenceBuilder();
		BSB.append(OpCode.GETLIST.value);
		BSB.pad();
		System.out.println(BSB.size());
		System.out.println("sending "+BSB.toByteArray());
		send(BSB.toByteArray());
		System.out.println(receive());
		
		
		try{
			socket = new Socket("localhost",5);
			
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
		System.out.println("receiving");
		try{
			multicastSocket.receive(packet);
		} catch (IOException e){
			e.printStackTrace();
		}
		String message = new String(packet.getData());
		System.out.println("message is received");

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
		

		while(name.equals("")){
			
			if(!login.getQueue().isEmpty()){
				
				name = login.getQueue().remove();
			}
		}
		
	    

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
