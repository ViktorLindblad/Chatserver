package JUnit;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import PDU.PDU;
import PDU.ULEAVE;

public class ULEAVETest {

	int length;
	int opCode;
	int time;
	int pads = 0;
	String nickname = "Superman";
	String getNickname;
	ULEAVE uleave;
	byte[] bytes;
	
	@Before
	public void beforeTest() {	
		
		uleave = new ULEAVE(nickname);
		bytes = uleave.toByteArray();
		
		opCode = (int)PDU.byteArrayToLong(bytes, 0, 1);		
		length = (int)PDU.byteArrayToLong(bytes, 1, 2);	
		time = (int)PDU.byteArrayToLong(bytes, 4, 8);
		getNickname = PDU.stringReader(bytes, 8, 8+length);
		
		for(int i = 0 ; i < bytes.length ; i++) {		
			byte[] tempbyte = Arrays.copyOfRange
					(bytes, i, i+1);
			if(PDU.bytaArrayToString(tempbyte, 1).equals("\0")) {
				pads++;
			}
		}
	}
	
	@Test
	public void testULEAVEHasRightOpcode() {
		assertEquals(17, opCode);
	}
	
	@Test
	public void testULEAVEHasRightNickLenght() {
		assertEquals(8, length);
	}
	
	@Test
	public void testULEAVEHasRightNickname() {
		assertEquals(nickname, getNickname);
	}
	
	@Test
	public void testULEAVEHasRightPads() {
		assertEquals(2, pads);
	}
	
	@Test
	public void testULEAVEHasRightBytes() {
		assertEquals(16, bytes.length);
	}
}

