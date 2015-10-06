package JUnit;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import PDU.GETLIST;
import PDU.PDU;

public class GETLISTTest {

	int opCode;
	int pads = 0;
	byte[] bytes;
	GETLIST getlist;
	
	@Before
	public void beforeTest() {	
		
		getlist = new GETLIST();	
		bytes = getlist.toByteArray();
		
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
	public void testGETLISTHasRigthOpcode() {
		assertEquals(3, opCode);
	}
	
	@Test
	public void testGETLISTHasRightPads() {
		assertEquals(3, pads);	
	}
	
	@Test
	public void testGETLISTHasRightBytes() {
		assertEquals(4, bytes.length);
	}
}
