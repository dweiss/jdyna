package org.jdyna.view.jme;

import java.io.IOException;
import java.nio.FloatBuffer;

import org.jdyna.view.resources.ResourceUtilities;
import org.jdyna.view.swing.StatusType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jme.image.Texture;
import com.jme.renderer.ColorRGBA;
import com.jme.renderer.Renderer;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.TexCoords;
import com.jme.scene.Text;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.TextureState;
import com.jme.util.TextureManager;
import com.jme.util.geom.BufferUtils;
import com.jmex.font2d.Text2D;

@SuppressWarnings("serial")
public class JMESingleStatus extends Node
{
    private final static Logger logger = LoggerFactory.getLogger(JMESingleStatus.class);

    final String BASE_DIR = "jme/status";
    private Text counterLabel;
    private Quad counterBackground;
    private int x, y, width, height;
    private StatusType statusType;
    private Renderer renderer;
    final int iconWidth = 24;
    final int iconHeight = 24;

    public JMESingleStatus(Renderer renderer, StatusType statusType, int position,
        int statusIndex)
    {
        boolean isRightSide = statusIndex % 2 != 0;
        this.x = (isRightSide ? 1 : 0) * renderer.getWidth() + (isRightSide ? -1 : 1)
            * iconWidth * (position + (isRightSide ? 1 : 0));
        this.y = renderer.getHeight() - (statusIndex / 2) * iconHeight;
        this.height = iconHeight;
        this.width = iconWidth;
        this.statusType = statusType;
        this.renderer = renderer;

        createStatusIcon();
        attachChild(createStatusLabel());
    }

    /**
     * Create empty label for status.
     */
    private Text createStatusLabel()
    {
        return createStatusLabel("");
    }

    /**
     * Create label for status.
     */
    private Text createStatusLabel(String textLabel)
    {
        counterLabel = Text2D.createDefaultTextLabel("status_label", textLabel);
        counterLabel.setTextColor(ColorRGBA.white.clone());
        counterLabel.setLocalTranslation(
            x + width - counterLabel.getWidth() - 1,
            y - height - 1,
            0);
        counterLabel.updateRenderState();

        return counterLabel;
    }

    /**
     * Create one icon of the status bar.
     */
    private void createStatusIcon()
    {
        counterBackground = new Quad("status_icon", this.width, this.height);
        counterBackground.setRenderQueueMode(Renderer.QUEUE_ORTHO);
        counterBackground.setLocalTranslation(
            this.x + this.width / 2,
            this.y - this.height / 2,
            0);
        counterBackground.setLightCombineMode(Spatial.LightCombineMode.Off);

        // create the texture state to handle the texture
        final TextureState ts = renderer.createTextureState();
        // set the texture for this texture state
        ts.setTexture(loadTexture(BASE_DIR + "/" + statusType.name().toLowerCase() + ".png"));
        // activate the texture state
        ts.setEnabled(true);

        // correct texture application:
        final FloatBuffer texCoords = BufferUtils.createVector2Buffer(4);
        texCoords.put(0).put(1);
        texCoords.put(0).put(0);
        texCoords.put(1).put(0);
        texCoords.put(1).put(1);
        // assign texture coordinates to the quad
        counterBackground.setTextureCoords(new TexCoords(texCoords));
        // apply the texture state to the quad
        counterBackground.setRenderState(ts);

        counterBackground.updateRenderState();

        attachChild(counterBackground);
    }

    private Texture loadTexture(String texturePath)
    {
        Texture texture = null;

        try
        {
            texture = TextureManager.loadTexture(
            ResourceUtilities.getResourceURL(texturePath),
            Texture.MinificationFilter.Trilinear,
            Texture.MagnificationFilter.Bilinear,
            1.0f,
            true);
        }
        catch (IOException e)
        {
            logger.error("Loading texture failed.");
        }

        return texture;
    }

    /**
     * Update displaying value.
     */
    public void update(int value)
    {
        detachAllChildren();
        if (value >= 0)
        {
            attachChild(counterBackground);
            attachChild(createStatusLabel(getValue(value)));
        }
    }

    /**
     * Return string that is displaying on the status base on the value. When value is
     * less than 0 no string is displaying, when value is infinity - displaying x.
     */
    public String getValue(int value)
    {
        if (value == Integer.MAX_VALUE) return "x";
        else if (value < 0) return "";
        else return Integer.toString(value);
    }
}
