import java.io.*;
import java.net.Socket;
import java.util.LinkedList;


public class ServerMessageHandler implements Runnable {
	
	private LinkedList <String> messageQueue; 
	private Socket socket;
	private InputStream inStream;
	private BufferedReader buffRead;
	private boolean running;
	
	public ServerMessageHandler(Socket socket){
		
		this.socket = socket;
		messageQueue = new LinkedList <String>();
		
	}

	@Override
	public void run() {
		
		while(running){
			
			try{
				inStream = socket.getInputStream();
			} catch (IOException e) {
				e.printStackTrace();
			}
	       
			buffRead = new BufferedReader(new InputStreamReader(inStream));
		    
	        try {
				messageQueue.add(buffRead.readLine());
			} catch (IOException e) {
				e.printStackTrace();
			} 
		}
	}
	
	public synchronized void setMessageRunning(boolean bool){
		running = bool;
	}
	
	public synchronized boolean getMessegeRunning(){
		return running;
	}
	
	public synchronized LinkedList <String> getMessageQueue(){
		return messageQueue;
	}
}