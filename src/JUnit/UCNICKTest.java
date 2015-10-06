package JUnit;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import PDU.PDU;
import PDU.UCNICK;

public class UCNICKTest {

	int oldNickLength;
	int newNickLength;
	int length;
	int opCode;
	int time;
	int pads = 0;
	String oldNickname = "Ironman";
	String newNickname = "Superman";
	String getNewNickname;
	String getOldNickname;
	UCNICK ucnick;	
	byte[] bytes;
	
	@Before
	public void beforeTest() {	
		
		ucnick = new UCNICK(oldNickname, newNickname);
		bytes = ucnick.toByteArray();
		
		opCode = (int)PDU.byteArrayToLong(bytes, 0, 1);		
		oldNickLength = (int)PDU.byteArrayToLong(bytes, 1, 2);	
		newNickLength = (int)PDU.byteArrayToLong(bytes, 2, 3);
		getOldNickname = PDU.stringReader(bytes, 8, 8+oldNickLength);
		
		length =oldNickLength;
		if(length%4!=0) {
			length += 4 - (length % 4);
		}
		getNewNickname = PDU.stringReader(bytes, 8+length, 
				8+length+newNickLength);			
		
		for(int i = 0 ; i < bytes.length ; i++) {		
			byte[] tempbyte = Arrays.copyOfRange
					(bytes, i, i+1);
			if(PDU.bytaArrayToString(tempbyte, 1).equals("\0")) {
				pads++;
			}
		}
	}
		
	@Test
	public void testUCNICKHasRightOpcode() {
		assertEquals(18, opCode);
	}
	
	@Test
	public void testUCNICKHHasRightOldNickLenght() {
		assertEquals(7, oldNickLength);
	}
	
	@Test
	public void testUCNICKHHasRightNewNickLenght() {
		assertEquals(8, newNickLength);
	}
	
	@Test
	public void testUCNICKHHasRightOldNick() {
		assertEquals(oldNickname, getOldNickname);
	}
	
	@Test
	public void testUCNICKHHasRightNewNick() {
		assertEquals(newNickname, getNewNickname);
	}
	
	@Test
	public void testUCNICKHasRightPads() {
		assertEquals(2, pads);
	}
	
	@Test
	public void testUCNICKHasRightBytes() {
		assertEquals(24, bytes.length);
	}
}
