package JUnit;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import PDU.JOIN;
import PDU.PDU;
import Server.Server;

public class ServerTest {
	
	private static boolean isSetUpDone = true;
	private static Server server;

	
	@Before
	public void setup() {
		
		if(isSetUpDone) {
			isSetUpDone = false;
			server = new Server(1555,"itchy.cs.umu.se",1337);
			for(int i=1; i<=255; i++) {
				@SuppressWarnings("unused")
				serverTestClient SC = new serverTestClient(server);
			}
			while(server.getSMH().size()<255){
			}
		}
	}
	
	@Test
	public void test255clients() {

		assertEquals(255,server.getSMH().size());
	}
	
	@Test
	public void test256clients(){
		serverTestClient c = new serverTestClient(server);
		
		byte[] temp = c.receive();
		
		int messageLength = (int)PDU.byteArrayToLong(temp, 4, 6);
		String message = PDU.stringReader(temp, 12, messageLength);

		
		assertEquals("Sorry, it's to many client online at the moment",message);
		
	}
}
