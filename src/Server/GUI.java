package Server;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

import PDU.CHNICK;
import PDU.MESS;
import PDU.PDU;
import PDU.QUIT;

/**
 * A graphical display used by a persons to interact with the program.
 *
 */

public class GUI implements ActionListener, Runnable{

	//Constants for OpCodes
	private static final int MESS = 10, QUIT = 11, UJOIN = 16, 
							ULEAVE = 17, UCNICK = 18, NICKS = 19;
	
	private JPanel panel;

	//A list with nick names online at the connected server.
	private ArrayList<String> nickNames;
	
	private boolean connected, updateServers, quitserver, running,
					clientconnect;
	
	private JButton button;
	private JButton connect;
	private JButton update;
	private JButton quit;
	private JButton changeNick;
	private JButton clearText;
	
	private Client client;
	
	private LinkedList<String> queue;
	private LinkedList<String> nickQueue;
	
	private String chatString = "";
	private String name = "";
	private String clientNames = "";

	private Socket socket;
	private OutputStream outStream;
	
	private JFrame frame;
	
	private int port,TCPport;
	
	private String address;
	
	private JTextArea chatbox;
	private JTextField message;
	private JTextArea clients;
	private JScrollPane chatScroll;
	private JScrollPane clientsScroll;
	
	private Thread clientThread;
	
	/**
	 * Creates a new GUI 
	 * 
	 * @param port - The port where it will try to connect to the 
	 * name server.
	 */
	
	public GUI(int port){
		
		nickNames = new ArrayList<String>();

		this.port = port;
		socket = null;
		updateServers = false;
		
		queue = new LinkedList<String> ();
		nickQueue = new LinkedList<String> ();


		frame = new JFrame("Chat");
		message = new JTextField();
		
		chatbox = new JTextArea();
		clients = new JTextArea();
		
		panel = new JPanel();
		button = new JButton("Send message");
		update = new JButton("Update");
		connect = new JButton("Connect");
		clearText = new JButton("Clear");

		
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

		panel.add(clearText);
		
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
		
		clearText.setBounds(50, 205, 150, 32);
		
		quit.setBounds(450, 290, 150, 32);
		changeNick.setBounds(450, 250, 150, 32);
		
		button.addActionListener(this);
		update.addActionListener(this);
		
		connect.addActionListener(this);
		clearText.addActionListener(this);

		quit.addActionListener(this);
		changeNick.addActionListener(this);
		running = true;
		clientconnect = true ;
		
		new Thread(this).start();
		
	}

	/**
	 * Gets the queue with the input from the user
	 * 
	 * @return queue - The queue with Strings.
	 */
	
	public synchronized LinkedList<String> getQueue(){
		return queue;
	}
	
	/**
	 * Adds a string to the queue.
	 * 
	 * @param string - The string to add.
	 */
	
	private void addStringToQueue(String string){
		queue.add(string);
	}
	
	/**
	 * This method is called if an ActionEvent is done.
	 * It will check which JButton was pressed and 
	 * act after that.
	 */

	public void actionPerformed(ActionEvent e) {
		
		if(e.getSource() == clearText){
			chatbox.setText(null);
			chatString = "Text cleared \n";
			chatbox.setText(chatString);
		}
		
		if(e.getSource() == connect){
			
			if(clientconnect) {
				Random random = new Random();
				TCPport += random.nextInt(1000);
				client = new  Client(address,this,TCPport,port);
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
			clients.setText(null);
			clientNames = "";
			
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
	
	/**
	 * Adds a string to the nickQueue, which is a queue only
	 * containing nick name.
	 * 
	 * @param text - The nick name to add.
	 * 
	 */
	
	private void addStringToNickQueue(String text) {
		nickQueue.add(text);
	}
	
	/**
	 * Gets the nickQueue.
	 * 
	 * @return nickQueue - The queue with nick names.
	 */
	
	public synchronized LinkedList<String> getNickQueue(){
		return nickQueue;
	}

	/**
	 * Sets the boolean quitserver to the given boolean.
	 * 
	 * @param b - The condition to set.
	 */

	public synchronized void setQuit(boolean b) {
		quitserver = b;
	}
	
	/**
	 * Gets the boolean quitserver condition.
	 *  
	 * @return boolean - True if quit button has been pressed else false.
	 */
	
	public synchronized boolean getQuit(){
		return quitserver;
	}
	
	/**
	 * Gets a string from a different thread and adds it into
	 * the chatbox.
	 *  
	 * @param string - The string to add.
	 */

	public synchronized void getStringFromClient(String string) {
		if(string != ""){

			chatString += string + "\n";
			chatbox.setText(chatString);
		}
	}
	
	/**
	 * Gets a string from a different thread and adds into the chatbox.
	 * This method does the same as getStringFromClient but inserts a \n
	 * after every 50 characters.
	 * 
	 * @param string - The string to add.
	 */
	
	public synchronized void getStringMessageFromClient(String string) {
		int length = string.length();
		int i = 0;
		int messlength = 50;
		if(string.length() <= 50) {
			chatString += string + "\n";
		} else {
			while(length > 50) {
			
				length -= 50;
				
				chatString += string.substring(i, i+messlength)+"\n";
				i += 50;
			}
			if(length <=50) {
				messlength = length;
				chatString += string.substring(i, i+messlength)+"\n";
			}
		}
		chatbox.setText(chatString);
	}
	
	/**
	 * Gets a list with names from the client and adds it into
	 * JTextArea with names.
	 * 
	 * @param nickNames - The list with the names.
	 */
	
	public synchronized void getNameFromClient(ArrayList<String> nickNames){
		clientNames = "";
		clients.setText(null);
		for(String temp : nickNames) {
			clientNames += temp + "\n";
		}
		clients.setText(clientNames);
		
	}
	
	/**
	 * Sets the boolean updateServers to the given boolean.
	 * 
	 * @param condition - The boolean to set.
	 */
	
	public synchronized void setUpdate(boolean condition){
		updateServers = condition;
	}
	
	/**
	 * Gets the boolean updateServers state.
	 * 
	 * @return opdateServers - boolean true if the update button
	 * has been pressed else false.
	 */
	
	public synchronized boolean getUpdate(){
		return updateServers;
	}
	
	/**
	 * The GUI threads run method.
	 * Checks two queue's and one boolean if the queue's not empty a 
	 * PDU is created and sent to the server. if the boolean is
	 * true the client closes.
	 */

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
				MESS mess = new MESS(message, name, true);
				sendTCP(mess.toByteArray());
			}
			if(!getNickQueue().isEmpty()&&socket!=null){
	
				message = getNickQueue().removeFirst();
				CHNICK mess = new CHNICK(message);
				sendTCP(mess.toByteArray());	
			}
			
			if(getQuit()&&socket!=null) {
				setQuit(false);
				QUIT quit = new QUIT();
				sendTCP(quit.toByteArray());
				connected = false;
				socket = null;

			}
			if(client != null && !client.getMessageQueue().isEmpty()){
				checkMessage(client.getMessageQueue().removeFirst());
			}
		}
	}	
	
	/**
	 * Check the received message from the server.
	 * It will check which kind of message it is and act after that.
	 * 
	 * @param bytes - The message to check in bytes.
	 */
	
	private void checkMessage(byte[] bytes) {
		int ca = (int)PDU.byteArrayToLong(bytes, 0, 1);
		int length;
		int time;
		String name = "";
		
		switch(ca) {
			case(MESS):
				String messname;
				int nameLength = (int)PDU.byteArrayToLong(bytes, 2, 3);
				length = (int)PDU.byteArrayToLong(bytes, 4, 6);

				time = (int)PDU.byteArrayToLong(bytes, 8, 12);
				String message = PDU.stringReader(bytes, 12,length);
				
				if(length % 4 != 0) {
					length += 4 - ( length % 4);
				}
				
				if(nameLength == 0){
					messname = "Server";
				} else {
					 messname = PDU.stringReader
							 		(bytes, 12+length,nameLength);
				}
				
				SimpleDateFormat sdf = new 
									SimpleDateFormat("MMM dd,yyyy HH:mm");    
				Date resultdate = new Date(time);
				
				
				
				String mess = sdf.format(resultdate)+": "
											+messname+" said: "+message;
				getStringMessageFromClient(mess);
				
			break;
			
			case(QUIT):
				System.out.println("CLOSE");
				client.closeClientsSocket();
				
			break;
			
			case(UJOIN):
				
				length = (int)PDU.byteArrayToLong(bytes, 1, 2);
				time = (int) PDU.byteArrayToLong(bytes, 4, 8);
				
				name = PDU.stringReader(bytes, 8, length);
				
				boolean don = true;
				for(String temp : nickNames){
					if(temp.equals(name)){
						don = false;
					}
				}
				if(don){
					nickNames.add(name);
					getNameFromClient(nickNames);
				}
				
				
				getStringFromClient(name+" joined chatroom at: " 
															+time);
				
			break;
			
			case(ULEAVE):
				length = (int)PDU.byteArrayToLong(bytes, 1, 2);
			
				name = PDU.stringReader(bytes, 8, length);
			
				for(int i = 0; i < nickNames.size(); i++ ){
					if(nickNames.get(i).equals(name)){
						nickNames.remove(i);
					}
				}
				getNameFromClient(nickNames);
				getStringFromClient(name+" leaved the chatroom");
			break;
			
			case(UCNICK):
				length = (int)PDU.byteArrayToLong(bytes, 1, 2);
				int secondLength = (int)PDU.byteArrayToLong(bytes, 2, 3);
				time = (int)PDU.byteArrayToLong(bytes, 4, 8);
				
				name = PDU.stringReader(bytes, 8, length);
				
				if(length%4!=0){
					length += 4 - (length % 4);
				}
				
				String name2 = PDU.stringReader
							(bytes, 8+length, secondLength);

		
			for(int i = 0; i < nickNames.size(); i++ ){
				if(nickNames.get(i).equals(name)){
					nickNames.set(i, name2);
					getNameFromClient(nickNames);
				}
			}
			String changeNick = "Time: "+String.valueOf(time)
					+"Old nick: "+name+"New nick: "+name2;
			
			getStringFromClient(changeNick);
			
			
			break;
			
			case(NICKS):
				length = (int)PDU.byteArrayToLong(bytes, 1, 2);
				int index = 4;
				boolean condition;
				System.out.println("NICKS " +length);
				for(int i = 0; i < length; i++){
					condition = true;
					name = "";
					do{
						byte[] tempbyte = Arrays.copyOfRange
								(bytes, index, index+1);
						
						String character = PDU.bytaArrayToString
								(tempbyte, 1);
						
						if(character.equals("\0")){
							condition = false;
						} else {
							name += character;
						}
						
						index++;
					}while(condition);
					
					boolean dont = true;
					
					for(String temp : nickNames){
						
						if(name.equals(temp)){
							dont = false;
						}
					}

					if(dont){
						nickNames.add(name);
					}
				}

				getNameFromClient(nickNames);

			break;
			
			default:
			break;
		}
	}
	
	/**
	 * Sets the field socket to the given socket
	 * 
	 * @param socket - The socket to set.
	 */
	
	public synchronized void setSocket(Socket socket){
		this.socket = socket;
		try {
			outStream = socket.getOutputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Sends a message to the server with TCP.
	 * 
	 * @param bytes - The message in bytes.
	 */
	private void sendTCP(byte[] bytes) {
		
		try {
			outStream.write(bytes);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * Get the boolean connected's state.
	 * 
	 * @return connected - boolean true if the socket is connected
	 * else false.
	 */

	public synchronized boolean getConnected() {
		return connected;
	}
	
	/**
	 * Sets the boolean connected to the given boolean.
	 * 
	 * @param condition - The boolean to set the field to.
	 */
	
	public synchronized void setConnected(boolean condition){
		connected = condition;
	}

}