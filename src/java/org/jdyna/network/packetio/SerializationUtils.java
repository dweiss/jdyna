package org.jdyna.network.packetio;

/**
 * Serialization of primitive types.
 */
public final class SerializationUtils
{
    private SerializationUtils()
    {
        // No instances
    }
    
    public static short getShort(byte [] b, int off)
    {
        return (short) (((b[off + 1] & 0xff) << 0) + ((b[off]) << 8));
    }

    public static int getInt(byte [] b, int off)
    {
        return ((b[off + 3] & 0xff) << 0)  + ((b[off + 2] & 0xff) << 8)
             + ((b[off + 1] & 0xff) << 16) + ((b[off + 0]) << 24);
    }

    public static void putShort(byte [] b, int off, short val)
    {
        b[off + 0] = (byte) (val >>> 8);
        b[off + 1] = (byte) (val);
    }

    public static void putInt(byte [] b, int off, int val)
    {
        b[off + 0] = (byte) (val >>> 24);
        b[off + 1] = (byte) (val >>> 16);
        b[off + 2] = (byte) (val >>> 8);
        b[off + 3] = (byte) (val);
    }
}
