package Server;
import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.LinkedList;

import PDU.PDU;

/**
 * MessageHandler listens after messages sent from the given socket.
 */

public class MessageHandler implements Runnable {
	
	private static final int MESS = 10, QUIT = 11,  JOIN = 12, 
				CHNICK = 13,UJOIN = 16,ULEAVE = 17, UCNICK = 18,
				NICKS = 19;
	
	private LinkedList <byte[]> messageQueue; 
	private Socket socket;
	private InputStream inStream;
	private boolean running, join;
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
		join = false;
		buffer = new byte[65535];
		
		try{
			inStream = socket.getInputStream();
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

			try {
				int len;
				byte[] tempbuffer = new byte[0];
				buffer = new byte[65535];
				len = inStream.read(buffer);
				tempbuffer = new byte[len];
				
				for(int i = 0; i < len; i++ ) {
					tempbuffer[i] = buffer[i];
				}
				
				
				int bytesread = checkReceivedMessage(tempbuffer);
				
				while((bytesread - tempbuffer.length) != 0) {
					
					if(bytesread - tempbuffer.length < 0) {
						
						addNextMessage(tempbuffer,bytesread);
						tempbuffer = Arrays.copyOfRange(tempbuffer,
										bytesread, tempbuffer.length);
						
						bytesread = checkReceivedMessage(tempbuffer);
						
					} else if(bytesread - tempbuffer.length > 0) {
						tempbuffer = waitForBytes(tempbuffer, bytesread);
						bytesread = checkReceivedMessage(tempbuffer);
					}
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			if(PDU.byteArrayToLong(buffer, 0, 1)==11) {
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
			messageQueue.add(buffer);
		}
	}
	
	private byte[] waitForBytes(byte[] bytes, int PDUlength) {
		byte[] tempbuffer = new byte[0];
		
		
		int len,lastByte;
		int missingBytes = PDUlength - bytes.length;
		byte[] returnBytes = new byte[missingBytes + bytes.length];
		lastByte = bytes.length;
		for(int j = 0; j < bytes.length; j++) {
			returnBytes[j] = bytes[j];
		}
		
		do {
			try {
				buffer = new byte[65535];
				len = inStream.read(buffer,0,PDUlength-lastByte);
				tempbuffer = new byte[len];
				
				for(int i = 0; i < len; i++ ) {
					tempbuffer[i] = buffer[i];
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			if(tempbuffer.length < missingBytes) {
				
				for(int i = 0; i < tempbuffer.length; i++) {
					returnBytes[lastByte+i] = tempbuffer[i];
				}
				lastByte += tempbuffer.length;
				missingBytes -= tempbuffer.length;
			} else {
				for(int i = 0; i < missingBytes; i++) {
					returnBytes[lastByte+i] = tempbuffer[i];
				}
				lastByte += missingBytes;
			}
		} while(lastByte < PDUlength);
			messageQueue.add(returnBytes);
			
			return Arrays.copyOfRange(tempbuffer, missingBytes, tempbuffer.length);
			
	}
		

	private void addNextMessage(byte[] bytes, int PDUlength) {
		messageQueue.add(Arrays.copyOfRange(bytes, 0, PDUlength));
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
	
	private int checkReceivedMessage(byte[] bytes) {
		int op = (int)PDU.byteArrayToLong(bytes, 0, 1);
		
		int nameLength;
		int messLength;
		int sequenceLength = 0;
		int pads=0;
		
		switch(op){
		
		case(MESS):
			
			nameLength = (int)PDU.byteArrayToLong(bytes, 2, 3);
			pads += calculatePads(nameLength);
			messLength = (int)PDU.byteArrayToLong(bytes, 4, 6);
			pads += calculatePads(messLength);
			
			sequenceLength = 12 + nameLength + messLength+pads;
			
			break;
		case(QUIT):
			
			sequenceLength = 4;
			
			break;
		case(UJOIN):
			
			nameLength = (int)PDU.byteArrayToLong(bytes, 1, 2);
			pads += calculatePads(nameLength);
		
			sequenceLength = 8 + nameLength + pads;
						
			break;
		case(ULEAVE):
			
			nameLength = (int)PDU.byteArrayToLong(bytes, 1, 2);
			pads += calculatePads(nameLength);
		
			sequenceLength = 8 + nameLength + pads;
			
			break;
		case(UCNICK):
			
			nameLength = (int)PDU.byteArrayToLong(bytes, 1, 2);
			pads += calculatePads(nameLength);

			int second = (int)PDU.byteArrayToLong(bytes, 2, 3);
			pads += calculatePads(second);

			
			sequenceLength = 8 + nameLength + second + pads;
			
			break;
		case(NICKS):
			
			nameLength = (int)PDU.byteArrayToLong(bytes, 2, 4);
			pads += calculatePads(nameLength);

			
			sequenceLength = 4 + nameLength + pads;
		
			break;
		default:
			
			break;
		}
		return sequenceLength;

	}

	private int calculatePads(int length) {
		if(length % 4 == 0){
			return 0;
		} else {
			return 4 - (length % 4 );
		}
	}


	
	public synchronized Socket getSocket() {
		return socket;
	}
	
	public synchronized boolean getHasJoin() {
		return join;
	}
	
	public synchronized void setHasJoin(boolean condition) {
		join = condition;
	}
	
}