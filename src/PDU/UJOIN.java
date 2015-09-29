package PDU;

import Server.ByteSequenceBuilder;
import Server.OpCode;

public class UJOIN extends PDU{

	public UJOIN(String name){
		
		long time = System.currentTimeMillis() / 1000L;
		
		bytes = new ByteSequenceBuilder(OpCode.UJOIN.value)
		.append((byte)name.getBytes().length).pad()
		.appendInt((int)time)
		.append(name.getBytes()).pad().toByteArray();
	}
	
	public byte[] toByteArray() {
		return bytes;
	}

	
	
}
