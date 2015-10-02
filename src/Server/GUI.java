package Server;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

import PDU.CHNICK;
import PDU.MESS;
import PDU.QUIT;


public class GUI implements ActionListener, Runnable{

	private JPanel panel;

	private boolean connected, updateServers, quitserver, running,
					clientconnect;
	
	private JButton button;
	private JButton connect;
	private JButton update;
	private JButton quit;
	private JButton changeNick;
	
	private LinkedList<String> queue;
	private LinkedList<String> nickQueue;
	
	private String chatString = "";
	private String name = "", address;
	private String clientNames = "";

	private Socket socket;
	private OutputStream outStream;
	
	private JFrame frame;
	
	private int port;
	private int TCPport;
	
	private JTextArea chatbox;
	private JTextField message;
	private JTextArea clients;
	private JScrollPane chatScroll;
	private JScrollPane clientsScroll;
	
	private Thread clientThread;
	
	
	
	public GUI(int TCPport){
		this.TCPport = TCPport;
		socket = null;
		updateServers = false;
		
		queue = new LinkedList<String> ();
		nickQueue = new LinkedList<String> ();


		frame = new JFrame("client");
		message = new JTextField();
		
		chatbox = new JTextArea();
		clients = new JTextArea();
		
		panel = new JPanel();
		button = new JButton("Send message");
		update = new JButton("Update");
		connect = new JButton("Connect");
		
		quit = new JButton("Quit");
		changeNick = new JButton("Change nickname");
		
		chatScroll = new JScrollPane(chatbox);
		clientsScroll = new JScrollPane(clients);
		
		frame.add(panel);

		panel.add(message);
		
		panel.add(chatScroll);
		panel.add(clientsScroll);
		
		panel.add(quit);
		panel.add(changeNick);
		
		panel.add(clients);
		panel.add(button);
		panel.add(update);
		panel.add(connect);

		frame.setSize(620,360);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		
		frame.setVisible(true);
		
		//g = getGraphics();
		panel.setLayout(null);
		panel.setBackground(Color.PINK);
		
		chatbox.setBounds(0,0,400,200);
		message.setBounds(0,250,400,30);
		
		chatbox.setEditable(false);
		chatbox.setFont(new Font("Century Gothic", Font.PLAIN, 12));
		
		clients.setBounds(450,0,150,200);
		chatScroll.setBounds(0,0,400,200);
		
		
		chatbox.setBackground(Color.MAGENTA);
		clients.setBackground(Color.magenta);
		
		chatScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		clientsScroll.setBounds(450,0,150,200);
		
		clientsScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		button.setBounds(250,290,150,32);
		
		connect.setBounds(50, 290, 150, 32);
		
		update.setBounds(450,210,150,32);
		
		
		quit.setBounds(450, 290, 150, 32);
		changeNick.setBounds(450, 250, 150, 32);
		
		button.addActionListener(this);
		update.addActionListener(this);
		
		connect.addActionListener(this);
		

		quit.addActionListener(this);
		changeNick.addActionListener(this);
		running = true;
		clientconnect = true ;
		
		new Thread(this).start();
		
	}

	
	public synchronized LinkedList<String> getQueue(){
		return queue;
	}
	
	private void addStringToQueue(String string){
		queue.add(string);
	}

	public void actionPerformed(ActionEvent e) {
		
		if(e.getSource() == connect){
			
			if(clientconnect) {
				
				Client client = new  Client(port,address,this,TCPport);
				clientThread = new Thread(client);
				clientThread.start();
				clientconnect = false;
			}
			
		}
		
		if(e.getSource() == update){
			setUpdate(true);
		}
		
		if(e.getSource() == quit){
			setQuit(true);
			clientconnect = true;
			connected = false;
			clientThread = null;
		}
		
		if(e.getSource() == changeNick){
			if(message.getText() != ""){
				addStringToNickQueue(message.getText());
				message.setText("");
			}
		}
		
		if(e.getSource() == button){
			if(message.getText() != ""){
				addStringToQueue(message.getText());
				message.setText("");
			}
		}
		
	}
	
	private void addStringToNickQueue(String text) {
		nickQueue.add(text);
	}
	public synchronized LinkedList<String> getNickQueue(){
		return nickQueue;
	}


	public synchronized void setQuit(boolean b) {
		quitserver = b;
	}
	
	public synchronized boolean getQuit(){
		return quitserver;
	}


	public synchronized void getStringFromClient(String string){
		if(string != ""){
			chatString += string + "\n";
			chatbox.setText(chatString);

		}
	}
	
	public synchronized void getNameFromClient(ArrayList<String> nickNames){
		clientNames = "";
		clients.setText(null);
		for(String temp : nickNames) {
			clientNames += temp + "\n";
		}
		clients.setText(clientNames);
		
	}
	
	public synchronized void setUpdate(boolean condition){
		updateServers = condition;
	}
	
	public synchronized boolean getUpdate(){
		return updateServers;
	}


	public void run() {
		chatbox.setText("Chose port to connect to and press sendmessage \n");
		
		boolean condition = true;
		do{
			if(!getQueue().isEmpty()){
				port = Integer.parseInt(getQueue().removeFirst());
				condition = false;
			}
			
		}while(condition);
		chatbox.setText("Send message for servers adress server\n");
		do{
			if(!getQueue().isEmpty()){
				address = getQueue().removeFirst();
				condition = true;
			}
		}while(!condition);
		chatbox.setText("Connect to the nameserver by press connect");
		
		while(running){
			String message = "";
			
			if(!getQueue().isEmpty()&&socket!=null){
				message = getQueue().removeFirst();
				System.out.println(message);
				MESS mess = new MESS(message, name, true);
				sendTCP(mess.toByteArray());
			}
			if(!getNickQueue().isEmpty()&&socket!=null){
	
				message = getNickQueue().removeFirst();
				CHNICK mess = new CHNICK(message);
				System.out.println("chnick "+message);
				sendTCP(mess.toByteArray());	
			}
			
			if(getQuit()&&socket!=null) {
				setQuit(false);
				
				QUIT quit = new QUIT();
				sendTCP(quit.toByteArray());
				connected = false;

			}
		}
	}	
	
	public synchronized void setSocket(Socket socket){
		this.socket = socket;
		try {
			outStream = socket.getOutputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private void sendTCP(byte[] bytes) {
		
		try {
			outStream.write(bytes.length);
			outStream.write(bytes);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}


	public synchronized boolean getConnected() {
		return connected;
	}
	
	public synchronized void setConnected(boolean condition){
		connected = condition;
	}

}
