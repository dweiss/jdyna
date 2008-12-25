package com.dawidweiss.dyna.serialization;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.h2.compress.LZFOutputStream;

import com.dawidweiss.dyna.GameEvent;
import com.dawidweiss.dyna.IGameEventListener;

/**
 * A {@link IGameEventListener} that saves snapshots from the game progress
 * to an external stream. Light LZF compression is used to compress frame data.
 */
public final class GameWriter implements IGameEventListener
{
    private ObjectOutputStream oos;

    public GameWriter(OutputStream os)
        throws IOException
    {
        this.oos = new ObjectOutputStream(
            new LZFOutputStream(os));
    }

    @Override
    protected void finalize() throws Throwable
    {
        super.finalize();
        IOUtils.closeQuietly(oos);
    }

    @Override
    public void onFrame(int frame, List<GameEvent> events)
    {
        if (oos == null)
            return;

        try
        {
            boolean closeAtEnd = false;
            oos.writeInt(frame);
            oos.writeShort((short) events.size());
            for (GameEvent ge : events)
            {
                oos.writeObject(ge);

                if (ge.type == GameEvent.Type.GAME_OVER)
                {
                    closeAtEnd = true;
                }
            }
            oos.flush();
            
            if (closeAtEnd)
            {
                oos.close();
                oos = null;
            }
        }
        catch (IOException e)
        {
            Logger.getAnonymousLogger().severe("Failed writing game log: "
                + e.getMessage());
            IOUtils.closeQuietly(oos);
            oos = null;
        }
    }
}
