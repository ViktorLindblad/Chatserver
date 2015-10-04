package JUnit;

import static org.junit.Assert.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.junit.Before;
import org.junit.Test;

import PDU.JOIN;
import PDU.PDU;
import Server.Server;

public class ServerTest {
	
	private static boolean isSetUpDone = false;
	private static Socket s1,s2;
	private static Server server;
	private static OutputStream outStream1,outStream2;
	private static InputStream inStream1, inStream2;
	private static DataInputStream dataInput1, dataInput2;
	private static DataOutputStream dataOutput1, dataOutput2;
	
	@Before
	public void setup(){
		if(isSetUpDone){
			server = new Server(12,"itchy.cs.umu.se",1337);
			try {
				s1 = new Socket(server.getAddress(),12);
				outStream1 = s1.getOutputStream();
				inStream1 = s1.getInputStream();
			} catch (IOException e) {
				e.printStackTrace();
			}
	
			dataInput1 = new DataInputStream(inStream1);
			dataOutput1 = new DataOutputStream(outStream1);
	
			try {
				s2 = new Socket(server.getAddress(),12);
				outStream2 = s2.getOutputStream();
				inStream2 = s2.getInputStream();
			} catch (IOException e) {
				e.printStackTrace();
			}
			dataInput2 = new DataInputStream(inStream2);
			dataOutput2 = new DataOutputStream(outStream2);
			isSetUpDone = true;
		}
	}
	
	@Test
	public void joinServerWithCorrectName() {
		JOIN join = new JOIN("anna");
		
		try {
			outStream1.write(join.toByteArray().length);
			outStream1.write(join.toByteArray());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		assertEquals(server.getNames().get(s1),"anna","Wrong names");
		try {
			int length =(int) dataInput1.readByte();
			byte [] buffer = new byte[length];
			buffer = PDU.readExactly(inStream1, length);			
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	@Test
	public void joinWithWrongName(){
		JOIN join = new JOIN("anna");
		
		try {
			outStream1.write(join.toByteArray().length);
			outStream1.write(join.toByteArray());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			outStream2.write(join.toByteArray().length);
			outStream2.write(join.toByteArray());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		assertTrue(server.getNames().size()==1);
	}
}
