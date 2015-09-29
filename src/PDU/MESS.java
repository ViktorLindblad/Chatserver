package PDU;

import java.nio.charset.StandardCharsets;
import java.util.zip.Checksum;

import Server.ByteSequenceBuilder;
import Server.OpCode;

public class MESS extends PDU{

	private Checksum checksum;
	
	public MESS(String message, String name, boolean isClient) {
		
		long time = System.currentTimeMillis() / 1000L;
		
		byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
		byte lengthMessage = ((byte)message.getBytes
									(StandardCharsets.UTF_8).length);
		
		if(isClient){
			byte length = 0; 
			
			
			bytes = new ByteSequenceBuilder(OpCode.MESSAGE.value).pad()
					.append(length)
					.appendShort(lengthMessage).pad()
					.appendInt((int)time)
					.append(messageBytes).pad().toByteArray();
			
			checksum.update(bytes, 0, bytes.length);
			bytes = null;
			
			bytes = new ByteSequenceBuilder(OpCode.MESSAGE.value).pad()
					.append(length)
					.append((byte)checksum.getValue())
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
					.append(name.getBytes()).pad().toByteArray();
			
			checksum.update(bytes, 0, bytes.length);
			bytes = null;
			
			bytes = new ByteSequenceBuilder(OpCode.MESSAGE.value).pad()
					.append(length)
					.append((byte)checksum.getValue())
					.appendShort(lengthMessage).pad()
					.appendInt((int)time)
					.append(messageBytes).pad()
					.append(name.getBytes()).pad().toByteArray();
		}	
	}
	
	public byte[] toByteArray() {
		return bytes;
	}

}
