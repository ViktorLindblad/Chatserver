package PDU;

import java.nio.charset.StandardCharsets;
import java.util.zip.Checksum;

import Server.ByteSequenceBuilder;
import Server.OpCode;

public class MESS extends PDU{

	
	public MESS(String message, String name, boolean isClient) {
		
		long time = System.currentTimeMillis() / 1000L;
		int checksum;
		byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
		byte lengthMessage = ((byte)message.getBytes
									(StandardCharsets.UTF_8).length);
		
		if(isClient){
			byte length = 0; 
			time = 0;
			
			bytes = new ByteSequenceBuilder(OpCode.MESSAGE.value).pad()
					.append(length)
					.appendShort(lengthMessage).pad()
					.appendInt((int)time)
					.append(messageBytes).pad().toByteArray();
			
			checksum = bytes.length - 255;
			bytes = null;
			
			bytes = new ByteSequenceBuilder(OpCode.MESSAGE.value).pad()
					.append(length)
					.append((byte)checksum)
					.appendShort(lengthMessage).pad()
					.appendInt((int)time)
					.append(messageBytes).pad().toByteArray();
			
		} else {
			byte length = (byte) name.getBytes().length;

			bytes = new ByteSequenceBuilder(OpCode.MESSAGE.value).pad()
					.append(length)
					.appendShort(lengthMessage).pad()
					.appendInt((int)time)
					.append(messageBytes).pad()
					.append(name.getBytes(StandardCharsets.UTF_8))
					.pad().toByteArray();
			
			checksum = bytes.length - 255;
			bytes = null;
			
			bytes = new ByteSequenceBuilder(OpCode.MESSAGE.value).pad()
					.append(length)
					.append((byte)checksum)
					.appendShort(lengthMessage).pad()
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
