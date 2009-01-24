package org.jdyna.network.packetio;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.StreamCorruptedException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A packet is a unit of information exchanged by parties. A packet is indivisible, and
 * atomic. The object is reusable and should be reused since it holds internal buffers for
 * sending/ receiving data. The body of a packet will change when new packets arrive from
 * the network.
 * <p>
 * At the moment the wire packet's format is:
 * 
 * <pre>
 * BYTES     CONTENT
 * 4         header magic ('dyna')
 * 4         header length (int)
 * 2         flags
 * [length]  packet body.
 * </pre>
 */
public class Packet
{
    private final static Logger logger = LoggerFactory.getLogger(Packet.class);

    /**
     * Packet header magic on the network.
     */
    public static final int HEADER_MAGIC = ('d' << 24) | ('y' << 16) | ('n' << 8) | ('a');

    /**
     * Maximum packet length. Must fit in an UDP datagram as well.
     */
    public static final int MAX_LENGTH = 63000;

    /**
     * Minimum data length to attempt compression.
     */
    private final static int MIN_COMPRESSION_LENGTH = 1024;
    
    /** Offset of the length field. */
    private final static int OFFSET_LENGTH = 4;

    /** Offset of the flags field. */
    private final static int OFFSET_FLAGS = OFFSET_LENGTH + 4;

    /**
     * Packet content is deflated (compressed).
     */
    private final static short CONTENT_DEFLATED = 0x0001; 

    /**
     * Internal buffer for compressing data.
     */
    private byte [] compressedBuffer = new byte [MAX_LENGTH];

    /**
     * Internal buffer for packet content.
     */
    private byte [] buffer;

    /**
     * Packet start in the buffer.
     */
    private int start;

    /**
     * Packet length in the buffer.
     */
    private int length;

    /**
     * Reusable array output stream.
     */
    private static class ReusableByteArrayOutputStream extends ByteArrayOutputStream
    {
        public ReusableByteArrayOutputStream()
        {
            super(MAX_LENGTH);
        }

        /**
         * Public buffer access.
         */
        public byte [] getBuffer()
        {
            return super.buf;
        }
    }

    /**
     * Reusable buffer for writes.
     */
    private final ReusableByteArrayOutputStream baos = new ReusableByteArrayOutputStream();

    /**
     * If writing packet body, store header lenght.
     */
    private int headerLenght;

    /**
     * Create a new packet and initialize buffers.
     */
    public Packet()
    {
        this.buffer = new byte [MAX_LENGTH];
        this.start = 0;
        this.length = 0;
    }

    /**
     * @return Return a data output stream for writing into this packet.
     */
    public DataOutputStream getOutputStream() throws IOException
    {
        baos.reset();
        final DataOutputStream dos = new DataOutputStream(baos);

        /*
         * Write header stuff, with stubs for fields filled later.
         */
        dos.writeInt(HEADER_MAGIC);
        dos.writeInt(0);
        dos.writeShort(0);
        headerLenght = baos.size();

        return dos;
    }

    /**
     * @return Return a data input stream for reading from this packet's body.
     */
    public DataInputStream getInputStream() throws IOException
    {
        baos.reset();

        return new DataInputStream(new ByteArrayInputStream(this.buffer, this.start,
            this.length));
    }

    /**
     * Read packet data from a stream. 
     */
    protected void read(DataInputStream input) throws IOException
    {
        final int magic = input.readInt();
        if (magic != HEADER_MAGIC)
        {
            throw new StreamCorruptedException("Invalid packet magic: " + Integer.toHexString(magic));
        }

        length = input.readInt();
        final short flags = input.readShort();

        // Content is compressed, decompress it.
        if ((flags & CONTENT_DEFLATED) != 0)
        {
            input.readFully(compressedBuffer, 0, length);

            try
            {
                final Inflater decompressor = new Inflater();
                decompressor.setInput(compressedBuffer, 0, length);
                length = decompressor.inflate(buffer);
                decompressor.end();
            }
            catch (DataFormatException e)
            {
                throw new IOException("Malformed compressed packet body.");
            }
        }
        else
        {
            input.readFully(buffer, 0, length);
        }
    }

    /**
     * Return the complete buffer for sending.
     */
    final byte [] getSendBuffer() 
    {
        this.buffer = baos.getBuffer();
        this.length = baos.size();
        this.start = 0;
        int dataLength = length - headerLenght;

        /*
         * Compress buffer if the length exceeds our minimum.
         */
        if (length > MIN_COMPRESSION_LENGTH)
        {
            final Deflater compressor = new Deflater();
            compressor.setInput(buffer, headerLenght, length - headerLenght);
            compressor.finish();
            final int compressedLength = compressor.deflate(compressedBuffer);
            
            /*
             * Only send compressed if we gain more than 10%.
             */
            if (compressedLength < length * 9 / 10)
            {
                System.arraycopy(compressedBuffer, 0, buffer, headerLenght, compressedLength);
                logger.debug("Compressed: [" + dataLength + "->" + compressedLength + "]");
                
                length = compressedLength + headerLenght;
                dataLength = compressedLength;

                SerializationUtils.putShort(buffer, OFFSET_FLAGS, CONTENT_DEFLATED);
            }
        }

        // Update entire packet length.
        SerializationUtils.putInt(buffer, OFFSET_LENGTH, length - headerLenght);

        return buffer;
    }

    /**
     * Start of data in {@link #getSendBuffer()}.
     */
    final int getStart()
    {
        return start;
    }

    /**
     * Total packet lenght.
     */
    final int getLength()
    {
        return length;
    }
}
