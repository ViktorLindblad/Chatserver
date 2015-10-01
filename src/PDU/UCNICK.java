package PDU;

import java.nio.charset.StandardCharsets;

import Server.ByteSequenceBuilder;
import Server.OpCode;

public class UCNICK extends PDU{

	public UCNICK(String oldName, String newName){
		
		long time = System.currentTimeMillis();
		
		bytes = new ByteSequenceBuilder(OpCode.UCNICK.value)
		.append((byte)oldName.getBytes(StandardCharsets.UTF_8).length)
		.append((byte)newName.getBytes(StandardCharsets.UTF_8).length).pad()
		.appendInt((int)time)
		.append(oldName.getBytes(StandardCharsets.UTF_8)).pad()
		.append(newName.getBytes(StandardCharsets.UTF_8)).pad().toByteArray();
	}
	
	public byte[] toByteArray() {
		return bytes;
	}

	
}
