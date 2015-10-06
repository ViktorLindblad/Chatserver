package JUnit;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import PDU.MESS;
import PDU.PDU;

public class MESSChecksumTest {
	
	int opCode;
	int nickLength;
	int checksum;
	int time;
	int pads = 0;
	int messageLength;
	String message = "";
	String messageBack;
	String nickname = "Ironman";
	String nicknameBack;
	boolean isClient = true;
	MESS mess;	
	byte[] bytes;
	
	@Before
	public void beforeTest() {	
		
		for(int i = 0 ; i < 4000 ; i++) {
			message += "a";
		}
		
		mess = new MESS(message, nickname, isClient);
		bytes = mess.toByteArray();
		
		checksum = (int)PDU.byteArrayToLong(bytes, 3,4 );	
		
		for(int i = 0 ; i < bytes.length ; i++) {		
			byte[] tempbyte = Arrays.copyOfRange
					(bytes, i, i+1);
			if(PDU.bytaArrayToString(tempbyte, 1).equals("\0")) {
				pads++;
			}
		}
	}
	
	@Test
	public void testMESSHasRightChecksum() {
		assertEquals(187, checksum);
	}
	
	@Test
	public void testMESSHasRightPads() {
		//nickLength and time is zero => 3 + 5 = 8 pads
		assertEquals(8, pads);
	}
	
	@Test
	public void testMESSHasRightBytes() {
		assertEquals(4012, bytes.length);
	}
}