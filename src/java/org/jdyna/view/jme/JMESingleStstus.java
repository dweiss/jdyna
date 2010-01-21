package org.jdyna.view.jme;

import org.jdyna.CellType;
import org.jdyna.view.jme.resources.MeshFactory;

import com.jme.math.FastMath;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jmex.font3d.Font3D;
import com.jmex.font3d.Text3D;

@SuppressWarnings("serial")
public class JMESingleStstus extends Node
{
    Spatial model;
    Text3D text3d;
    
    public JMESingleStstus(CellType type, float position, Font3D font3d)
    {
        model = MeshFactory.getSingleton().createMesh(type);
        
        model.setLocalTranslation(position, 0, 0);
        model.setLocalRotation(new Quaternion(new float[]{-FastMath.PI/3, 0, 0}));
        attachChild(model);
        model.updateRenderState();
        
        text3d = font3d.createText("0", 0.5f, 0);
        text3d.setFontColor(ColorRGBA.red.clone());
        text3d.setLocalTranslation(position, -0.1f, 0.4f);
        text3d.setLocalScale(new Vector3f(0.5f, 0.5f, 0.01f));
        text3d.setLocalRotation(new Quaternion(new float[]{-FastMath.PI/3, 0, 0}));
        
        attachChild(text3d);
    }
    
    public void update(int value)
    {
        text3d.setText(getValue(value));
        text3d.updateRenderState();
    }

    public String getValue(int value)
    {
        if (value == Integer.MAX_VALUE) return "x";
        else if (value < 0) return "";
        else return Integer.toString(value);
    }
}
