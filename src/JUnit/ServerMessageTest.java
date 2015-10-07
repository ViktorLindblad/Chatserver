package JUnit;

import static org.junit.Assert.assertEquals;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import PDU.JOIN;
import PDU.MESS;
import PDU.PDU;
import Server.ByteSequenceBuilder;
import Server.OpCode;
import Server.Server;

public class ServerMessageTest {

	private static boolean isSetUpDone = true;
	private static Server server;
	
	@Before
	public void setup() {
		if(isSetUpDone) {
			isSetUpDone = false;
			server = new Server(1555,"itchy.cs.umu.se",1337);
		}
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
	
	@Test
	public void testSendHalfMessage() {
		serverTestClient SC = new serverTestClient(server);
		String nickName = "Tyra";
		JOIN join = new JOIN(nickName);
		byte [] first = Arrays.copyOfRange(join.toByteArray(), 0, 4);
		SC.send(first);
		for(int i = 0; i < 100000; i++){
			
		}
		byte[] second = Arrays.copyOfRange(join.toByteArray(), 4, 8);
		SC.send(second);
		byte[] buffer = SC.receive();
		int opCode = (int)PDU.byteArrayToLong(buffer, 0, 1);
		
		SC.send(second);
		
		
		assertEquals(19, opCode);
	}
	
	@Test
	public void testToLongMassage() {
		serverTestClient SC = new serverTestClient(server);
		String nickName = "Bo";
		
//		JOIN join = new JOIN(nickName);
//		SC.send(join.toByteArray());
//		SC.receive();
//		SC.receive();
		
		String message = "";
		
		for(int i = 0 ; i < 66000 ; i++){
			 message += "I";
		}
		
		MESS mess = new MESS(message, nickName, true);
		SC.send(mess.toByteArray());
				
		byte[] buffer = SC.receive();
		int length = (int)PDU.byteArrayToLong(buffer, 4, 6);
		String messageBack = PDU.stringReader(buffer, 12, 12+length);
		
		assertEquals(" have send a corrupt message, goodbye!", messageBack);
	}
	/*
	@Test 
	public void testWrongPad(){
		serverTestClient SC = new serverTestClient(server);
		String name = "tyras";
		byte[] names = name.getBytes(StandardCharsets.UTF_8);
		byte[] b = new ByteSequenceBuilder(OpCode.JOIN.value)
		.append((byte)0).pad()
		.append(names).toByteArray();
		
		SC.send(b);
		SC.receive();
		SC.receive();

		byte[] buffer = SC.receive();

		int length = (int)PDU.byteArrayToLong(buffer, 1, 2);
		String message = PDU.stringReader(buffer, 12, length);
		
		SC.send(Arrays.copyOfRange(b, 2, b.length));
		
		assertEquals(" have send a corrupt message, goodbye!",message);
	}
*/
}