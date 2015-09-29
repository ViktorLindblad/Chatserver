package Server;

/**
 * Enum for representing OpCodes.
 */
public enum OpCode {

    REG(0),
    ACK(1),
    ALIVE(2),
    GETLIST(3),
    SLIST(4),
    NOTREG(100),
    MESSAGE(10),
    QUIT(11),
    JOIN(12),
    CHNICK(13),
    UJOIN(16),
    ULEAVE(17),
    UCNICK(18),
    NICKS(19);

    public final byte value;

    OpCode(int value) {
        this.value = (byte) value;
    }
}