package Server;
import java.io.*;
import java.net.Socket;
import java.util.LinkedList;


public class ServerMessageHandler implements Runnable {
	
	private LinkedList <byte[]> messageQueue; 
	private Socket socket;
	private InputStream inStream;
	private DataInputStream dataInput;
	private boolean running;
	
	public ServerMessageHandler(Socket socket){
		
		this.socket = socket;
		messageQueue = new LinkedList <byte[]>();
		running = true;
	}

	public void run() {
		
		while(running){
			System.out.println("a message handler is running");
			ByteSequenceBuilder BSB = new ByteSequenceBuilder();
			try{
				inStream = socket.getInputStream();
				dataInput = new DataInputStream(inStream);
			} catch (IOException e) {
				e.printStackTrace();
			}
	       System.out.println("reading messages");
			try {
				while(dataInput.read() != -1){
					BSB.append(dataInput.readByte());
				}
				System.out.println("message received");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		    
	        messageQueue.add(BSB.toByteArray()); 
		}
	}
	
	public synchronized void setMessageRunning(boolean bool){
		running = bool;
	}
	
	public synchronized boolean getMessegeRunning(){
		return running;
	}
	
	public synchronized LinkedList <byte[]> getMessageQueue(){
		return messageQueue;
	}
	
	public synchronized Socket getSocket(){
		return socket;
	}
}