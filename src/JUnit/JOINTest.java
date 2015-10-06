package JUnit;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import PDU.JOIN;
import PDU.PDU;

public class JOINTest {

	int nickLength;
	int opCode;
	int pads = 0;
	JOIN join;	
	byte[] bytes;
	String nickname = "Ironman";
	String nicknameBack;
	
	@Before
	public void beforeTest() {	
		
		join = new JOIN(nickname);
		bytes = join.toByteArray();
		
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
	public void testJOINHasRightOpcode() {
		assertEquals(12, opCode);
	}
	
	@Test
	public void testJOINHasRigthLength() {
		assertEquals(7, nickLength);
	}
	
	@Test
	public void testJOINHasRightNickname() {
		assertEquals("Ironman", nicknameBack);
	}
	
	@Test
	public void testJOINHasRightPads() {
		assertEquals(3, pads);
	}
	
	@Test
	public void testJOINHasRightBytes() {
		assertEquals(12, bytes.length);
	}
}
