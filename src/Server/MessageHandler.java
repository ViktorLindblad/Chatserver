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
	private Thread thread;
	private boolean correct;
	
	public MessageHandler(Socket socket){
		correct = true;
		this.socket = socket;
		messageQueue = new LinkedList <byte[]>();
		running = true;
		thread = new Thread(this);
		thread.start();
	}

	public void run() {

		while(running){

			if(!receivedCorrectMessage()){
				

	        	try {
	                dataInput.close();
	            } catch (IOException e) {
	            	e.printStackTrace();
	            }
	    		try {
	    			inStream.close();
	    		} catch (IOException e) {
	    			e.printStackTrace();
	    		}
	    		try {
	    			socket.close();
	    		} catch (IOException e) {
	    			e.printStackTrace();
	    		}
	    		try {
					thread.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
	        }
		
			
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
	
	public synchronized void notifyThread(){
		thread.notify();
	}
	
	private synchronized boolean receivedCorrectMessage() {
		return correct;
	}
	
	public synchronized void setReceivedCorrectMessage(boolean condition) {
		correct  = condition;
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