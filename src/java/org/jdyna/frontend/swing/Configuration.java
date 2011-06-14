package org.jdyna.frontend.swing;

import java.awt.event.KeyEvent;
import java.io.*;

import org.jdyna.network.sockets.Closeables;
import org.simpleframework.xml.*;
import org.simpleframework.xml.load.Persister;

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
