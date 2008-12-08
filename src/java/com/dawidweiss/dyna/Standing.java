package com.dawidweiss.dyna;

/**
 * Player's standing.
 */
public final class Standing
{
    /* */
    public final Player player;

    /**
     * The victim number indicates the order of being killed, starting from 0 (the
     * first player killed). Note that the number may be the same for two players if
     * they were killed in the same explosion.
     */
    public final int victimNumber;

    public Standing(Player p, int victimNumber)
    {
        this.player = p;
        this.victimNumber = victimNumber;
    }
}