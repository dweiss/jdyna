package com.dawidweiss.dyna;

import java.util.*;

/**
 * The result of a single game.
 */
public final class GameResult
{
    /**
     * The winner (last man standing). If <code>null</code>, the game was a draw.
     */
    public final Player winner;

    /**
     * An array of standings (players and the order in which they were killed).
     */
    public final List<Standing> standings;

    /**
     * 
     */
    public GameResult(Player winner, ArrayList<Standing> standings)
    {
        this.winner = winner;
        this.standings = Collections.unmodifiableList(standings);
    }

    /*
     * 
     */
    @Override
    public String toString()
    {
        final StringBuilder b = new StringBuilder();

        b.append("Winner: " + (winner != null ? winner.name : "-- (draw)"));
        b.append('\n');
        for (Standing s : standings)
        {
            b.append(String.format("%-3d  %s\n", s.victimNumber, s.player.name));
        }

        return b.toString();
    }
}
