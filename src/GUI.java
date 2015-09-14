import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;


public class GUI extends JFrame implements ActionListener{

	private JPanel panel;

	private JButton button;
	
	//private Graphics g;
	private Client client;
	
	private JTextArea chat;
	private JTextField write;
	private JTextArea connecteds;
	private JScrollPane scroll;
	
	public GUI(Client client){
		super("Client");
		
		this.client = client;
		
		setSize(620,360);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		setResizable(false);
		setLocationRelativeTo(null);
		
		setVisible(true);
		setBackground(Color.PINK);
		
		//g = getGraphics();
		
		panel = new JPanel();
		panel.setLayout(null);

		
		chat = new JTextArea();
		write = new JTextField();
		connecteds = new JTextArea();
		
		chat.setBounds(0,0,400,200);
		
		write.setBounds(0,200,400,60);
		
		connecteds.setBounds(400,0,220,300);
		
		scroll = new JScrollPane(chat);
		scroll.setBounds(0,0,400,200);
		
		scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		
		button = new JButton("Send message");
		
		button.setBounds(450,300,64,32);
		
		button.addActionListener(this);
		
		panel.add(scroll);
		panel.add(write);
		panel.add(connecteds);
		panel.add(button);
		
		add(panel);
		
		
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
			System.out.println("click");
			client.getStringFromGUI(write.getText());
		}
	}
	
	
	
}
