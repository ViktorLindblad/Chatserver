package Server;
import java.io.*;
import java.net.Socket;
import java.util.LinkedList;

import PDU.PDU;


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

			try{
				inStream = socket.getInputStream();
				dataInput = new DataInputStream(inStream);
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("reading messages");
	       	int length;
			byte [] buffer = null;
			
			try {
				System.out.println("before");
				length = dataInput.readInt();
				System.out.println(length);
				System.out.println("after");
				buffer = new byte[length];
				buffer = PDU.readExactly(inStream, length);
				System.out.println("Message is read");
				
			} catch (IOException e) {
				e.printStackTrace();
			}   
			System.out.println("Sending message");
	        messageQueue.add(buffer); 
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