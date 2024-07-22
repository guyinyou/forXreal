import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Optional;
import java.util.zip.Adler32;

public class McuPacket {
    public final int cmdId; // u16 in Rust, so we'll use int to store 16-bit unsigned value
    public final byte[] data;

    public McuPacket(int cmdId, byte[] data) {
        this.cmdId = cmdId;
        this.data = data;
    }

    public static Optional<McuPacket> deserialize(byte[] data) {
        if (data[0] != (byte) 0xfd || data.length != 0x40) {
            return Optional.empty();
        }

        ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        int head = buffer.get();
        int checksum = buffer.getInt();
        int length = buffer.getShort() & 0xffff; // Convert to unsigned
        int requestId = buffer.getInt();
        int timestamp = buffer.getInt();
        int cmdId = buffer.getShort() & 0xffff; // Convert to unsigned
        byte[] reserved = new byte[5];
        buffer.get(reserved);
        byte[] dataBytes = new byte[42];
        buffer.get(dataBytes);

        byte[] actualData = Arrays.copyOfRange(dataBytes, 0, length - 17);

        // TODO: maybe check CRC?

        return Optional.of(new McuPacket(cmdId, actualData));
    }

    public Optional<byte[]> serialize() {
        ByteBuffer buffer = ByteBuffer.allocate(0x40).order(ByteOrder.LITTLE_ENDIAN);

        buffer.put((byte) 0xfd);
        buffer.putInt(0); // Placeholder for checksum
        buffer.putShort((short) (data.length + 17));
        buffer.putInt(0x1337);
        buffer.putInt(0x0);
        buffer.putShort((short) cmdId);
        buffer.put(new byte[5]); // Reserved bytes default to 0
        buffer.put(data);

        // Calculate checksum
        Adler32 adler32 = new Adler32();
        byte[] rawPacketWithoutChecksum = Arrays.copyOfRange(buffer.array(), 5, buffer.position());
        adler32.update(rawPacketWithoutChecksum);
        long checksum = adler32.getValue();

        // Update checksum in buffer
        buffer.putInt(1, (int) checksum); // Set checksum at offset 1

        return Optional.of(buffer.array());
    }

    @Override
    public String toString() {
        return "McuPacket{" +
            "cmdId=" + cmdId +
            ", data=" + Arrays.toString(data) +
            '}';
    }

    // Getters and other methods...
}
