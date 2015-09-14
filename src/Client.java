import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.MulticastSocket;
import java.net.Socket;
import java.net.SocketAddress;

import javax.swing.JFrame;
import javax.swing.JTextField;



public class Client extends Thread {
	
	private MulticastSocket multicastSocket;
	private Socket socket;
	
	private GUI gui;
	private String string = "";

	private boolean connected = true;
	private PrintWriter out;
	private BufferedReader in;
	private BufferedReader keyReader;
	private OutputStream outStream;
	private InputStream inStream;
	
	
	//client is the name we use
	
	public Client(int port){
		super("a Client Thread");
						
		gui = new GUI(this);
		
		try{
			socket = new Socket("localhost",port);
			keyReader = new BufferedReader(new InputStreamReader(System.in));
			
			outStream = socket.getOutputStream();
			out = new PrintWriter(outStream, true);
			
			inStream = socket.getInputStream();
	        in = new BufferedReader(new InputStreamReader(inStream));
		} catch(IOException e){
			e.printStackTrace();
		}
		
		start(); // calls run
	}

	public Client(int port, SocketAddress IP){
		super("a Client Thread");
	try {
		multicastSocket = new MulticastSocket(port);
	} catch (IOException e) {
		e.printStackTrace();
		}
	}
	
	public void run(){
		
		//Skicka namn till servern
		
		while(connected){
			
			
	        String receiveMessage;
	        //sendMessage = Write.getText();
			if(string != null){
				out.println(string);
		        out.flush();
		        //string = "";
			}
	        /*try {
				//sendMessage = keyReader.readLine();
	        	out.println(sendMessage);
		        out.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}*/
	 
	        try{
	        	if((receiveMessage = in.readLine()) != null){
	        		System.out.println(receiveMessage);
	        		gui.getStringFromClient(receiveMessage);
	        		
	        	}
	        } catch (IOException e){
	        	e.printStackTrace();
	        }
		}
	}
	
	public void getStringFromGUI(String text) {
		if(text != ""){
			string = text;
		}
	}
	
	public static void main(String[] args){
		Client client = new Client(45);
	}
}
