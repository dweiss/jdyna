package org.jdyna.network.packetio;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * A packet that stores and retrieves an instance of the {@link Serializable} interface
 * and two additional custom fields.
 * <p>
 * At the moment the wire packet's format is:
 * 
 * <pre>
 * BYTES             CONTENT
 * [packet header]   Packet header
 * 4                 custom field 1 (for filtering)
 * 4                 custom field 2 (for filtering)
 * [data]            Serializable data.
 * </pre>
 */
public class SerializablePacket extends Packet
{
    private int custom1;
    private int custom2;
    private DataInputStream body;

    /**
     * Read packet header and custom fields. Prepare the body for deserialization.
     */
    @Override
    protected void read(DataInputStream input) throws IOException
    {
        /* Read packet header and body into the superclass. */
        super.read(input);

        body = super.getInputStream();
        custom1 = body.readInt();
        custom2 = body.readInt();
    }

    /**
     * Deserialize a serializable object from a packet body.
     */
    public <T> T deserialize(Class<T> clazz) throws IOException
    {
        final ObjectInputStream ois = new ObjectInputStream(body);

        try
        {
            return clazz.cast(ois.readObject());
        }
        catch (ClassCastException e)
        {
            throw new IOException("Cannot deserialize packet, expected class: "
                + clazz.getSimpleName(), e);
        }
        catch (ClassNotFoundException e)
        {
            throw new IOException("Cannot deserialize packet, class not found: "
                + e.getMessage());
        }
    }

    /**
     * Serialize a serializable object to this packet's buffer.
     */
    public void serialize(int custom1, int custom2, Serializable object)
        throws IOException
    {
        final DataOutputStream os = super.getOutputStream();
        os.writeInt(custom1);
        os.writeInt(custom2);

        final ObjectOutputStream oos = new ObjectOutputStream(os);
        oos.writeObject(object);
        oos.close();
    }

    /**
     * @return Return the first custom field.
     */
    public int getCustom1()
    {
        return custom1;
    }

    /**
     * @return Return the second custom field.
     */
    public int getCustom2()
    {
        return custom2;
    }
}
