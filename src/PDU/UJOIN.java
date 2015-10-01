package PDU;

import java.nio.charset.StandardCharsets;

import Server.ByteSequenceBuilder;
import Server.OpCode;

public class UJOIN extends PDU{

	public UJOIN(String name){
		
		long time = System.currentTimeMillis();
		
		bytes = new ByteSequenceBuilder(OpCode.UJOIN.value)
		.append((byte)name.getBytes(StandardCharsets.UTF_8).length).pad()
		.appendInt((int)time)
		.append(name.getBytes(StandardCharsets.UTF_8)).pad().toByteArray();
	}
	
	public byte[] toByteArray() {
		return bytes;
	}

	
	
}
