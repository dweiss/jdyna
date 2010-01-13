package org.jdyna.view.jme.resources;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.jdyna.CellType;
import org.jdyna.view.resources.ResourceUtilities;

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

// TODO: this class needs to be cleaned up and rewritten. It's a mess. Why all this
// crap with resource loaders from JME if an URL can be given directly?

public class MeshFactory
{
    private static final String BASE_DIR = "jme";
    private static EnumMap<CellType, String> modelPaths = new EnumMap<CellType, String>(
        CellType.class);
    private EnumMap<CellType, Spatial> meshes = new EnumMap<CellType, Spatial>(
        CellType.class);
    private Spatial unknownBonus;
    private Spatial [] playerMeshes;
    private Spatial [] playerDyingMeshes;
    private static CullState cull = DisplaySystem.getDisplaySystem().getRenderer()
        .createCullState();

    public static final short PLAYER_MESHES = 1;
    public static final short PLAYER_DYING_MESHES = 2;
    
    static
    {
        cull.setCullFace(Face.Back);
    }

    static
    {
        modelPaths.put(CellType.CELL_EMPTY, "floor");
        modelPaths.put(CellType.CELL_WALL, "wall");
        modelPaths.put(CellType.CELL_CRATE, "crate");
        modelPaths.put(CellType.CELL_BOMB, "bomb");
        modelPaths.put(CellType.CELL_BONUS_BOMB, "bonus_bomb");
        modelPaths.put(CellType.CELL_BONUS_RANGE, "bonus_range");
        modelPaths.put(CellType.CELL_BONUS_NO_BOMBS, "bonus_no_bombs");
        modelPaths.put(CellType.CELL_BONUS_IMMORTALITY, "bonus_immortality");
        modelPaths.put(CellType.CELL_BONUS_CRATE_WALKING, "bonus_crate_walking");
        modelPaths.put(CellType.CELL_BONUS_CONTROLLER_REVERSE, "bonus_controller_reverse");
        modelPaths.put(CellType.CELL_BONUS_AHMED, "bonus_ahmed");
        modelPaths.put(CellType.CELL_BONUS_MAXRANGE, "bonus_maxrange");
        modelPaths.put(CellType.CELL_BONUS_SPEED_UP, "bonus_speed_up");
        modelPaths.put(CellType.CELL_BONUS_DIARRHEA, "bonus_diarrhea");
        modelPaths.put(CellType.CELL_BONUS_SLOW_DOWN, "bonus_slow_down");
        modelPaths.put(CellType.CELL_BONUS_BOMB_WALKING, "bonus_bomb_walking");
        modelPaths.put(CellType.CELL_BONUS_SURPRISE, "bonus_surprise");
    }

    private static MeshFactory singleton;

    // TODO: I'm not so sure it needs to be a singleton here...
    public synchronized static MeshFactory getSingleton()
    {
        /*
         * Important learning lesson: 
         * You had a broken singleton pattern here (null check outside of the monitor).
         * This is wrong for a variety of reasons. Don't do it.
         */
        if (singleton == null)
        {
            throw new IllegalStateException("Initialize first.");
        }

        return singleton;
    }
    
    public synchronized static void initializeSingleton(ProgressListener progressCallback)
    {
        if (singleton != null)
            throw new IllegalStateException("Already initialized.");
        
        singleton = new MeshFactory(progressCallback);
    }

    public MeshFactory(ProgressListener progressCallback)
    {
        for (Entry<CellType, String> entry : modelPaths.entrySet())
        {
            Spatial model = loadModel(entry.getValue(), progressCallback);
            meshes.put(entry.getKey(), model);
        }

        unknownBonus = loadModel("unknown_bonus", progressCallback);

        String [] names = getAnimatedModelNames("player/player");
        playerMeshes = new Spatial [names.length]; // a separate cloner for each frame
        for (int j = 0; j < names.length; j++)
        {
            playerMeshes[j] = loadModel(names[j], progressCallback);
        }

        names = getAnimatedModelNames("player/player-dying");
        playerDyingMeshes = new Spatial [names.length]; // a separate cloner for each
        // frame
        for (int j = 0; j < names.length; j++)
        {
            playerDyingMeshes[j] = loadModel(names[j], progressCallback);
        }
    }

    private static String [] getAnimatedModelNames(String name)
    {
        List<String> names = new ArrayList<String>(100);
        for (int i = 1;; i++)
        {
            try
            {
                String nameWithFrameNum = String.format("%s_%06d", name, i);
                ResourceUtilities.getResourceURL(BASE_DIR + "/" + nameWithFrameNum
                    + ".jme");
                names.add(nameWithFrameNum);
            }
            catch (IOException e)
            {
                break;
            }
        }
        if (names.size() == 0) throw new RuntimeException(
            "No frames found for model: " + name);

        return names.toArray(new String [names.size()]);
    }

    public static Spatial loadModel(String name, ProgressListener progress)
    {
        try
        {
            progress.update(name);
            return loadModelImpl(BASE_DIR + "/" + name + ".jme");
        }
        catch (IOException e)
        {
            throw new RuntimeException("Loading model " + name + " failed", e);
        }
    }

    private static Spatial loadModelImpl(String path) throws IOException
    {
        if (!path.endsWith(".jme")) throw new IllegalArgumentException(
            "Model formats other than JME are not supported");

        final long start = System.currentTimeMillis();
        try
        {
            SimpleResourceLocator locator;
            locator = new SimpleResourceLocator(ResourceUtilities.getResourceURL(path));
            ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_TEXTURE,
                locator);
            ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_MODEL,
                locator);
        }
        catch (URISyntaxException e)
        {
            throw new IllegalArgumentException("Bad pathname to model resource.");
        }

        BinaryImporter importer = BinaryImporter.getInstance();
        Savable savable = importer.load(ResourceUtilities.getResourceURL(path));

        Spatial mesh = (Spatial) savable;

        mesh.setModelBound(new BoundingBox());
        mesh.updateModelBound();
        
        Logger.getLogger(MeshFactory.class).info(
            String.format(Locale.ENGLISH, "Loading: " + path + " took: %.2f sec.",
                (System.currentTimeMillis() - start) / 1000.0));

        return mesh;
    }

    public static TriMesh copyMesh(TriMesh mesh)
    {
        TriMesh copy = new TriMesh(mesh.getName() + "_copy", mesh.getVertexBuffer(), mesh
            .getNormalBuffer(), mesh.getColorBuffer(), mesh.getTextureCoords(0), mesh
            .getIndexBuffer());
        copy.setRenderState(mesh.getRenderState(StateType.Texture));
        copy.setRenderState(mesh.getRenderState(StateType.Blend));
        copy.setRenderState(mesh.getRenderState(StateType.Cull));
        copy.setRenderState(mesh.getRenderState(StateType.Material));
        copy.setRenderState(cull);

        return copy;
    }

    public static Spatial copySpatial(Spatial spatial)
    {
        if (spatial instanceof TriMesh) return copyMesh((TriMesh) spatial);

        Node copy = new Node();

        for (Spatial child : ((Node) spatial).getChildren())
        {

            Spatial childCopy = copySpatial(child);
            copy.attachChild(childCopy);
        }

        return copy;
    }

    public Spatial createMesh(CellType type)
    {
        Spatial mesh = meshes.get(type);
        return copySpatial(mesh);
    }

    public Spatial [] createPlayer(short meshesType)
    {
        Spatial [] meshes;

        if (meshesType == PLAYER_MESHES) meshes = playerMeshes;
        else if (meshesType == PLAYER_DYING_MESHES) meshes = playerDyingMeshes;
        else return null;

        Spatial [] models = new Spatial [meshes.length];

        for (int i = 0; i < meshes.length; i++)
        {
            Spatial model = copySpatial(meshes[i]);
            model.setModelBound(new BoundingBox());
            model.updateModelBound();
            models[i] = model;
        }
        return models;
    }

    public Spatial getUnknownBonus()
    {
        return copySpatial(unknownBonus);
    }
}
