package JUnit;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import PDU.MESS;
import PDU.PDU;

public class MESSIsNotClientTest {
	
	long timeNow;
	long time;
	int opCode;
	int nickLength;
	int checksum;
	int pads = 0;
	int messageLength;
	String message = "Hi! Im Ironman";
	String messageBack;
	String nickname = "Ironman";
	String nicknameBack;
	boolean isClient = false;
	MESS mess;	
	byte[] bytes;
	
	
	@Before
	public void beforeTest() {	
		
		mess = new MESS(message, nickname, isClient);
		bytes = mess.toByteArray();
		
		timeNow = System.currentTimeMillis();
		
		opCode = (int)PDU.byteArrayToLong(bytes, 0, 1);		
		nickLength = (int)PDU.byteArrayToLong(bytes, 2, 3);		
		checksum = (int)PDU.byteArrayToLong(bytes, 3, 4);		
		messageLength = (int)PDU.byteArrayToLong(bytes, 4, 6);
		System.out.println("messlength: " + messageLength);
		time = PDU.byteArrayToLong(bytes, 8, 12);
		messageBack = PDU.stringReader(bytes, 12, messageLength);
		
		if(messageLength % 4 != 0) {
			messageLength += 4 - ( messageLength % 4);
		}
		
		nicknameBack = PDU.stringReader(bytes, 12 + messageLength, 
				nickLength);
		
		for(int i = 0 ; i < bytes.length; i++) {		
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
		assertEquals(7, nickLength);
	}
	
	@Test
	public void testMESSHasRightChecksum() {
		assertEquals(0, checksum);
	}
	@Test
	public void testMESSTime() {
		assertNotEquals(timeNow, time);
	}
	
	@Test
	public void testMESSHasRightMessage() {
		assertEquals(message, messageBack);
	}
	
	@Test
	public void testMESSHasRightNicknameBack() {
		assertEquals(nickname, nicknameBack);
	}
	
	@Test
	public void testMESSHasRightPads() {
		//Checksum is 0 and length is only one bytes => 6+2 = 8 pads.
		assertEquals(8, pads);
	}
	
	@Test
	public void testMESSHasRightBytes() {
		assertEquals(36, bytes.length);
	}
}