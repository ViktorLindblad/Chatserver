package Server;
import java.io.IOException;
import java.net.*;
import java.util.LinkedList;

import PDU.ALIVE;
import PDU.PDU;


public class ServerConnector implements Runnable {

	private ServerSocket server;
	private LinkedList<Socket> queue;
	private boolean running;

	
	public ServerConnector(ServerSocket server){
		queue = new LinkedList<Socket>();
		this.server = server;
		running = true;

	}
	
	public void run(){
		while(running){
			
			System.out.println("listning after clients");
			
			try {
				queue.add(server.accept());
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			System.out.println("client joined server");
		}
	}


	public synchronized boolean getConnectorRunning(){
		return running;
	}
	
	public synchronized void setConnectorRunning(boolean condition){
		running = condition;
	}
	
	public synchronized LinkedList<Socket> getSocketQueue(){
		return queue;
	}
	
}
