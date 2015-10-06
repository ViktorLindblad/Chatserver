package JUnit;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import PDU.CHNICK;
import PDU.JOIN;
import PDU.PDU;
import Server.ByteSequenceBuilder;
import Server.Server;

public class ServerMessageTest {

	private static boolean isSetUpDone = true;
	private static Server server;
	private static String nickName = "";
	
	@Before
	public void setup() {
		if(isSetUpDone) {
			isSetUpDone = false;
			server = new Server(1555,"itchy.cs.umu.se",1337);
		}
	}
	
	@Test
	public void testToLongNickName() {
		serverTestClient SC = new serverTestClient(server);
		
		for(int i = 0 ; i < 256 ; i ++) {
			nickName += "A";
		}
		
		JOIN join = new JOIN(nickName);
		
		SC.send(join.toByteArray());
		
		byte[] buffer = SC.receive();
		
		int length = (int)PDU.byteArrayToLong(buffer, 4, 6);
		System.out.println(length);
		String message = PDU.stringReader(buffer,12,length);
		
		assertEquals("Your nickname is either to long"+
				" or to short, goodbye!", message);
		
	}
	
	@Test
	public void testToShortNickName() {
		serverTestClient SC = new serverTestClient(server);
		
		nickName = "";
		
		JOIN join = new JOIN(nickName);
		
		SC.send(join.toByteArray());
		
		byte[] buffer = SC.receive();
		
		int length = (int)PDU.byteArrayToLong(buffer, 4, 6);
		System.out.println(length);
		String message = PDU.stringReader(buffer,12,length);
		
		assertEquals("Your nickname is either to long"+
				" or to short, goodbye!", message);
		
	}
	
	@Test
	public void testToLongCHNickName() {
		serverTestClient SC = new serverTestClient(server);
		
		for(int i = 0 ; i < 256 ; i ++) {
			nickName += "A";
		}
		
		JOIN join = new JOIN("adam");
		
		SC.send(join.toByteArray());
		
		byte [] buffer = SC.receive();
		
		buffer = SC.receive();
		
		CHNICK chnick = new CHNICK(nickName);

		SC.send(chnick.toByteArray());
		
		buffer = SC.receive();

		
		int length = (int)PDU.byteArrayToLong(buffer, 4, 6);
		String message = PDU.stringReader(buffer,12,length);
		
		assertEquals("Your nickname is either to long"+
				" or to short, goodbye!", message);
	}
	
	@Test
	public void testToShortCHNickName() {
		serverTestClient SC = new serverTestClient(server);

		JOIN join = new JOIN("adam");
		
		SC.send(join.toByteArray());
		
		byte[] buffer = SC.receive();
				buffer = SC.receive();
		

		CHNICK chnick = new CHNICK("");
		
		SC.send(chnick.toByteArray());
		
		
		buffer = SC.receive();
		int length = (int)PDU.byteArrayToLong(buffer, 4, 6);

		String message = PDU.stringReader(buffer,12,length);
		assertEquals("Your nickname is either to long"+
				" or to short, goodbye!", message);
	}
	
	
	@Test
	public void testWrongOPCode() {
		serverTestClient SC = new serverTestClient(server);
		
		int OpCode = 20;
		
		byte[] bytes = new ByteSequenceBuilder((byte)OpCode)
						.pad().toByteArray();
		
		SC.send(bytes);
		
		byte[] buffer = SC.receive();

		
		int length = (int)PDU.byteArrayToLong(buffer, 0, 1);
		
		assertEquals(11,length);
	}

}