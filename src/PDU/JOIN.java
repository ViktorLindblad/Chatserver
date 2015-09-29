package PDU;

import java.nio.charset.StandardCharsets;

import Server.ByteSequenceBuilder;
import Server.OpCode;

public class JOIN extends PDU {

	public JOIN(String nickname){
		
		byte[] nickBytes = nickname.getBytes(StandardCharsets.UTF_8);
		byte length = ((byte)nickname
				.getBytes(StandardCharsets.UTF_8).length);
		
		bytes = new ByteSequenceBuilder(OpCode.JOIN.value, length).pad()
				.append(nickBytes).pad().toByteArray();			
	
	}


	public byte[] toByteArray() {
		return bytes;
	}


}