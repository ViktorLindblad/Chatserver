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
	
	private Graphics g;
	private JTextField Chat;
	private JTextField Write;
	private JTextField Connecteds;

	private boolean connected = true;
	private PrintWriter out;
	private BufferedReader in;
	private BufferedReader keyReader;
	private OutputStream outStream;
	private InputStream inStream;
	
	private JFrame frame;
	
	//client is the name we use
	
	public Client(int port){
		super("a Client Thread");
			
		createFrame();
		
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
	
	private void createFrame() {
		frame= new JFrame("Client");
		frame.setSize(620,360);
	
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		
		g = frame.getGraphics();
		
		Chat = new JTextField(15);
		Write = new JTextField(5);
		Connecteds = new JTextField(20);
		
		Dimension d1 = new Dimension(500,200);
		Dimension d2 = new Dimension(120,200);
		Dimension d3 = new Dimension(620,160);
		Chat.setSize(d1);
		Write.setSize(d3);
		Connecteds.setSize(d2);
		frame.add(Chat);
		frame.add(Write);
		frame.add(Connecteds);
		
		
		
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
		while(connected){
			
			
	        String sendMessage, receiveMessage;
	        System.out.println(Write.getText().length());
	        sendMessage = Write.getText();
	        
			out.println(sendMessage);
	        out.flush();
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
	    			render(g,receiveMessage);

	        	}
	        } catch (IOException e){
	        	e.printStackTrace();
	        }
	        
	        
		}
	}
	
	public void render(Graphics g, String str){
		
		g.setColor(Color.PINK);
		g.fillRect(0, 0, 620, 360);
		g.setColor(Color.RED);
		g.fillRect(0, 0, 400, 200);
		g.setColor(Color.BLACK);
		g.drawString(str, 330, 180);
	}
	
	public static void main(String[] args){
		Client client = new Client(45);
	}
	
}
