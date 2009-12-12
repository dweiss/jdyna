package org.jdyna.view.jme.resources;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map.Entry;

import org.jdyna.view.jme.adapter.AbstractGameAdapter.DynaCell;

import com.jme.bounding.BoundingBox;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.TriMesh;
import com.jme.scene.state.CullState;
import com.jme.scene.state.CullState.Face;
import com.jme.scene.state.RenderState.StateType;
import com.jme.system.DisplaySystem;
import com.jme.util.export.Savable;
import com.jme.util.export.binary.BinaryImporter;
import com.jme.util.resource.ResourceLocatorTool;
import com.jme.util.resource.SimpleResourceLocator;

public class MeshFactory
{
    private static final String BASE_DIR = "src/graphics/jme";
    private EnumMap<DynaCell, String> modelPaths = new EnumMap<DynaCell, String>(
        DynaCell.class);
    private EnumMap<DynaCell, Spatial> meshes = new EnumMap<DynaCell, Spatial>(
        DynaCell.class);
    private Spatial[] playerMeshes;
    private Spatial[] playerDyingMeshes;
    private static CullState cull = DisplaySystem.getDisplaySystem().getRenderer().createCullState();

    static 
    {
        cull.setCullFace(Face.Back);
    }
    
    {
        modelPaths.put(DynaCell.EMPTY, "floor");
        modelPaths.put(DynaCell.WALL, "wall");
        modelPaths.put(DynaCell.CRATE, "crate");
        modelPaths.put(DynaCell.BOMB, "bomb");
        modelPaths.put(DynaCell.BONUS_RANGE, "bonus_range");
        modelPaths.put(DynaCell.BONUS_BOMB, "bonus_bomb");
        modelPaths.put(DynaCell.OTHER_CELL, "other_bonus");
    }

    static MeshFactory inst;

    public static MeshFactory inst()
    {
        if (inst == null)
        {
            synchronized (MeshFactory.class)
            {
                if (inst == null) inst = new MeshFactory();
            }
        }
        return inst;
    }

    public MeshFactory()
    {
        for (Entry<DynaCell, String> entry : modelPaths.entrySet())
        {
        	Spatial model = loadModel(entry.getValue());
            meshes.put(entry.getKey(), model);
        }
        
        String[] names = getAnimatedModelFiles("player/player");
        playerMeshes = new Spatial[names.length]; //a separate cloner for each frame
        for (int j = 0; j < names.length; j++) {
        	playerMeshes[j] = loadModel(names[j]);
		}

        names = getAnimatedModelFiles("player/player-dying");
        playerDyingMeshes = new Spatial[names.length]; //a separate cloner for each frame
        for (int j = 0; j < names.length; j++) {
        	playerDyingMeshes[j] = loadModel(names[j]);
		}
    }
    
    private static String[] getAnimatedModelFiles(String name) {
    	
    	List<String> filenames = new ArrayList<String>(100);
    	for (int i=1;;i++) {
    		String nameWithFrameNum = String.format("%s_%06d",name,i);
    		File file = new File(BASE_DIR,nameWithFrameNum+".jme");
    		if (!file.exists())
    			break;
    		filenames.add(nameWithFrameNum);
    	}
    	if (filenames.size() == 0)
    		throw new RuntimeException("No frames found for model: "+name);
    	System.out.println("Loaded "+filenames.size()+" frames for model: "+name);
    	return filenames.toArray(new String[filenames.size()]);
    }

    public static Spatial loadModel(String name)
    {
        try
        {
            return loadModelImpl(new File(BASE_DIR,name+".jme"));
        }
        catch (IOException e)
        {
            throw new RuntimeException("Loading model failed", e);
        }
    }

    private static Spatial loadModelImpl(File f) throws IOException
    {
        if (!f.getName().endsWith(".jme"))
            throw new IllegalArgumentException("Model formats other than JME are not supported");

        f = f.getAbsoluteFile();

        SimpleResourceLocator locator = new SimpleResourceLocator(f.getParentFile()
            .toURI());
        ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_TEXTURE, locator);
        ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_MODEL, locator);

        BinaryImporter importer = BinaryImporter.getInstance();
        Savable savable = importer.load(f);
        Spatial mesh = (Spatial) savable;
            
        mesh.setModelBound(new BoundingBox());
        mesh.updateModelBound();

        return mesh;
    }
    
    public static TriMesh copyMesh(TriMesh mesh) {
    	
    	TriMesh copy = new TriMesh(mesh.getName()+"_copy",
    			mesh.getVertexBuffer(),
    			mesh.getNormalBuffer(),
    			mesh.getColorBuffer(),
    			mesh.getTextureCoords(0),
    			mesh.getIndexBuffer());
    	copy.setRenderState(mesh.getRenderState(StateType.Texture));
    	copy.setRenderState(mesh.getRenderState(StateType.Blend));
    	copy.setRenderState(mesh.getRenderState(StateType.Cull));
    	copy.setRenderState(mesh.getRenderState(StateType.Material));
	    copy.setRenderState(cull);
    	
    	return copy;
    }

    public static Spatial copySpatial(Spatial spatial) {
    	
    	if (spatial instanceof TriMesh)
    		return copyMesh((TriMesh) spatial);
    	
    	Node copy = new Node();
    	
    	for (Spatial child : ((Node)spatial).getChildren()) {
    		
    		Spatial childCopy = copySpatial(child);
    		copy.attachChild(childCopy);
    	}
    	
    	return copy;
    }
    
    public Spatial createMesh(DynaCell type)
    {
    	Spatial mesh = meshes.get(type);
        
        return copySpatial(mesh);
    }

    public Spatial[] createPlayer()
    {
    	Spatial[] models = new Spatial[playerMeshes.length];
        for (int i=0;i<playerMeshes.length;i++) {
        	Spatial model = copySpatial(playerMeshes[i]);
        	model.setModelBound(new BoundingBox());
        	model.updateModelBound();
        	models[i] = model;
        }
        return models;
    }

    public Spatial[] createDyingPlayer()
    {
    	Spatial[] models = new Spatial[playerDyingMeshes.length];
        for (int i=0;i<playerDyingMeshes.length;i++) {
        	Spatial model = copySpatial(playerDyingMeshes[i]);
        	model.setModelBound(new BoundingBox());
        	model.updateModelBound();
        	models[i] = model;
        }
        return models;
    }
}
