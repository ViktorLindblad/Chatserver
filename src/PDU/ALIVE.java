package PDU;

import Server.ByteSequenceBuilder;
import Server.OpCode;

public class ALIVE extends PDU {
	
	public ALIVE(int clients, int serverID){
		bytes = new ByteSequenceBuilder
				(OpCode.ALIVE.value)
				.append((byte)clients)
				.appendShort((short)serverID).pad()
				.toByteArray();
	}


	public byte[] toByteArray() {
		return bytes;
	}

}