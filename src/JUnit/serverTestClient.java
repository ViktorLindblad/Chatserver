package JUnit;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import PDU.JOIN;
import PDU.PDU;
import Server.Server;

public class serverTestClient{

	private  Socket s1;
	private  OutputStream outStream1;
	private  InputStream inStream1;
	
	public serverTestClient(Server server){
		
		for(int i=0; i<100000; i++){
			
		}
		
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

	}

	public byte[] receive() {
		byte[] buffer = null;
		try {
			
			do{
				int len = inStream1.available();
				buffer = PDU.readExactly(inStream1, len);
			}while(buffer.length == 0);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return buffer;
	}

}
