package org.jdyna.audio.openal;

import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.List;

import org.jdyna.*;
import org.jdyna.view.resources.ResourceUtilities;
import org.lwjgl.util.WaveData;

import com.google.common.collect.Maps;

/**
 * Sound listener and player based on OpenAL library (native).
 */
public final class OpenALSFX implements IGameEventListener
{
    /**
     * Audio manager for this listener.
     */
    private OpenALClipManager<SoundEffect> audioManager;

    /**
     * Load the default samples.
     */
    public OpenALSFX()
    {
        final EnumMap<SoundEffect, WaveData> clips = Maps.newEnumMap(SoundEffect.class);

        clips.put(SoundEffect.BOMB, load("bomb.wav"));
        clips.put(SoundEffect.BONUS, load("bonus.wav"));
        clips.put(SoundEffect.DYING, load("dying.wav"));

        this.audioManager = new OpenALClipManager<SoundEffect>(SoundEffect.class, clips);
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
                 * "fuzzification" (small delays) of replay of multiple events would be
                 * probably a good idea, as reported by A. Kłopotek.
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
    private WaveData load(String resource)
    {
        try
        {
            InputStream stream = ResourceUtilities.open(resource);
            if (stream == null)
                throw new IOException("No resource: " + resource);
            WaveData wd = WaveData.create(stream);
            if (wd == null)
                throw new IOException("WaveData creation failed: " + resource);
            return wd;
        }
        catch (Exception e)
        {
            throw new RuntimeException("Could not open audio clip: " + resource, e);
        }
    }
}
