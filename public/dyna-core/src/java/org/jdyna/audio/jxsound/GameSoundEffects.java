package org.jdyna.audio.jxsound;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import javax.sound.sampled.*;

import org.jdyna.*;

import com.google.common.collect.Maps;

/**
 * Sound listener and player based on Java's built-in <code>javax.sound</code> package. 
 */
public final class GameSoundEffects implements IGameEventListener
{
    /**
     * Audio manager for this listener.
     */
    private ClipManager<SoundEffect> audioManager;

    /**
     * Load the default samples. 
     */
    public GameSoundEffects()
    {
        final EnumMap<SoundEffect, List<Clip>> clips = Maps.newEnumMap(SoundEffect.class);

        clips.put(SoundEffect.BOMB, load("bomb.wav", 4));
        clips.put(SoundEffect.BONUS, load("bonus.wav", 4));
        clips.put(SoundEffect.DYING, load("dying.wav", 4));

        this.audioManager = new ClipManager<SoundEffect>(SoundEffect.class, clips);
    }

    /*
     * 
     */
    public void onFrame(int frame, List<? extends GameEvent> events)
    {
        for (GameEvent e : events)
        {
            if (e.type == GameEvent.Type.SOUND_EFFECT)
            {
                final SoundEffectEvent event = (SoundEffectEvent) e;

                /*
                 * TODO: The count of events per frame is currently not used,
                 * "fuzzification" (small delays) of replay of multiple events would
                 * be probably a good idea, as reported by A. Kłopotek.
                 */
                audioManager.play(event.effect);
            }
        }
    }

    /**
     * Dispose of all native resources stored in the listener.
     */
    public void dispose()
    {
       this.audioManager.close();
       this.audioManager = null;
    }
     
    /*
     * 
     */
    private List<Clip> load(String resource, int count)
    {
        try
        {
            return Arrays.asList(ClipManager.load(open(resource), count));
        }
        catch (Exception e)
        {
            throw new RuntimeException("Could not open audio clip: " + resource, e);
        }
    }

    /*
     * 
     */
    private InputStream open(String resource) throws IOException
    {
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        final InputStream is = contextClassLoader.getResourceAsStream(resource);
        if (is == null)
        {
            throw new IOException("Resource not found: " + resource);
        }
        return is;
    }
}
