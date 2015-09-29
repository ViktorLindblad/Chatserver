package Server;
import java.util.ArrayList;

/**
 * Class for building byte sequences by appending bytes, shorts and integers.
 *
 * <hr/>
 *
 * The byte array representation of a JoinPDU could for example be built
 * with the following code:
 *
 * <p><pre>
 *     <code>
 *     byte[] nickBytes = nickname.getBytes(StandardCharsets.UTF_8);
 *     new ByteSequenceBuilder(OpCode.JOIN, (byte) nickBytes.length)).pad()
 *              .append(nickBytes).pad()
 *              .toByteArray();
 *     </code>
 * </pre></p>
 */
public final class ByteSequenceBuilder {

    private final ArrayList<Byte> bytes = new ArrayList<>();

    public ByteSequenceBuilder(byte... bytes) {
        append(bytes);
    }

    /**
     * @param bytes The bytes appended at the end of the sequence.
     * @return The ByteSequenceBuilder.
     */
    public ByteSequenceBuilder append(byte... bytes) {
        for (byte b : bytes) {
            this.bytes.add(b);
        }
        return this;
    }
    
    public ByteSequenceBuilder appendInteger(int i) {
    	bytes.add((byte) (i & 0xff));
    	return this;
    }

    /**
     * @param s The short appended as two bytes at the end of the sequence.
     * @return The ByteSequenceBuilder.
     */
    public ByteSequenceBuilder appendShort(short s) {
        bytes.add((byte) ((s >> 8) & 0xff));
        bytes.add((byte) (s & 0xff));
        return this;
    }

    /**
     * @param i The int appended at the end of the sequence as four bytes.
     * @return The ByteSequenceBuilder.
     */
    public ByteSequenceBuilder appendInt(int i) {
        bytes.add((byte) ((i >> 24) & 0xff));
        bytes.add((byte) ((i >> 16) & 0xff));
        bytes.add((byte) ((i >> 8) & 0xff));
        bytes.add((byte) (i & 0xff));
        return this;
    }

    /**
     * @return A byte array with the bytes in the sequence.
     */
    public byte[] toByteArray() {
        byte[] result = new byte[bytes.size()];
        for (int i = 0; i < bytes.size(); i++) {
            result[i] = bytes.get(i);
        }
        return result;
    }

    /**
     * @return The number of bytes in the sequence.
     */
    public int size() {
        return bytes.size();
    }

    /**
     * Pads the sequence with zero valued bytes so that its length is
     * divisible by 4.
     *
     * @return The ByteSequenceBuilder.
     */
    public ByteSequenceBuilder pad() {
        int toPad = bytes.size() % 4 == 0 ? 0 : 4 - (bytes.size() % 4);
        for (int i = 0; i < toPad; i++) {
            bytes.add((byte) 0);
        }
        return this;
    }
}