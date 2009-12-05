package org.jdyna.view.resources;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.jdyna.*;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Static resource factory. 
 */
public final class ImagesFactory
{
    /**
     * Locally used image cache.
     */
    private static final HashMap<String, BufferedImage> imageCache = Maps.newHashMap();

    /**
     * Dyna classic resources.
     */
    public static final Images DYNA_CLASSIC = createDynaClassic();

    private static class StateImageSlices
    {
        public int state;
        public ImageSlice [] slices;
        
        public StateImageSlices(int state, ImageSlice [] slices)
        {
            this.state = state;
            this.slices = slices;
        }
    }

    /* */
    private ImagesFactory() 
    {
        // no instances.
    }

    /**
     * Resources for Dyna Classic.
     */
    private static Images createDynaClassic()
    {
        /*
         * Globals.
         */

        final int cellSize = 16;
        final int bombFrameRate = 4;
        final int boomFrameRate = 2;
        final int playerFrameRate = 4;
        final int crateFrameRate = 2;

        /*
         * Cell (grid) data. 
         */

        ImageSliceBuilder sb = new ImageSliceBuilder("tiles/05.png", cellSize);
        ImageSliceBuilder sb2 = new ImageSliceBuilder("tiles/composites.png", cellSize);
        List<CellData> cells = Arrays.asList(new CellData [] {
            /* Simple cells */
            create(CellType.CELL_EMPTY, 1, sb.tile(0, 0)),
            create(CellType.CELL_WALL, 1, sb.tile(1, 0)),

            /* Crates */
            create(CellType.CELL_CRATE, 1, sb.tile(2, 0)),
            create(CellType.CELL_CRATE_OUT, crateFrameRate, sb.tile(new int [][] {
                {3, 0}, {3, 0}, {4, 0}, {5, 0}, {6, 0}, {7, 0}, {8, 0}})),

            /* Bombs */
            create(CellType.CELL_BOMB, bombFrameRate, sb.tile(new int [][] {
                {10, 0}, {9, 0}, {11, 0}, {9, 0}})),

            /* Explosions */
            create(CellType.CELL_BOOM_TY, boomFrameRate, sb.tile(new int [][] {
                {3, 1}, {2, 1}, {1, 1}, {0, 1}, {1, 1}, {2, 1}, {3, 1}})
            ),
            create(CellType.CELL_BOOM_RX, boomFrameRate, sb.tile(new int [][] {
                {7, 1}, {6, 1}, {5, 1}, {4, 1}, {5, 1}, {6, 1}, {7, 1}})
            ),
            create(CellType.CELL_BOOM_BY, boomFrameRate, sb.tile(new int [][] {
                {11, 1}, {10, 1}, {9, 1}, {8, 1}, {9, 1}, {10, 1}, {11, 1}})
            ),
            create(CellType.CELL_BOOM_LX, boomFrameRate, sb.tile(new int [][] {
                {15, 1}, {14, 1}, {13, 1}, {12, 1}, {13, 1}, {14, 1}, {15, 1}})
            ),
            create(CellType.CELL_BOOM_Y, boomFrameRate, sb.tile(new int [][] {
                {19, 1}, {18, 1}, {17, 1}, {16, 1}, {17, 1}, {18, 1}, {19, 1}})
            ),
            create(CellType.CELL_BOOM_X, boomFrameRate, sb.tile(new int [][] {
                {3, 2}, {2, 2}, {1, 2}, {0, 2}, {1, 2}, {2, 2}, {3, 2}})
            ),
            create(CellType.CELL_BOOM_XY, boomFrameRate, sb.tile(new int [][] {
                {8, 2}, {6, 2}, {5, 2}, {4, 2}, {5, 2}, {6, 2}, {7, 2}})
            ),

            /* Bonuses */
            create(CellType.CELL_BONUS_BOMB, 4, sb2.tile(new int [][] {
                {0, 0}, {3, 0} })
            ),
            create(CellType.CELL_BONUS_RANGE, 4, sb2.tile(new int [][] {
                {1, 0}, {4, 0} })
            ),
            create(CellType.CELL_BONUS_DIARRHEA, 4, sb2.tile(new int [][] {
            	{6, 0}, {7, 0} })
            ),
            create(CellType.CELL_BONUS_IMMORTALITY, 4, sb2.tile(new int [][] {
              	{12, 0}, {13, 0} })
            ),
            create(CellType.CELL_BONUS_NO_BOMBS, 4, sb2.tile(new int [][] {
               	{8, 0}, {9, 0} })
            ),
            create(CellType.CELL_BONUS_MAXRANGE, 4, sb2.tile(new int [][] {
                {10, 0}, {11, 0} })
            ),
            create(CellType.CELL_BONUS_SPEED_UP, 4, sb2.tile(new int [][] {
                {14, 0}, {15, 0} })
            ),
            create(CellType.CELL_BONUS_CRATE_WALKING, 4, sb2.tile(new int [][] {
                {16, 0}, {17, 0} })
            ),
            create(CellType.CELL_BONUS_BOMB_WALKING, 4, sb2.tile(new int [][] {
                {18, 0}, {19, 0} })
            ),
            create(CellType.CELL_BONUS_CONTROLLER_REVERSE, 4, sb2.tile(new int [][] {
                {0, 1}, {1, 1} })
            ),
            create(CellType.CELL_BONUS_SLOW_DOWN, 4, sb2.tile(new int [][] {
                {2, 1}, {3, 1} })
            ),
            create(CellType.CELL_BONUS_AHMED, 4, sb2.tile(new int [][] {
                {4, 1}, {5, 1} })
            ),
            create(CellType.CELL_BONUS_EASTER_EGG, 4, sb2.tile(new int [][] {
                {6, 1}, {7, 1} })
            ),
        });

        /*
         * Sprites.
         */
        
        final List<SpriteData> sprites = Lists.newArrayList();
        
        /*
         * Players.
         */

        final int playerFrameSize = 24;
        sb = new ImageSliceBuilder("tiles/02.png", playerFrameSize);
        sb.w = playerFrameSize - 1;
        sb.h = playerFrameSize - 1;
        final Point offset = new Point(-playerFrameSize / 2, -playerFrameSize / 2);

        final StateImageSlices [] slices1 = new StateImageSlices [] {
            stateSlices(Player.State.DOWN, 
                sb.tile(new int [][] { {0, 0}, {1, 0}, {0, 0}, {2, 0}})),
            stateSlices(Player.State.RIGHT,
                sb.tile(new int [][] { {3, 0}, {4, 0}, {3, 0}, {5, 0}})),
            stateSlices(Player.State.LEFT,
                sb.tile(new int [][] { {6, 0}, {7, 0}, {6, 0}, {8, 0}})),
            stateSlices(Player.State.UP,
                sb.tile(new int [][] { {9, 0}, {10, 0}, {9, 0}, {11, 0}})),
            stateSlices(Player.State.DYING,
                sb.tile(new int [][] { {12, 0}, {0, 1}, {1, 1}, {2, 1}, {3, 1}, {4, 1}, {5, 1}, {6, 1}})),
            stateSlices(Player.State.DEAD,
                sb.tile(new int [][] { }))            
        };

        final StateImageSlices [] slices2 = new StateImageSlices [] {
            stateSlices(Player.State.DOWN, 
                sb.tile(new int [][] { {7, 1}, {8, 1}, {7, 1}, {9, 1}})),
            stateSlices(Player.State.RIGHT,
                sb.tile(new int [][] { {10, 1}, {11, 1}, {10, 1}, {12, 1}})),
            stateSlices(Player.State.LEFT,
                sb.tile(new int [][] { {0, 2}, {1, 2}, {0, 2}, {2, 2}})),
            stateSlices(Player.State.UP,
                sb.tile(new int [][] { {3, 2}, {4, 2}, {3, 2}, {5, 2}})),
            stateSlices(Player.State.DYING,
                sb.tile(new int [][] { {6, 2}, {7, 2}, {8, 2}, {9, 2}, {10, 2}, {11, 2}, {12, 2}, {0, 3}})),
            stateSlices(Player.State.DEAD,
                sb.tile(new int [][] { }))            
        };

        final StateImageSlices [] slices3 = new StateImageSlices [] {
            stateSlices(Player.State.DOWN, 
                sb.tile(new int [][] { {1, 3}, {2, 3}, {1, 3}, {3, 3}})),
            stateSlices(Player.State.RIGHT,
                sb.tile(new int [][] { {4, 3}, {5, 3}, {4, 3}, {6, 3}})),
            stateSlices(Player.State.LEFT,
                sb.tile(new int [][] { {7, 3}, {8, 3}, {7, 3}, {9, 3}})),
            stateSlices(Player.State.UP,
                sb.tile(new int [][] { {10, 3}, {11, 3}, {10, 3}, {12, 3}})),
            stateSlices(Player.State.DYING,
                sb.tile(new int [][] { {0, 4}, {1, 4}, {2, 4}, {3, 4}, {4, 4}, {5, 4}, {6, 4}, {7, 4}})),
            stateSlices(Player.State.DEAD,
                sb.tile(new int [][] { }))            
        };

        final StateImageSlices [] slices4 = new StateImageSlices [] {
            stateSlices(Player.State.DOWN, 
                sb.tile(new int [][] { {8, 4}, {9, 4}, {8, 4}, {10, 4}})),
            stateSlices(Player.State.RIGHT,
                sb.tile(new int [][] { {11, 4}, {12, 4}, {11, 4}, {0, 5}})),
            stateSlices(Player.State.LEFT,
                sb.tile(new int [][] { {1, 5}, {2, 5}, {1, 5}, {3, 5}})),
            stateSlices(Player.State.UP,
                sb.tile(new int [][] { {4, 5}, {5, 5}, {4, 5}, {6, 5}})),
            stateSlices(Player.State.DYING,
                sb.tile(new int [][] { {7, 5}, {8, 5}, {9, 5}, {10, 5}, {11, 5}, {12, 5}, {11, 6}, {12, 6}})),
            stateSlices(Player.State.DEAD,
                sb.tile(new int [][] { }))            
        };

        sprites.addAll(Arrays.asList(new SpriteData [] {
            createPlayer(ISprite.Type.PLAYER_1, playerFrameRate, offset, slices1),
            createPlayer(ISprite.Type.PLAYER_2, playerFrameRate, offset, slices2),
            createPlayer(ISprite.Type.PLAYER_3, playerFrameRate, offset, slices3),
            createPlayer(ISprite.Type.PLAYER_4, playerFrameRate, offset, slices4),
        }));

        return new Images(cellSize, cells, sprites);
    }

    /*
     * 
     */
    private static SpriteData createPlayer(ISprite.Type type, int frameRate,
        Point offsetForAllFrames, StateImageSlices... stateSlices)
    {
        final SpriteData data = new SpriteData();
        data.frameAdvanceRate = frameRate;
        data.spriteType = type;
        data.slices = slices(stateSlices);
        data.frames = load(data.slices);

        data.offsets = new Point [data.frames.length][];
        for (int i = 0; i < data.frames.length; i++)
        {
            data.offsets[i] = new Point[data.frames[i].length];
            Arrays.fill(data.offsets[i], offsetForAllFrames);
        }

        return data;
    }

    /*
     * 
     */
    private static BufferedImage [][] load(ImageSlice [][] slices)
    {
        BufferedImage [][] result = new BufferedImage [slices.length][];
        for (int i = 0; i < slices.length; i++)
        {
            result[i] = load(slices[i]);
        }
        return result;
    }

    /*
     * 
     */
    private static ImageSlice [][] slices(StateImageSlices [] stateSlices)
    {
        int maxStateIndex = -1;
        for (StateImageSlices s : stateSlices) 
        {
            maxStateIndex = Math.max(maxStateIndex, s.state);
        }

        ImageSlice [][] slices = new ImageSlice [maxStateIndex + 1][];
        for (int i = 0; i < stateSlices.length; i++) 
        {
            slices[stateSlices[i].state] = stateSlices[i].slices;
        }

        return slices;
    }

    /*
     * 
     */
    private static StateImageSlices stateSlices(Enum<?> state, ImageSlice [] tiles)
    {
        return new StateImageSlices(state.ordinal(), tiles);
    }

    /*
     * 
     */
    private static CellData create(CellType cellType, int advanceRate, ImageSlice [] slices)
    {
        final CellData cellData = new CellData();
        cellData.cellType = cellType;
        cellData.frameAdvanceRate = advanceRate;
        cellData.slices = slices;
        cellData.frames = load(slices);
        return cellData;
    }

    /*
     * 
     */
    private static BufferedImage [] load(ImageSlice [] slices)
    {
        final BufferedImage [] images = new BufferedImage[slices.length];
        for (int i = 0; i < slices.length; i++)
        {
            final ImageSlice slice = slices[i];
            final BufferedImage source = getCached(slice.imageName);
            images[i] = source.getSubimage(slice.x, slice.y, slice.w, slice.h);
        }
        return images;
    }

    /*
     * Cache intermediate images from which tiles are cut out.
     */
    private static BufferedImage getCached(String imageName)
    {
        try
        {
            if (!imageCache.containsKey(imageName))
            {
                imageCache.put(imageName, ImageUtilities.loadResourceImage(imageName));
            }
            return imageCache.get(imageName);
        } 
        catch (IOException e)
        {
            throw new RuntimeException("Missing resource: "
                + imageName);
        }
    }    
}
