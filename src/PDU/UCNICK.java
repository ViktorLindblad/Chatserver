package PDU;

import Server.ByteSequenceBuilder;
import Server.OpCode;

public class UCNICK extends PDU{

	public UCNICK(String oldName, String newName){
		
		long time = System.currentTimeMillis() / 1000L;
		
		bytes = new ByteSequenceBuilder(OpCode.UCNICK.value)
		.append((byte)oldName.length())
		.append((byte)newName.length()).pad()
		.appendInt((int)time)
		.append(oldName.getBytes()).pad()
		.append(newName.getBytes()).pad().toByteArray();
	}
	
	public byte[] toByteArray() {
		return bytes;
	}

	
}
