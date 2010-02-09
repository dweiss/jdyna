package org.jdyna.frontend.swing;

import java.awt.event.KeyEvent;
import java.io.*;

import org.jdyna.network.sockets.Closeables;
import org.jdyna.network.sockets.GameServer;
import org.simpleframework.xml.*;
import org.simpleframework.xml.load.Persister;

import com.jme.system.GameSettings;
import com.jme.system.PropertiesGameSettings;

/**
 * Game configuration (persisted).
 */
@Root(name = "jdyna-config")
public class Configuration
{
    /**
     * Sound engine to use. 
     */
    static enum SoundEngine 
    {
        NONE,
        JAVA_AUDIO,
        OPEN_AL;

        public String toString()
        {
            switch (this)
            {
                case NONE: return "<disabled>";
                case JAVA_AUDIO: return "Java Audio API";
                case OPEN_AL: return "OpenAL";
            }
            throw new RuntimeException("Unreachable.");
        }
    }

    /**
     * Board view type.
     */
    static enum ViewType 
    {
        SWING_VIEW,
        JME_VIEW;

        public String toString()
        {
            switch (this)
            {
                case SWING_VIEW: return "2D - Swing";
                case JME_VIEW: return "3D - JME";
            }
            throw new RuntimeException("Unreachable.");
        }
    }
    
    /**
     * Key bindings.
     */
    public static enum KeyBinding
    {
        KB_UP,
        KB_DOWN,
        KB_LEFT,
        KB_RIGHT,
        KB_BOMB;
        
        public String toString()
        {
            switch (this)
            {
                case KB_UP: return "up";
                case KB_DOWN: return "down";
                case KB_LEFT: return "left";
                case KB_RIGHT: return "right";
                case KB_BOMB: return "bomb";
            }
            throw new RuntimeException("Unreachable.");
        }
    }
    
    /**
     * Enable sounds effects?
     */
    @Element(name = "sound-engine", required = true)
    public SoundEngine soundEngine = SoundEngine.OPEN_AL;

    /**
     * Remember most recent board selection.
     */
    @Element(name = "recent-board-name", required = false)
    public String mostRecentBoard = "classic";

    /**
     * Board view type.
     */
    @Element(name = "view-type", required = true)
    public ViewType viewType = ViewType.SWING_VIEW;
    
    /**
     * Configured key bindings - setting defaults on creation.
     */
    @Element(name = "key-bindings", required = true)
    public final int[] keyBindings = {
        // default player 1 keys
        KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_CONTROL,
        // default player 2 keys
        KeyEvent.VK_R, KeyEvent.VK_F, KeyEvent.VK_D, KeyEvent.VK_G, KeyEvent.VK_Z,
        // default player 3 keys
        KeyEvent.VK_NUMPAD8, KeyEvent.VK_NUMPAD2, KeyEvent.VK_NUMPAD4, KeyEvent.VK_NUMPAD6, KeyEvent.VK_NUMPAD0,
        // default player 4 keys
        KeyEvent.VK_I, KeyEvent.VK_K, KeyEvent.VK_J, KeyEvent.VK_L, KeyEvent.VK_QUOTE
    };
    
    /**
     * TCP port for server configuration.
     */
    @Element(name = "tcp-port", required = true)
    public int TCPport = GameServer.DEFAULT_TCP_CONTROL_PORT;
    /**
     * UDP port for server configuration.
     */
    @Element(name = "udp-port", required = true)
    public int UDPport = GameServer.DEFAULT_UDP_FEEDBACK_PORT;
    /**
     * UDP broadcast port for server configuration.
     */
    @Element(name = "udp-broadcast-port", required = true)
    public int UDPBroadcastPort = GameServer.DEFAULT_UDP_BROADCAST;

    // JME configuration.
    @Element(name = "jme-renderer", required = false)
    public String renderer = GameSettings.DEFAULT_RENDERER;
    @Element(name = "jme-resolution-width", required = false)
    public int resolutionWidth = GameSettings.DEFAULT_WIDTH;
    @Element(name = "jme-resolution-height", required = false)
    public int resolutionHeight = GameSettings.DEFAULT_HEIGHT;
    @Element(name = "jme-depth", required = false)
    public int depth = GameSettings.DEFAULT_DEPTH;
    @Element(name = "jme-frequency", required = false)
    public int frequency = GameSettings.DEFAULT_FREQUENCY;
    @Element(name = "jme-vertical-sync", required = false)
    public boolean vSync = GameSettings.DEFAULT_VERTICAL_SYNC;
    @Element(name = "jme-fullscreen", required = false)
    public boolean fullscreen = GameSettings.DEFAULT_FULLSCREEN;
    @Element(name = "jme-music", required = false)
    public boolean music = GameSettings.DEFAULT_MUSIC;
    @Element(name = "jme-sfx", required = false)
    public boolean sfx = GameSettings.DEFAULT_SFX;
    @Element(name = "jme-depth-bits", required = false)
    public int depthBits = GameSettings.DEFAULT_DEPTH_BITS;
    @Element(name = "jme-alpha-bits", required = false)
    public int alphaBits = GameSettings.DEFAULT_ALPHA_BITS;
    @Element(name = "jme-stencil-bits", required = false)
    public int stencilBits = GameSettings.DEFAULT_STENCIL_BITS;
    @Element(name = "jme-samples", required = false)
    public int samples = GameSettings.DEFAULT_SAMPLES;
    
    /**
     * Returns JME game settings as specified by this <code>Configuration</code>.
     */
    public GameSettings getJMESettings()
    {
        final PropertiesGameSettings settings = new PropertiesGameSettings("", null);
        settings.setRenderer(renderer);
        settings.setWidth(resolutionWidth);
        settings.setHeight(resolutionHeight);
        settings.setDepth(depth);
        settings.setFrequency(frequency);
        settings.setVerticalSync(vSync);
        settings.setFullscreen(fullscreen);
        settings.setMusic(music);
        settings.setSFX(sfx);
        settings.setDepthBits(depthBits);
        settings.setAlphaBits(alphaBits);
        settings.setStencilBits(stencilBits);
        settings.setSamples(samples);
        return settings;
    }
    
    /**
     * Save the state of this object to an XML.
     */
    public String save() throws IOException
    {
        try
        {
            final Serializer serializer = new Persister();
            final StringWriter sw = new StringWriter();
            serializer.write(this, sw);
            return sw.toString();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new IOException("Could not save configuration.", e);
        }
    }
    
    /**
     * Load the state of configuration from an XML.
     */
    public static Configuration load(InputStream is) throws IOException
    {
        try
        {
            final Serializer serializer = new Persister();
            return serializer.read(Configuration.class, is);
        }
        catch (Exception e)
        {
            throw new IOException("Could not load configuration.", e);
        }
        finally
        {
            Closeables.close(is);
        }
    }
}
