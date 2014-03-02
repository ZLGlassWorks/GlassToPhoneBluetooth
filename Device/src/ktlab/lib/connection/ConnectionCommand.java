package ktlab.lib.connection;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ConnectionCommand {

    // Header (type + optionLen) length
    public static final int HEADER_LENGTH = Integer.SIZE / Byte.SIZE + 1;

    // Command fields
    public byte type;
    public int optionLen;
    public byte[] option;

    /**
     * Constructor Create BTCommand without option.
     *
     * @param type
     *            Commands type
     */
    public ConnectionCommand(byte type) {
        this(type, null);
    }

    /**
     * Constructor Create BTCommand with option.
     *
     * @param type
     *            Commands type
     * @param option
     *            Commands option
     */
    public ConnectionCommand(byte type, byte[] option) {
        this.type = type;
        if (option != null) {
            optionLen = option.length;
            this.option = new byte[option.length];
            System.arraycopy(option, 0, this.option, 0, option.length);
        } else {
            optionLen = 0;
            this.option = new byte[0];
        }
    }

    /**
     * Convert BTCommand to byte array
     *
     * @param command
     *            target command
     * @param order
     *            byte order
     * @return byte array
     * @hide
     */
    protected static byte[] toByteArray(ConnectionCommand command, ByteOrder order) {
        byte[] ret = new byte[command.optionLen + HEADER_LENGTH];
        // set byte order
        ByteBuffer bf = ByteBuffer.wrap(ret).order(order);
        bf.put(command.type);
        bf.putInt(command.optionLen);
        bf.put(command.option);

        return ret;
    }

    /**
     * Convert byte array to BTCommand
     *
     * @param data
     *            byte array
     * @param order
     *            Byte order
     * @return BTCommand
     * @hide
     */
    protected static ConnectionCommand fromByteArray(byte[] data, ByteOrder order) {
        ByteBuffer bf = ByteBuffer.wrap(data).order(order);
        byte type = bf.get();
        int len = bf.getInt();
        byte[] option = new byte[len];
        bf.get(option);

        return new ConnectionCommand(type, option);
    }

    /**
     * create BTCommand from Header and Option
     *
     * @param header
     *            header(byte array)
     * @param option
     *            option(byte array)
     * @return BTCommand
     * @hide
     */
    protected static ConnectionCommand fromHeaderAndOption(byte[] header, byte[] option,
            ByteOrder order) {
        byte[] data = new byte[header.length + option.length];

        System.arraycopy(header, 0, data, 0, header.length);
        System.arraycopy(option, 0, data, header.length, option.length);

        return fromByteArray(data, order);
    }
}
