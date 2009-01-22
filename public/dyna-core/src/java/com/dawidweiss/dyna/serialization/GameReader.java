package com.dawidweiss.dyna.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.h2.compress.LZFInputStream;

import com.dawidweiss.dyna.GameEvent;
import com.dawidweiss.dyna.GameEvent.Type;
import com.google.common.collect.Lists;

/**
 * Reads events from a game saved to a stream.
 */
public final class GameReader
{
    private ObjectInputStream ois;
    
    private final List<GameEvent> events = Lists.newArrayList();
    private int frame;
    private boolean lastFrameRead;

    public GameReader(final InputStream stream)
        throws IOException
    {
        ois = new ObjectInputStream(new LZFInputStream(stream));
    }

    public boolean nextFrame()
        throws IOException
    {
        if (lastFrameRead) return false;

        try
        {
            frame = ois.readInt();
            events.clear();
            short eventCount = ois.readShort();
            while (eventCount-- > 0)
            {
                final GameEvent e = (GameEvent) ois.readObject();
                if (e.type == Type.GAME_OVER)
                {
                    lastFrameRead = true;
                }
                events.add(e);
            }
        }
        catch (ClassNotFoundException e)
        {
            throw new IOException("Class not found when deserializing: " 
                + e.getMessage());            
        }

        return true;
    }

    public int getFrame()
    {
        return frame;
    }

    public List<GameEvent> getEvents()
    {
        return events;
    }

    public void close()
    {
        IOUtils.closeQuietly(ois);
        ois = null;
    }
}
