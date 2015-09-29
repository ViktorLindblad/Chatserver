package PDU;

import java.nio.charset.StandardCharsets;
import Server.ByteSequenceBuilder;
import Server.OpCode;

public class REG extends PDU{

	public REG(String serverName, int TCPport){
		
		byte[] reg = serverName.getBytes(StandardCharsets.UTF_8);
		byte length = ((byte)serverName.
				getBytes(StandardCharsets.UTF_8).length);
		
		bytes = new ByteSequenceBuilder(OpCode.REG.value, length)
			.appendShort((short)TCPport)
			.append(reg).pad().toByteArray();
	}


	public byte[] toByteArray() {
		return bytes;
	}
}