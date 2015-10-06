package JUnit;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import PDU.CHNICK;
import PDU.PDU;

public class CHNICKTest {

	int nickLength;
	int opCode;
	int pads = 0;
	CHNICK chnick;	
	byte[] bytes;
	String nickname = "Ironman";
	String nicknameBack;
	
	@Before
	public void beforeTest() {	
		
		chnick = new CHNICK(nickname);
		bytes = chnick.toByteArray();
		
		opCode = (int)PDU.byteArrayToLong(bytes, 0, 1);		
		nickLength = (int)PDU.byteArrayToLong(bytes, 1, 2);		
		nicknameBack = PDU.stringReader(bytes, 4, 4+nickLength);
		
		for(int i = 0 ; i < bytes.length ; i++) {		
			byte[] tempbyte = Arrays.copyOfRange
					(bytes, i, i+1);
			if(PDU.bytaArrayToString(tempbyte, 1).equals("\0")) {
				pads++;
			}
		}
	}
	
	@Test
	public void testCHNICKHasRightOpcode() {
		assertEquals(13, opCode);
	}
	
	@Test
	public void testCHNICKHasRigthLength() {
		assertEquals(7, nickLength);
	}
	
	@Test
	public void testCHNICKHasRightNickname() {
		assertEquals(nickname, nicknameBack);
	}
	
	@Test
	public void testCHNICKHasRightPads() {
		assertEquals(3, pads);
	}
	
	@Test
	public void testCHNICKHasRightBytes() {
		assertEquals(12, bytes.length);
	}
}