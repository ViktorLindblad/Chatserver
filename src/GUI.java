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
	
	private JTextArea chat;
	private JTextField write;
	private JTextArea connecteds;
	private JScrollPane scroll;
	
	public GUI(Client client){
		
		this.client = client;

		frame = new JFrame("client");
		chat = new JTextArea();
		write = new JTextField();
		connecteds = new JTextArea();
		panel = new JPanel();
		button = new JButton("Send message");
		scroll = new JScrollPane(chat);
		
		frame.add(panel);
		
		panel.add(scroll);
		panel.add(write);
		panel.add(connecteds);
		panel.add(button);

		frame.setSize(620,360);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		
		frame.setVisible(true);
		
		//g = getGraphics();
		panel.setLayout(null);
		
		chat.setBounds(0,0,400,200);
		
		write.setBounds(0,200,400,60);
		
		connecteds.setBounds(400,0,220,300);
		
		scroll.setBounds(0,0,400,200);
		
		scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		
		button.setBounds(450,300,64,32);
		
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
			System.out.println(write.getText());
			client.getStringFromGUI(write.getText());
		}
	}
}
