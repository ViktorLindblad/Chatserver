package PDU;

import java.nio.charset.StandardCharsets;

import Server.ByteSequenceBuilder;
import Server.OpCode;

public class NICKS extends PDU{
	
	public NICKS(String nickname){
		byte[] nickBytes = nickname.getBytes(StandardCharsets.UTF_8);
		
		byte length = ((byte)nickname
				.getBytes(StandardCharsets.UTF_8).length);
		
		bytes = new ByteSequenceBuilder(OpCode.NICKS.value, length).pad()
				.append(nickBytes).pad().toByteArray();		
	}


	public byte[] toByteArray() {
		return bytes;
	}
	
}