package org.jdyna.frontend.swing;

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
    public static enum SoundEngine 
    {
        NONE,
        JAVA_AUDIO;
        
        public String toString()
        {
            switch (this)
            {
                case NONE: return "<disabled>";
                case JAVA_AUDIO: return "Java Audio API";
            }
            throw new RuntimeException("Unreachable.");
        }
    }

    /**
     * Enable sounds effects?
     */
    @Element(name = "sound-engine", required = true)
    public SoundEngine soundEngine = SoundEngine.JAVA_AUDIO;

    /**
     * Remember most recent board selection.
     */
    @Element(name = "recent-board-name", required = false)
    public String mostRecentBoard = "classic";
    
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
