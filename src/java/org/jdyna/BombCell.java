package org.jdyna;

/**
 * Subtype of {@link Cell}, with additional properties.
 */
final class BombCell extends Cell
{
    /**
     * If bomb has been dropped by a player, we need to keep its reference so that his
     * bomb counter can be restored properly.
     */
    public PlayerInfo player;

    /**
     * How many frames until explosion (down-to-zero counter).
     */
    public int fuseCounter = Globals.DEFAULT_FUSE_FRAMES;

    /**
     * Explosion range (number of cells in each direction).
     */
    public int range = Globals.DEFAULT_BOMB_RANGE;

    /*
     * 
     */
    BombCell()
    {
        super(CellType.CELL_BOMB);
    }
}
