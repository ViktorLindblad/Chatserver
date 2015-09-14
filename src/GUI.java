import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;


public class GUI implements ActionListener{

	private JPanel panel;

	private JButton button;
	
	//private Graphics g;
	private Client client;
	private JFrame frame;
	
	private JTextArea chatbox;
	private JTextField message;
	private JTextArea clients;
	private JScrollPane chatScroll;
	private JScrollPane clientsScroll;
	
	public GUI(Client client){
		
		this.client = client;

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
	/*
	public void render(Graphics g, String str){
		
		g.setColor(Color.PINK);
		g.fillRect(0, 0, 620, 360);
		g.setColor(Color.RED);
		g.fillRect(0, 0, 400, 200);
		g.setColor(Color.BLACK);
		g.drawString(str, 330, 180);
	}*/

	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == button){
			client.getStringFromGUI(message.getText());
			message.setText("");
		}
	}
	
	public void getStringFromClient(String string){
		if(string!=""){
			chatbox.setText(string);
		}
	}
}
