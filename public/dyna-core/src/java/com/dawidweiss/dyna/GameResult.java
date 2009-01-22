package com.dawidweiss.dyna;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;

/**
 * The result of a single game. The data contained in the result depends on the
 * {@link Game.Mode}.
 */
public final class GameResult
{
    /**
     * The game mode in which the game was executed.
     */
    public final Game.Mode mode;

    /**
     * An array of statistics for players after the game is over. Statistics depend on the
     * game mode.
     */
    public final List<PlayerStatus> stats;

    /**
     * The result is not complete (from an interrupted game).
     */
    public boolean gameInterrupted;

    /*
     * 
     */
    public GameResult(Game.Mode mode, Collection<PlayerStatus> stats)
    {
        this.mode = mode;
        this.stats = Lists.newArrayList(stats);
    }

    /*
     * 
     */
    @Override
    public String toString()
    {
        final StringBuilder b = new StringBuilder();
        b.append("Game result [mode=" + mode + "] " + (gameInterrupted ? "[interrupted]" : "") + "\n\n");
        for (PlayerStatus ps : stats)
        {
            b.append(ps.toString());
            b.append("\n");
        }
        return b.toString();
    }
}
