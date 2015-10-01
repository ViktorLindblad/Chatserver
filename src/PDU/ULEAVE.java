package PDU;

import java.nio.charset.StandardCharsets;

import Server.ByteSequenceBuilder;
import Server.OpCode;

public class ULEAVE extends PDU{

	public ULEAVE(String name){
		
		long time = System.currentTimeMillis();
	
		bytes = new ByteSequenceBuilder(OpCode.ULEAVE.value)
		.append((byte)name.getBytes(StandardCharsets.UTF_8).length).pad()
		.appendInt((int)time)
		.append(name.getBytes(StandardCharsets.UTF_8)).pad().toByteArray();
	}
	
	public byte[] toByteArray() {
	return bytes;
	}
}
