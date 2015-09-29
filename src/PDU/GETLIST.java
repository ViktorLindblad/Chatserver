package PDU;

import Server.ByteSequenceBuilder;
import Server.OpCode;

public class GETLIST extends PDU{

	public GETLIST(){
		bytes = new ByteSequenceBuilder(OpCode.GETLIST.value).pad()
				.toByteArray();
	}
	
	public byte[] toByteArray() {
		return bytes;
	}

	
}
