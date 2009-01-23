package org.jdyna.network.sockets;

import java.io.*;

/**
 * Convert Java {@link Serializable} object to and from packet bodies. Supports
 * compression of package body, if needed.
 */
final class ObjectPacket
{
    /**
     * Deserialize a serializable object from a packet body.
     */
    @SuppressWarnings("unchecked")
    public static <T> T deserialize(Packet p) throws IOException
    {
        InputStream is = new ByteArrayInputStream(p.buffer, p.start, p.length);
        final ObjectInputStream ois = new ObjectInputStream(is);
        try
        {
            return (T) ois.readObject();
        }
        catch (ClassCastException e)
        {
            throw new IOException(
                "Cannot deserialize packet, unexpected class on input.", e);
        }
        catch (ClassNotFoundException e)
        {
            throw new IOException("Cannot deserialize packet, class not found: "
                + e.getMessage());
        }
    }

    /**
     * Serialize a serializable packet to an existing buffer.
     */
    public static Packet serialize(Serializable object) throws IOException
    {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(object);
        oos.close();

        final Packet p = new Packet();
        p.setBody(baos.toByteArray(), 0, baos.size());

        return p;
    }
}
