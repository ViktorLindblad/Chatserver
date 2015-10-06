package JUnit;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import PDU.MESS;
import PDU.PDU;

public class MESSIsClientTest {
	
	int opCode;
	int nickLength;
	int checksum;
	int time;
	int pads = 0;
	int messageLength;
	String message = "Hi! Im Ironman";
	String messageBack;
	String nickname = "Ironman";
	String nicknameBack;
	boolean isClient = true;
	MESS mess;	
	byte[] bytes;
		
	@Before
	public void beforeTest() {	
		
		mess = new MESS(message, nickname, isClient);
		bytes = mess.toByteArray();
		
		opCode = (int)PDU.byteArrayToLong(bytes, 0, 1);		
		nickLength = (int)PDU.byteArrayToLong(bytes, 2, 3);		
		checksum = (int)PDU.byteArrayToLong(bytes, 3, 4);		
		messageLength = (int)PDU.byteArrayToLong(bytes, 4, 6);
		time = (int)PDU.byteArrayToLong(bytes, 8, 12);
		messageBack = PDU.stringReader(bytes, 12, messageLength);
		nicknameBack = PDU.stringReader(bytes, messageLength,
				messageLength+nickLength);
		
		for(int i = 0 ; i < bytes.length ; i++) {		
			byte[] tempbyte = Arrays.copyOfRange
					(bytes, i, i+1);
			if(PDU.bytaArrayToString(tempbyte, 1).equals("\0")) {
				pads++;
			}
		}	
	}
	
	@Test
	public void testMESSHasRightOpcode() {
		assertEquals(10, opCode);
	}
	
	@Test
	public void testMESSHasRightNickLength() {
		assertEquals(0, nickLength);
	}
	
	@Test
	public void testMESSHasRightChecksum() {
		assertEquals(0, checksum);
	}
	
	@Test
	public void testMESSHasRightTime() {
		assertEquals(0, time);
	}
	
	@Test
	public void testMESSHasRightMessage() {
		assertEquals(message, messageBack);
	}
	
	@Test
	public void testMESSHasRightPads() {
		//NickLength, checksum and time is all zero and
		//messageLength only takes one bytes => 5+7 = 12 pads.
		assertEquals(12, pads);
	}
	
	@Test
	public void testMESSHasRightBytes() {
		assertEquals(28, bytes.length);
	}
}
