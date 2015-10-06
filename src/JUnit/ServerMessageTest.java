package JUnit;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import PDU.JOIN;
import PDU.PDU;
import Server.Server;

public class ServerMessageTest {

	private static boolean isSetUpDone = true;
	private static Server server;
	private static String nickName = "";
	private static byte[] buffer;
	
	@Before
	public void setup() {
		
		for(int i = 0 ; i < 256 ; i ++) {
			nickName += "A";
		}
		System.out.println(nickName.length());
		if(isSetUpDone) {
			isSetUpDone = false;
			server = new Server(1555,"itchy.cs.umu.se",1337);
		}
	}
	
	@Test
	public void testToLongNickName() {
		serverTestClient SC = new serverTestClient(server);
		
		JOIN join = new JOIN(nickName);
		
		SC.send(join.toByteArray());
		
		buffer = SC.receive();
		
		int length = (int)PDU.byteArrayToLong(buffer, 4, 6);
		System.out.println(length);
		String message = PDU.stringReader(buffer,12,length);
		
		assertEquals("Your nickname is either to long"+
				" or to short, goodbye!", message);
		
	}

}