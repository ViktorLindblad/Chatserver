package PDU;

import Server.ByteSequenceBuilder;
import Server.OpCode;

public class QUIT extends PDU {

	public QUIT(){
		bytes = new ByteSequenceBuilder(OpCode.QUIT.value).pad()
				.toByteArray();
	}
	
	public byte[] toByteArray() {
		return bytes;
	}

	
}
