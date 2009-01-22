package com.dawidweiss.dyna.audio.jxsound;

import java.io.*;
import java.util.*;

import javax.sound.sampled.*;

import org.apache.commons.io.IOUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Sound effect manager manages audio samples and their concurrent replay in the audio
 * mixer.
 */
public final class ClipManager<T extends Enum<T>>
{
    /**
     * An exclusive monitor for managing clips queue. 
     */
    private final Object lock = new Object();
    
    /**
     * Clips currently being replayed.
     */
    private final EnumMap<T, List<Clip>> playing;

    /**
     * Clips ready to be replayed.
     */
    private final EnumMap<T, List<Clip>> ready;

    /**
     * Creates a sound manager attached to a particular enum constant for selecting sound
     * effects to be played.
     * 
     * @param mixerInfo The mixer to be used, <code>null</code> for the default.
     */
    public <V extends Collection<Clip>> ClipManager(Class<T> keyType, EnumMap<T, V> clips)
    {
        playing = Maps.newEnumMap(keyType);
        ready = Maps.newEnumMap(keyType);

        for (T e : clips.keySet())
        {
            ready.put(e, Lists.newArrayList(clips.get(e)));
            playing.put(e, Lists.<Clip>newArrayList());
        }
    }

    /**
     * Play a given audio sample. If the pool of clips for the given sample
     * has been exhausted, no action is taken. 
     */
    public void play(final T effect)
    {
        final Clip clip; 
        synchronized (lock)
        {
            final List<Clip> effectClips = ready.get(effect);
            if (effectClips == null || effectClips.isEmpty())
            {
                // Sample does not exist for such key or samples queue exhausted.
                return;
            }

            clip = effectClips.remove(effectClips.size() - 1);
            playing.get(effect).add(clip);
        }

        clip.addLineListener(new LineListener()
        {
            public void update(LineEvent event)
            {
                if (event.getType() == LineEvent.Type.STOP)
                {
                    synchronized (lock)
                    {
                        ready.get(effect).add((Clip) event.getSource());
                    }
                }
            }
        });

        final int frame = 0;
        clip.setFramePosition(frame);
        clip.start();
    }

    /**
     * Close and release any audio resources.
     */
    public void close()
    {
        synchronized (lock)
        {
            for (List<Clip> cl : ready.values())
            {
                for (Clip c : cl)
                {
                    c.close();
                }
                cl.clear();
            }
    
            for (List<Clip> cl : playing.values())
            {
                for (Clip c : cl)
                {
                    c.close();
                }
                cl.clear();
            }
        }      
    }

    /**
     * Loads a sample (clip) in multiple copies, closing the input stream (always).
     * <p>
     * I experimented a bit with various ways of loading clips and {@link AudioSystem#getLine(Clip)}
     * seems to work most reliable on multiple systems (Linux, Windows). It selects proper
     * mixer internally and the logic of selecting such a mixer is not trivial. 
     */
    public static Clip [] load(InputStream is, int copies)
        throws IOException, UnsupportedAudioFileException, LineUnavailableException
    {
        final byte [] sample;
        try
        {
            sample = IOUtils.toByteArray(is);
        }
        finally
        {
            is.close();
        }

        final Clip [] clips = new Clip [copies];
        for (int i = 0; i < copies; i++)
        {
            final AudioInputStream ais = AudioSystem.getAudioInputStream(
                new ByteArrayInputStream(sample));
            final AudioFormat format = ais.getFormat();
            final DataLine.Info info = new DataLine.Info(Clip.class, format);

            clips[i] = (Clip) AudioSystem.getLine(info);
            clips[i].open(ais);
        }
        
        return clips;
    }
}
