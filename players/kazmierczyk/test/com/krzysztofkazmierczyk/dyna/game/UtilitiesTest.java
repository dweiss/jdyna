package com.krzysztofkazmierczyk.dyna.game;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.dawidweiss.dyna.BoardInfo;
import com.dawidweiss.dyna.Cell;
import com.dawidweiss.dyna.CellType;
import com.dawidweiss.dyna.GameStateEvent;
import com.dawidweiss.dyna.Globals;
import com.dawidweiss.dyna.IPlayerSprite;
import com.krzysztofkazmierczyk.dyna.GameStateEventUpdater;
import com.krzysztofkazmierczyk.dyna.PlayerInfo;
import com.krzysztofkazmierczyk.dyna.client.game.Utilities;
import com.krzysztofkazmierczyk.dyna.client.game.wayFinder.WayCell;
import com.krzysztofkazmierczyk.dyna.client.game.wayFinder.WayFinder;

public class UtilitiesTest
{

    private static final Cell b = Cell.getInstance(CellType.CELL_BOMB); // bomb
    private static final Cell c = Cell.getInstance(CellType.CELL_CRATE); // crate
    private static final Cell e = Cell.getInstance(CellType.CELL_EMPTY); // empty
    private static final Cell w = Cell.getInstance(CellType.CELL_WALL); // wall

    private static Cell [][] generateInitialBoard(int no)
    {
        switch (no)
        {
            case 0:
            {
                final Cell [][] cells =
                {
                    {
                        w, w, w, w, w
                    },
                    {
                        w, b, c, e, w
                    },
                    {
                        w, e, e, e, w
                    },
                    {
                        w, e, e, e, w
                    },
                    {
                        w, w, w, w, w
                    }
                };

                return cells;
            }
            default:
            {
                final Cell [][] cells =
                {
                    {
                        w, w, w, w, w
                    },
                    {
                        w, e, c, e, w
                    },
                    {
                        w, c, c, e, w
                    },
                    {
                        w, e, b, e, w
                    },
                    {
                        w, w, w, w, w
                    }
                };

                return cells;
            }
        }

    }

    private static GameStateEventUpdater generateInitialGameStateUpdater(final int no, final Point playerCell)
    {

        final BoardInfo bi = new BoardInfo(new Dimension(5, 5), Globals.DEFAULT_CELL_SIZE);

        final Cell [][] cells = generateInitialBoard(no);

        final GameStateEvent gse = new GameStateEvent(cells,
            new ArrayList<IPlayerSprite>());
        final GameStateEventUpdater gseu = new GameStateEventUpdater(bi, gse, 0);
        final PlayerInfo pi = new PlayerInfo(4, 4, "test Player", gseu.getBoardInfo()
            .gridToPixel(playerCell));

        gseu.getPlayers().add(pi);

        return gseu;
    }

    @Test
    public void safeToDropBomb()
    {
        GameStateEventUpdater gseu = generateInitialGameStateUpdater(0, new Point(2,2));
        Assert.assertTrue(Utilities.safeToDropBomb(gseu, 0));
        
        GameStateEventUpdater gseu2 = generateInitialGameStateUpdater(1, new Point(1,1));
        Assert.assertFalse(Utilities.safeToDropBomb(gseu2, 0));
    }

    @Test
    public void testGetExplosionFrames()
    {
        BoardInfo bi = new BoardInfo(new Dimension(5, 5), Globals.DEFAULT_CELL_SIZE);

        Cell [][] cells = generateInitialBoard(0);

        GameStateEvent gse = new GameStateEvent(cells, new ArrayList<IPlayerSprite>());
        GameStateEventUpdater gseu = new GameStateEventUpdater(bi, gse, 0);

        cells[3][3] = b;
        cells[2][1] = b;

        gseu.update(gse, 1);

        List<Integer> [][] actual = Utilities.getExplosionFrames(gseu);

        Assert.assertEquals(2, actual[2][3].size());
    }

    @Test
    public void testGetWaysTable()
    {
        final GameStateEventUpdater gseu = generateInitialGameStateUpdater(0, new Point(1,1));

        final WayCell [][] result = WayFinder.getWaysTable(gseu, new Point(1, 1));
        Assert.assertEquals((int) 0, (int) result[1][1].getTimeOfArrive());
        Assert.assertEquals(new Point(1, 1), result[2][1].getCellOfArrive());
        Assert
            .assertEquals((int) Integer.MAX_VALUE, (int) result[0][1].getTimeOfArrive());
        Assert.assertEquals((int) 32, (int) result[1][3].getTimeOfArrive());
        Assert.assertEquals(new Point(2, 3), result[1][3].getCellOfArrive());
        
        final GameStateEventUpdater gseu2 = generateInitialGameStateUpdater(1, new Point(1,1));

        final WayCell [][] result2 = WayFinder.getWaysTable(gseu2, new Point(1, 1));
        Assert.assertFalse(result2[3][3].isReachable());
        
        final WayCell [][] result3 = WayFinder.getWaysTable(gseu2, new Point(3, 1));
        Assert.assertNull(result3[1][3].getCellOfArrive());
        Assert.assertNull(result3[3][1].getCellOfArrive());
    }

    
    @Test
    public void testSafeToDropBomb() {
        final GameStateEventUpdater gseu = generateInitialGameStateUpdater(1, new Point(3,3));
        gseu.getPlayers().add(new PlayerInfo(3,6,"test2", new Point(3,1)));
        Assert.assertFalse(Utilities.safeToDropBomb(gseu, 0));
        Assert.assertFalse(Utilities.safeToDropBomb(gseu, 1));
    }
}
