package PDU;

import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Hashtable;

import Server.ByteSequenceBuilder;
import Server.OpCode;

public class NICKS extends PDU{
	
	public NICKS(Hashtable<Socket,String> names){
		
		int numberOfNicks = names.size();
		
		ByteSequenceBuilder BSB = new ByteSequenceBuilder();
		
		for(Socket temp : names.keySet()){
			BSB.append(names.get(temp)
					.getBytes(StandardCharsets.UTF_8)).padshort();			
		}

		bytes = new ByteSequenceBuilder(OpCode.NICKS.value) 
				.append((byte)numberOfNicks)
				.appendShort((short)BSB.size())
				.append(BSB.toByteArray()).pad().toByteArray();		
	}


	public byte[] toByteArray() {
		return bytes;
	}
	
}