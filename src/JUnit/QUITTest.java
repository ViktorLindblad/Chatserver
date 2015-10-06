package JUnit;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import PDU.PDU;
import PDU.QUIT;

public class QUITTest {
	
	int opCode;
	int pads = 0;
	byte[] bytes;
	QUIT quit;

	@Before
	public void beforeTest() {
		
		quit = new QUIT();
		bytes = quit.toByteArray();
		
		opCode = (int)PDU.byteArrayToLong(bytes, 0, 1);	
		
		for(int i = 0 ; i < bytes.length ; i++) {		
			byte[] tempbyte = Arrays.copyOfRange
					(bytes, i, i+1);
			if(PDU.bytaArrayToString(tempbyte, 1).equals("\0")) {
				pads++;
			}
		}
	}
	
	@Test
	public void testQUITHasRightOpcode() {
		assertEquals(11, opCode);
	}
	
	@Test
	public void testQUITHasRightPads() {
		assertEquals(3, pads);
	}
	
	@Test
	public void testQUITHasRightBytes() {
		assertEquals(4, bytes.length);
	}
}
