package Server;
import java.io.*;
import java.net.Socket;
import java.util.LinkedList;

import PDU.PDU;

/**
 * MessageHandler listens after messages sent from the given socket.
 */

public class MessageHandler implements Runnable {
	
	private LinkedList <byte[]> messageQueue; 
	private Socket socket;
	private InputStream inStream;
	private DataInputStream dataInput;
	private boolean running;
	private Thread thread;
	private byte[] buffer;
	
	/**
	 * Creates a new MessageHandler and tries to get the inputStream
	 * from the given socket. It also starts a new thread where it
	 * will listens after messages.
	 * 
	 * @param socket - The socket which it listens after messages. 
	 */
	
	public MessageHandler(Socket socket) {
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

	/**
	 * MessageHandler thread's run method. Here it listens after 
	 * messages from the socket and adds it to the queue.
	 * If a QUIT PDU is sent the messageHandler closes the socket, 
	 * inputStream, dataInput and wait's to die.
	 */
	
	public void run() {

		while(running) {

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
			if(PDU.byteArrayToLong(buffer, 0, 1)==11) {
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
	
	/**
	 * Sets MessageHandler's state. 
	 * 
	 * @param bool - boolean, determines if the MessageHandler will run
	 * or not. 
	 */
	
	public synchronized void setMessageRunning(boolean bool) {
		running = bool;
	}
	
	/**
	 * Gets MessageHandler's state.
	 * 
	 * @return running - boolean, true if MessageHandler is running
	 * else false.
	 */
	
	public synchronized boolean getMessegeRunning() {
		return running;
	}
	
	/**
	 * Gets MessageHandler's message queue. 
	 * 
	 * @return messageQueue - The queue where all messages will be put
	 * when they arrives from this socket.
	 */
	
	public synchronized LinkedList <byte[]> getMessageQueue() {
		return messageQueue;
	}
	
	/**
	 * Gets the socket which MessageHandler listens after messages.
	 * 
	 * @return socket - The socket which MessageHandler listens after
	 * messages.
	 */
	
	public synchronized Socket getSocket() {
		return socket;
	}
}