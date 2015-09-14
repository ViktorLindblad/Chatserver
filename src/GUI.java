import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

import javax.swing.*;


public class GUI implements ActionListener{

	private JPanel panel;

	private JButton button;
	
	private LinkedList<String> queue;
	private String chatString = "";
	private String name = "";
	private String clientNames = "";

	
	private JFrame frame;
	
	private JTextArea chatbox;
	private JTextField message;
	private JTextArea clients;
	private JScrollPane chatScroll;
	private JScrollPane clientsScroll;

	
	public GUI(int width, int height){
		
		
		frame = new JFrame("Login");	
		message = new JTextField();
		
		panel = new JPanel();
		button = new JButton("Login");
		chatbox = new JTextArea();
		
		frame.add(panel);
		panel.add(button);
		
		panel.add(message);
		panel.add(chatbox);
		
		panel.setLayout(null);
		button.addActionListener(this);
		
		frame.setSize(width, height);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		
		frame.setVisible(true);
		button.setBounds((width/3),(height/3)*2 , width/3, height/3);
		
		chatbox.setBounds(width/3, 0, width/3, height/3);
		message.setBounds(width/3, height/3, width/3, height/3);
	}
	
	public GUI(){
		
		queue = new LinkedList<String> ();

		frame = new JFrame("client");
		message = new JTextField();
		
		chatbox = new JTextArea();
		clients = new JTextArea();
		
		panel = new JPanel();
		button = new JButton("Send message");
		
		chatScroll = new JScrollPane(chatbox);
		clientsScroll = new JScrollPane(clients);
		
		frame.add(panel);

		panel.add(message);
		
		panel.add(chatScroll);
		panel.add(clientsScroll);
		
		panel.add(clients);
		panel.add(button);

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
		button.setBounds(450,250,150,32);
		
		button.addActionListener(this);

	}

	
	public LinkedList<String> getQueue(){
		return queue;
	}
	
	public void addStringToQueue(String string){
		queue.add(string);
	}

	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == button){
			if(message.getText() != ""){
				addStringToQueue(message.getText());
				message.setText("");
			}
		}
	}
	
	public void getStringFromClient(String string){
		if(string != ""){
			chatString += string + "\n";
			chatbox.setText(chatString);
		}
	}
	
	public void getNameFromClient(String string){
		if(string != ""){
			clientNames += string + "\n";
			clients.setText(clientNames);
		}
	}
	
	public String getName(){
		return name;
	}
	
}
