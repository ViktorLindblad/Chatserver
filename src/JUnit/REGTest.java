package JUnit;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import PDU.PDU;
import PDU.REG;

public class REGTest {
	
	int length;
	int rightLength;
	int opCode;
	int port = 1234;
	int rightPort;
	int pads = 0;
	String serverName;
	String rightServerName  = "PinkFluffyUnicorn";
	REG reg;
	byte[] bytes;
	
	@Before
	public void beforeTest() {
		
		reg = new REG(rightServerName, port);
		bytes = reg.toByteArray();
		
		opCode = (int)PDU.byteArrayToLong(bytes, 0, 1);	
		length = (int)PDU.byteArrayToLong(bytes, 1, 2);
		port = (int)PDU.byteArrayToLong(bytes, 2, 4);
		serverName = PDU.stringReader(bytes, 4, 4+length);
		
		for(int i = 1 ; i < bytes.length ; i++) {		
			byte[] tempbyte = Arrays.copyOfRange
					(bytes, i, i+1);
			if(PDU.bytaArrayToString(tempbyte, 1).equals("\0")) {
				pads++;
			}
		}
	}
	
	@Test
	public void testREGHasRigthOpcode() {
		assertEquals(0, opCode);
	}
	
	@Test
	public void testREGHasRightLength() {
		assertEquals(17, length);
	}
	
	@Test
	public void testREGHasRightPort() {
		assertEquals(1234, port);
	}
	
	@Test
	public void testREGHasRightServerName() {
		assertEquals("PinkFluffyUnicorn", serverName);
	}
	
	@Test
	public void testREGHasRightPads() {
		assertEquals(3, pads);
	}
	
	@Test
	public void testREGHasRightBytes() {
		assertEquals(24, bytes.length);
	}	
}
