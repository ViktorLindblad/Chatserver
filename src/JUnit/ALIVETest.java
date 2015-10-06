package JUnit;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import PDU.ALIVE;
import PDU.PDU;

public class ALIVETest {
	
	int howManyClients = 250;
	int nrOfClientBack;
	int id = 2055;
	int idBack;
	int opCode;
	int pads = 0;
	ALIVE alive;	
	byte[] bytes;
	
	@Before
	public void beforeTest() {	
		
		alive = new ALIVE(howManyClients, id);
		bytes = alive.toByteArray();
		
		opCode = (int)PDU.byteArrayToLong(bytes, 0, 1);		
		nrOfClientBack = (int)PDU.byteArrayToLong(bytes, 1, 2);		
		idBack = (int)PDU.byteArrayToLong(bytes, 2, 4);		
		
		for(int i = 0 ; i < bytes.length ; i++) {		
			byte[] tempbyte = Arrays.copyOfRange
					(bytes, i, i+1);
			if(PDU.bytaArrayToString(tempbyte, 1).equals("\0")) {
				pads++;
			}
		}
	}	
	
	@Test
	public void testALIVEHasRightOpcode() {
		assertEquals(2, opCode);
	}
	
	@Test
	public void testALIVEHasRightNrOfClients() {
		assertEquals(howManyClients, nrOfClientBack);
	}
	
	@Test
	public void testALIVEHasRightId() {
		assertEquals(id, idBack);
	}
	
	@Test
	public void testALIVEHasRightPads() {
		assertEquals(0, pads);
	}
	
	@Test
	public void testALIVEHasRightBytes() {
		assertEquals(4, bytes.length);
	}
}
