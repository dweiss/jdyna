package org.jdyna.audio.openal;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.EnumMap;

import org.apache.log4j.Logger;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.util.WaveData;

/**
 * OpenAL-based audio clip player.
 * 
 * TODO: static initialization is brrr...
 * TODO: there is no disposal of clip manager (ever).
 * TODO: how does OpenAL handle multi-threaded calls?
 */
public final class OpenALClipManager<T extends Enum<T>>
{
    private static final Logger logger = Logger.getLogger(OpenALClipManager.class);

    /**
     * Static initializer block.
     */
    static
    {
        try
        {
            AL.create();
        }
        catch (LWJGLException ex)
        {
            throw new RuntimeException(ex);
        }
        
        final int alErrorNO = AL10.alGetError();
        if (alErrorNO != AL10.AL_NO_ERROR)
        {
            throw new RuntimeException("OpenAL initialization error: " + alErrorNO);
        }
    }

    /** Buffers store information about how a sound should be played. */
    private IntBuffer buffer;

    /** Loaded clips and their buffer number. */
    private EnumMap<T, Integer> clipsInBuffer;

    /** Sources are points emitting sound. */
    private IntBuffer source;

    /** Position of the source sound. */
    FloatBuffer sourcePos;

    /** Velocity of the source sound. */
    FloatBuffer sourceVel;

    /** Position of the listener. */
    FloatBuffer listenerPos = BufferUtils.createFloatBuffer(3).put(new float []
    {
        0.0f, 0.0f, 0.0f
    });

    /** Velocity of the listener. */
    FloatBuffer listenerVel = BufferUtils.createFloatBuffer(3).put(new float []
    {
        0.0f, 0.0f, 0.0f
    });

    FloatBuffer listenerOri = BufferUtils.createFloatBuffer(6).put(new float []
    {
        0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f
    });

    /**
     * Creates a sound manager attached to a particular enum constant for selecting sound
     * effects to be played.
     * 
     * @param mixerInfo The mixer to be used, <code>null</code> for the default.
     */
    public <V extends WaveData> OpenALClipManager(Class<T> keyType, EnumMap<T, V> clips)
    {
        listenerPos.flip();
        listenerVel.flip();
        listenerOri.flip();

        final int numBuffers = clips.size();

        buffer = BufferUtils.createIntBuffer(numBuffers);
        source = BufferUtils.createIntBuffer(numBuffers);
        sourcePos = BufferUtils.createFloatBuffer(3 * numBuffers);
        sourceVel = BufferUtils.createFloatBuffer(3 * numBuffers);

        loadAllData(keyType, clips);
        setListenerValues();

        int alErrorNO = AL10.alGetError();
        if (alErrorNO != AL10.AL_NO_ERROR)
        {
            throw new RuntimeException("OpenAL error while loading sounds: " + alErrorNO);
        }
    }

    /*
     * 
     */
    private <V extends WaveData> void loadAllData(Class<T> keyType, EnumMap<T, V> clips)
    {
        AL10.alGenBuffers(buffer);

        clipsInBuffer = new EnumMap<T, Integer>(keyType);
        int posInBuffer = 0;
        for (T e : clips.keySet())
        {
            final WaveData waveFile = clips.get(e);
            clipsInBuffer.put(e, posInBuffer);
            AL10.alBufferData(
                buffer.get(posInBuffer), waveFile.format, waveFile.data, waveFile.samplerate);
            waveFile.dispose();

            posInBuffer++;
        }

        AL10.alGenSources(source);

        final int bufferSize = clips.size();
        for (int i = 0; i < bufferSize; i++)
        {
            AL10.alSourcei(source.get(i), AL10.AL_BUFFER, buffer.get(i));
            AL10.alSourcef(source.get(i), AL10.AL_PITCH, 1.0f);
            AL10.alSourcef(source.get(i), AL10.AL_GAIN, 1.0f);
            AL10.alSource (source.get(i), AL10.AL_POSITION, (FloatBuffer) sourcePos.position(i * 3));
            AL10.alSource (source.get(i), AL10.AL_VELOCITY, (FloatBuffer) sourceVel.position(i * 3));
            AL10.alSourcei(source.get(i), AL10.AL_LOOPING, AL10.AL_FALSE);
        }
    }

    /*
     * 
     */
    public void play(final T effect)
    {
        /*
         * Start or resume the sample.
         */
        AL10.alSourcePlay(source.get(clipsInBuffer.get(effect)));

        /*
         * Check error condition.
         */
        final int alErrorNO = AL10.alGetError();
        if (alErrorNO != AL10.AL_NO_ERROR)
        {
            logger.warn("OpenAL play error: " + alErrorNO);
        }
    }

    /*
     * 
     */
    private void setListenerValues()
    {
        AL10.alListener(AL10.AL_POSITION, listenerPos);
        AL10.alListener(AL10.AL_VELOCITY, listenerVel);
        AL10.alListener(AL10.AL_ORIENTATION, listenerOri);
    }

    /*
     * 
     */
    public void close()
    {
        AL10.alDeleteSources(source);
        AL10.alDeleteBuffers(buffer);
    }
}
