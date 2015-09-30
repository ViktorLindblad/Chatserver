package PDU;

import java.nio.charset.StandardCharsets;
import java.util.zip.Checksum;

import Server.ByteSequenceBuilder;
import Server.OpCode;

public class MESS extends PDU{

	
	public MESS(String message, String name, boolean isClient) {
		
		long time = System.currentTimeMillis() / 1000L;
		int checksum = 0;
		byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
		int lengthMessage = message.getBytes
									(StandardCharsets.UTF_8).length;
		System.out.println("messageLength: "+lengthMessage);
		if(isClient){
			byte length = 0; 
			time = 0;
			
			bytes = new ByteSequenceBuilder(OpCode.MESSAGE.value).pad()
					.append(length)
					.appendShort((short)lengthMessage).pad()
					.appendInt((int)time)
					.append(messageBytes).pad().toByteArray();
			if(bytes.length>255) {
				checksum = bytes.length - 255;
			}
			bytes = null;
			
			System.out.println(checksum);
			
			bytes = new ByteSequenceBuilder(OpCode.MESSAGE.value).pad()
					.append(length)
					.append((byte)checksum)
					.appendShort((short)lengthMessage).pad()
					.appendInt((int)time)
					.append(messageBytes).pad().toByteArray();
			
		} else {
			byte length = (byte) name.getBytes().length;

			bytes = new ByteSequenceBuilder(OpCode.MESSAGE.value).pad()
					.append(length)
					.appendShort((short)lengthMessage).pad()
					.appendInt((int)time)
					.append(messageBytes).pad()
					.append(name.getBytes(StandardCharsets.UTF_8))
					.pad().toByteArray();
			
			if(bytes.length>255) {
				checksum = bytes.length - 255;
			}			bytes = null;
			
			bytes = new ByteSequenceBuilder(OpCode.MESSAGE.value).pad()
					.append(length)
					.append((byte)checksum)
					.appendShort((short)lengthMessage).pad()
					.appendInt((int)time)
					.append(messageBytes).pad()
					.append(name.getBytes(StandardCharsets.UTF_8))
					.pad().toByteArray();
		}	
		System.out.println(bytes.length);
	}
	
	public byte[] toByteArray() {
		return bytes;
	}

}
