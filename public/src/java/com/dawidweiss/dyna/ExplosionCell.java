package com.dawidweiss.dyna;

import java.util.ArrayList;

import com.google.common.collect.Lists;

/**
 * Subtype of {@link Cell}, with additional properties for tracking who killed whom in an
 * explosion.
 */
final class ExplosionCell extends Cell
{
    /**
     * If bomb has been dropped by a player, we need to keep its reference so that his
     * bomb counter can be restored properly.
     */
    public ArrayList<PlayerInfo> flamesBy = Lists.newArrayListWithExpectedSize(3);

    /*
     * 
     */
    ExplosionCell(CellType type)
    {
        super(type);
    }

    /**
     * Add flame attribution to a given player.
     */
    void addAttribution(PlayerInfo player)
    {
        if (player == null) return;

        /*
         * Avoid duplicates. Because we have a small number of players, naive scan through
         * the array should be fine.
         */
        if (!flamesBy.contains(flamesBy))
        {
            flamesBy.add(player);
        }
    }

    /**
     * Merge attributions from other cells with this one.
     */
    void mergeAttributions(ExplosionCell... cells)
    {
        for (ExplosionCell e : cells)
        {
            for (PlayerInfo pi : e.flamesBy)
            {
                addAttribution(pi);
            }
        }
    }
}
