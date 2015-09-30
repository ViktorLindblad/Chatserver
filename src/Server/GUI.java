package Server;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;


public class GUI implements ActionListener, Runnable{

	private JPanel panel;

	private boolean updateServers, quitserver;
	
	private JButton button;
	private JButton update;
	private JButton quit;
	private JButton changeNick;
	
	private LinkedList<String> queue;
	private LinkedList<String> nickQueue;
	
	private String chatString = "";
	private String name = "";
	private String clientNames = "";

	
	private JFrame frame;
	
	private JTextArea chatbox;
	private JTextField message;
	private JTextArea clients;
	private JScrollPane chatScroll;
	private JScrollPane clientsScroll;
	
	public GUI(){
		
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
		
		update.setBounds(450,210,150,32);
		
		quit.setBounds(450, 290, 150, 32);
		changeNick.setBounds(450, 250, 150, 32);
		
		button.addActionListener(this);
		update.addActionListener(this);

		quit.addActionListener(this);
		changeNick.addActionListener(this);
		
	}

	
	public synchronized LinkedList<String> getQueue(){
		return queue;
	}
	
	private void addStringToQueue(String string){
		queue.add(string);
	}

	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == update){
			setUpdate(true);
		}
		
		if(e.getSource() == quit){
			setQuit(true);
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
		clients.setText("");
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
		
	}	
}
