package Server;
import java.io.*;
import java.net.Socket;
import java.util.LinkedList;

import PDU.PDU;


public class MessageHandler implements Runnable {
	
	private LinkedList <byte[]> messageQueue; 
	private Socket socket;
	private InputStream inStream;
	private DataInputStream dataInput;
	private boolean running;
	
	public MessageHandler(Socket socket){
		
		this.socket = socket;
		messageQueue = new LinkedList <byte[]>();
		running = true;
	}

	public void run() {

		while(running){
			
			
			try{
				inStream = socket.getInputStream();
				dataInput = new DataInputStream(inStream);
			} catch (IOException e) {
				e.printStackTrace();
			}

			int length;
			byte [] buffer = null;
			
			try {
				length = dataInput.readByte();
				buffer = new byte[length];
				buffer = PDU.readExactly(inStream, length);
				
			} catch (IOException e) {
				e.printStackTrace();
			}   
						
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