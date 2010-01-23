package org.jdyna.view.jme;

import org.jdyna.CellType;
import org.jdyna.view.jme.resources.MeshFactory;
import org.jdyna.view.swing.StatusType;

import com.jme.math.FastMath;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jmex.font3d.Font3D;
import com.jmex.font3d.Text3D;

@SuppressWarnings("serial")
public class JMESingleStatus extends Node
{
    /**
     * 3d model for the status icon.
     */
    Spatial model;
    
    /**
     * The text that displaying current value.
     */
    Text3D text3d;
    
    public JMESingleStatus(StatusType type, float position, Font3D font3d)
    {
        model = createModel(type);
        model.setLocalTranslation(position, 0, 0);
        model.setLocalRotation(new Quaternion(new float[]{-FastMath.PI/3, 0, 0}));
        attachChild(model);
        model.updateRenderState();
        
        text3d = font3d.createText("", 0.5f, 0);
        text3d.setFontColor(ColorRGBA.red.clone());
        text3d.setLocalTranslation(position, -0.1f, 0.4f);
        text3d.setLocalScale(new Vector3f(0.5f, 0.5f, 0.01f));
        text3d.setLocalRotation(new Quaternion(new float[]{-FastMath.PI/3, 0, 0}));
        attachChild(text3d);
        text3d.updateRenderState();
    }
    
    /**
     * Create a model for status icon.  
     */
    private Spatial createModel(StatusType type)
    {
        switch (type)
        {
            case AHMED:
                return MeshFactory.getSingleton().createMesh(CellType.CELL_BONUS_AHMED);
            case BOMB_RANGE:
                return MeshFactory.getSingleton().createMesh(CellType.CELL_BONUS_RANGE);
            case BOMB_WALKING:
                return MeshFactory.getSingleton().createMesh(CellType.CELL_BONUS_BOMB_WALKING);
            case BOMBS:
                return MeshFactory.getSingleton().createMesh(CellType.CELL_BONUS_BOMB);
            case CRATE_WALKING:
                return MeshFactory.getSingleton().createMesh(CellType.CELL_BONUS_CRATE_WALKING);
            case CTRL_REVERSE:
                return MeshFactory.getSingleton().createMesh(CellType.CELL_BONUS_CONTROLLER_REVERSE);
            case DIARRHOEA:
                return MeshFactory.getSingleton().createMesh(CellType.CELL_BONUS_DIARRHEA);
            case IMMORTALITY:
                return MeshFactory.getSingleton().createMesh(CellType.CELL_BONUS_IMMORTALITY);
            case LIVES:
                return MeshFactory.getSingleton().getLivesModel();
            case MAX_RANGE:
                return MeshFactory.getSingleton().createMesh(CellType.CELL_BONUS_MAXRANGE);
            case NO_BOMBS:
                return MeshFactory.getSingleton().createMesh(CellType.CELL_BONUS_NO_BOMBS);
            case SLOW_DOWN:
                return MeshFactory.getSingleton().createMesh(CellType.CELL_BONUS_SLOW_DOWN);
            case SPEED_UP:
                return MeshFactory.getSingleton().createMesh(CellType.CELL_BONUS_SPEED_UP);
        }
        throw new RuntimeException("Unreachable status type.");
    }

    /**
     * Update displaying value.
     */
    public void update(int value)
    {
        text3d.setText(getValue(value));
        if (value < 0)
            detachAllChildren();
        else
        {
            attachChild(model);
            attachChild(text3d);
        }
        text3d.updateRenderState();
    }

    /**
     * Return string that is displaying on the status base on the value. When value is
     * less than 0 no string is displaying, when value is infinity - displaying x.
     */
    private String getValue(int value)
    {
        if (value == Integer.MAX_VALUE) return "x";
        else if (value < 0) return "";
        else return Integer.toString(value);
    }
}
