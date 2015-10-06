package JUnit;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import PDU.PDU;
import Server.Server;

public class serverTestClient{

	private  Socket s;
	private  InputStream inStream;
	private OutputStream outStream;
	
	public serverTestClient(Server server){
		
		for(int i=0; i<100000; i++){
			
		}
		
		try {
			s = new Socket(server.getAddress(),1555);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try {
			
			inStream = s.getInputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			outStream = s.getOutputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void send(byte[] bytes) {
		
		try {
			outStream.write(bytes);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public byte[] receive() {
		byte[] buffer = new byte[65535];
		try {
			inStream.read(buffer);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return buffer;
	}

}
