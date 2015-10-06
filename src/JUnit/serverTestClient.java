package JUnit;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import PDU.JOIN;
import PDU.PDU;
import Server.Server;

public class serverTestClient implements Runnable{

	private  Socket s1;
	private  OutputStream outStream1;
	private  InputStream inStream1;
	
	public serverTestClient(Server server,String name){
		try {
			s1 = new Socket(server.getAddress(),1555);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try {
			
			outStream1 = s1.getOutputStream();
			inStream1 = s1.getInputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		JOIN join = new JOIN(name);
		try {
			outStream1.write(join.toByteArray());
		} catch (IOException e) {
			e.printStackTrace();
		}
		new Thread(this).start();
	}
	
	@Override
	public void run() {
		byte[] buffer;
		while(true){
			try {
				
				do{
					int len = inStream1.available();
					buffer = PDU.readExactly(inStream1, len);
				}while(buffer.length == 0);	
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
