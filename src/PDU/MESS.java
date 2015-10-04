package PDU;

import java.nio.charset.StandardCharsets;
import Server.ByteSequenceBuilder;
import Server.OpCode;

public class MESS extends PDU{

	
	public MESS(String message, String name, boolean isClient) {
		
		long time = System.currentTimeMillis();
		int checksum = 0;
		byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
		int lengthMessage = message.getBytes
									(StandardCharsets.UTF_8).length;
		int checkLength = 0;
		if(isClient){

			time = 0;
			
			bytes = new ByteSequenceBuilder(OpCode.MESSAGE.value).padshort()
					.append((byte)0).pad()
					.appendShort((short)lengthMessage).pad()
					.appendInt(0)
					.append(messageBytes).pad().toByteArray();
			
			checkLength = bytes.length;

			while(checkLength>255){
				checkLength -= 255;
				checksum = checkLength;	
			}
			bytes = null;
			
			
			bytes = new ByteSequenceBuilder(OpCode.MESSAGE.value).padshort()
					.append((byte)checksum)
					.appendShort((short)lengthMessage).pad()
					.append(messageBytes).pad().toByteArray();
		} else {
			int length = name.getBytes().length;

			bytes = new ByteSequenceBuilder(OpCode.MESSAGE.value).padshort()
					.append((byte)length)
					.appendShort((short)lengthMessage).pad()
					.appendInt((int)time)
					.append(messageBytes).pad()
					.append(name.getBytes(StandardCharsets.UTF_8))
					.pad().toByteArray();
			
			checkLength = bytes.length;

			while(checkLength>255){
				checkLength -= 255;
				checksum = checkLength;	
			}
				
		
			bytes = null;
			
			bytes = new ByteSequenceBuilder(OpCode.MESSAGE.value).padshort()
					.append((byte)length)
					.append((byte)checksum)
					.appendShort((short)lengthMessage).pad()
					.appendInt((int)time)
					.append(messageBytes).pad()
					.append(name.getBytes(StandardCharsets.UTF_8))
					.pad().toByteArray();

		}	
	}
	
	public byte[] toByteArray() {
		return bytes;
	}

}
