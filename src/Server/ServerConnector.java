package Server;
import java.io.IOException;
import java.net.*;
import java.util.LinkedList;

/**
 * ServerConnector listens after clients to join the server, if 
 * a client joins ServerConnector puts the clients socket in 
 * it's queue so that the server can add it.
 */

public class ServerConnector implements Runnable {

	private ServerSocket server;
	private LinkedList<Socket> queue;
	private boolean running;

	/**
	 * Creates a new ServerConnector.
	 * 
	 * @param server - The serverSocket where it listens after clients.
	 */
	public ServerConnector(ServerSocket server){
		queue = new LinkedList<Socket>();
		this.server = server;
		running = true;

	}
	
	/**
	 * Thread run method, it runs when this thread.start() is called.
	 */
	
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

	/**
	 * Returns this ServerConnector's state, if it runs or not.
	 * 
	 * @return running - true if the ServerConnector is running else false.
	 */
	
	public synchronized boolean getConnectorRunning(){
		return running;
	}
	
	/**
	 * Sets this ServerConnector's state.
	 * @param condition - a boolean to set the state.
	 */
	
	public synchronized void setConnectorRunning(boolean condition){
		running = condition;
	}
	
	/**
	 * Gets ServerConnector's queue.
	 * 
	 * @return queue - The queue where all clients sockets will be
	 * put when they connect.
	 */
	
	public synchronized LinkedList<Socket> getSocketQueue(){
		return queue;
	}
	
}
