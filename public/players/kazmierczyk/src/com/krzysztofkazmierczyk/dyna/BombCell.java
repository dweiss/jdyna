package com.krzysztofkazmierczyk.dyna;

import com.dawidweiss.dyna.Cell;
import com.dawidweiss.dyna.CellType;
import com.dawidweiss.dyna.Globals;

/**
 * This class contains additional information about BombCell. BombCell is also for me cell after explosion.
 * 
 * @author kazik
 */
public class BombCell extends Cell implements Cloneable
{

    public static final int explosionFrameCount = 7 * 2 + 1;
    
    /** Time of put this bomb */
    private int frameNO;

    /**
     * If bomb has been dropped by a player, we need to keep its reference so that his
     * bomb counter can be restored properly.
     */
    private Integer playerId;

    /**
     * Explosion range (number of cells in each direction).
     */
    private int range = Globals.DEFAULT_BOMB_RANGE;
    
    private int explosionFrameNO;

    public BombCell(int frameNO)
    {
        super(CellType.CELL_BOMB);
        this.frameNO = frameNO;
        this.explosionFrameNO = frameNO + Globals.DEFAULT_FUSE_FRAMES;
    }

    public BombCell(int frameNO, int range)
    {
        super(CellType.CELL_BOMB);
        this.frameNO = frameNO;
        this.range = range;
        this.explosionFrameNO = frameNO + Globals.DEFAULT_FUSE_FRAMES;
    }

    public BombCell(Integer playerId, int frameNO, int range)
    {
        super(CellType.CELL_BOMB);
        this.playerId = playerId;
        this.frameNO = frameNO;
        this.range = range;
        this.explosionFrameNO = frameNO + Globals.DEFAULT_FUSE_FRAMES;
    }

    @Override
    public Object clone() throws CloneNotSupportedException
    {
        return new BombCell(frameNO, playerId, range);
    }

    public int getFrameNO()
    {
        return frameNO;
    }

    public Integer getPlayerId()
    {
        return playerId;
    }

    public int getRange()
    {
        return range;
    }

    /* package */void setFrameNO(int frameNO)
    {
        this.frameNO = frameNO;
    }
    
    /** Returns time when this cell ends being exlosion cell. */
    public int getBombcellEndFrameNO() {
        return explosionFrameNO + explosionFrameCount;
    }
    
    public void setExplosionFrameNO(int frameNO) {
        this.explosionFrameNO = frameNO;
    }
    
    public int getExplosionFrameNO() {
        return explosionFrameNO;
    }
}
