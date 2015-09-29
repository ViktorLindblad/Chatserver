package PDU;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import Server.ByteSequenceBuilder;
import Server.OpCode;

public class NICKS extends PDU{
	
	public NICKS(ArrayList<String> names){
		
		int numberOfNicks = names.size();
		
		ByteSequenceBuilder BSB = new ByteSequenceBuilder();
		for(int i = 0; i < numberOfNicks; i++){
			BSB.append(names.get(i).getBytes(StandardCharsets.UTF_8)).pad();
		}

		bytes = new ByteSequenceBuilder(OpCode.NICKS.value) 
				.append((byte)numberOfNicks).pad()
				.append(BSB.toByteArray()).pad().toByteArray();		
	}


	public byte[] toByteArray() {
		return bytes;
	}
	
}