package JUnit;

import static org.junit.Assert.*;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;

import org.junit.Before;
import org.junit.Test;

import PDU.NICKS;
import PDU.PDU;


public class NICKSTest {
	
	public static boolean set = true;
	private static String nickname1 = "Anna";
	private static String nickname2 = "Kalle";
	private static Socket socket;
	private static Socket socket2;
	private static byte[] bytes; 
	private static byte[] checkTheNames; 
	private static int howManyNames, length, opCode;
	private static int pads = 0;
	private static ArrayList<String> nickNames;
	private static NICKS nicks;
	private static Hashtable<Socket,String> names;
	
	@Before
	public void beforeTest() {
		if(set){
			socket = new Socket();
			socket2 = new Socket();
			names = new Hashtable<Socket, String>();
			nickNames = new ArrayList<String>();
	
			names.put(socket, nickname1);
			names.put(socket2, nickname2);
			
			nicks = new NICKS(names);
			bytes = nicks.toByteArray();
			
			opCode = (int)PDU.byteArrayToLong(bytes, 0, 1);
			howManyNames = (int)PDU.byteArrayToLong(bytes, 1, 2);
			length = (int)PDU.byteArrayToLong(bytes, 2, 4);
			checkTheNames = Arrays.copyOfRange(bytes, 4, 4+length);

			for(int i = 0 ; i < bytes.length ; i++) {		
				byte[] tempbyte = Arrays.copyOfRange
						(bytes, i, i+1);
				if(PDU.bytaArrayToString(tempbyte, 1).equals("\0")) {
					pads++;
				}
			}	
			set = false;
		}
	}
	
	@Test
	public void testNICKSHasRightHowManyName() {
		assertEquals(2, howManyNames);
	}
	
	@Test
	public void testNICKSHasRightNamesBack() {
		
		int index = 0;
		boolean condition;
		String name;

		for(int i = 0; i < howManyNames; i++) {
			condition = true;
			name = "";
			do{
				byte[] tempbyte = Arrays.copyOfRange(checkTheNames, 
						index, index+1);
				String character = PDU.bytaArrayToString(tempbyte, 1);
				if(character.equals("\0")){
					condition = false;
				} else {
					name += character;
				}
				index++;
			} while(condition);
			nickNames.add(name);
		}
		String secondName = nickNames.get(1);
	
	assertEquals("Kalle", secondName);
	}
	
	@Test
	public void testNICKSHasRightOpcode() {
		assertEquals(19, opCode );
	}
	
	@Test
	public void testNICKSHasRightLength() {
		assertEquals(11, length);
	}
	
	@Test
	public void testNICKSHasRightPads() {
		//length is only one byte => 3 + 1 = 4 pads
		assertEquals(4, pads);
	}
	
	@Test
	public void testNICKSHasRightBytes() {
		assertEquals(16, bytes.length);
	}
}
