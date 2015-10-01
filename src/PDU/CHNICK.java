package PDU;

import java.nio.charset.StandardCharsets;
import Server.ByteSequenceBuilder;
import Server.OpCode;

public class CHNICK extends PDU {
	public CHNICK(String newNickname){

		byte[] nickBytes = newNickname.getBytes(StandardCharsets.UTF_8);
		
		byte length = ((byte)newNickname
				.getBytes(StandardCharsets.UTF_8).length);
		
		bytes = new ByteSequenceBuilder(OpCode.CHNICK.value, length).pad()
				.append(nickBytes).pad().toByteArray();	
				
	}


	public byte[] toByteArray() {
		return bytes;
	}	
	
	
}