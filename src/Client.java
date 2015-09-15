
import java.io.*;
import java.net.*;

public class Client extends PDU implements Runnable{
	
	@SuppressWarnings("unused")
	private MulticastSocket multicastSocket;
	private Socket socket;
	
	private Thread thread;
	
	private GUI gui;
	private GUI login;
	private String name;

	private boolean connected = true;
	private PrintWriter out;
	private BufferedReader in;
	private OutputStream outStream;
	private InputStream inStream;	
	
	//client is the name we use
	
	public Client(int port){
		thread = new Thread("A Client Thread");

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
	}

	public Client(int port, SocketAddress IP){
		thread = new Thread("A Client Thread");
	try {
		multicastSocket = new MulticastSocket(port);
	} catch (IOException e) {
		e.printStackTrace();
		}
		thread.start();
	}
	
	public void run(){
	    String receiveMessage = "";
		login = new GUI(360,360);
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
	
	@SuppressWarnings("unused")
	public static void main(String[] args){
		
		Client client = new Client(45);
	}

	@Override
	public byte[] toByteArray() {
		// TODO Auto-generated method stub
		return null;
	}
}
