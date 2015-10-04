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
	private byte[] buffer;
	
	public MessageHandler(Socket socket){
		this.socket = socket;
		messageQueue = new LinkedList <byte[]>();
		running = true;
		
		try{
			inStream = socket.getInputStream();
			dataInput = new DataInputStream(inStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		thread = new Thread(this);
		thread.start();
		
		
	}

	public void run() {

		while(running){

			long length;
			byte temp;
			System.out.println("message Queue running");
			try {
				buffer = null;
				
				temp = dataInput.readByte();
				length = ((long) temp) & 0xff;
				
				
				buffer = new byte[(int)length];
				buffer = PDU.readExactly(inStream, (int)length);
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			if(PDU.byteArrayToLong(buffer, 0, 1)==11){
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
				messageQueue.add(buffer);
	    		try {
					thread.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
							     
			messageQueue.add(buffer);

			
		}
	}
	
	public synchronized void notifyThread(){
		thread.notify();
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