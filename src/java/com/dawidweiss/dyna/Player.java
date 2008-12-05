package com.dawidweiss.dyna;

/**
 * A single player in the game.
 */
public final class Player
{
    /**
     * The player can be in the following states (possibly a combination of these at the same time).
     */
    public static enum State
    {
        LEFT,
        RIGHT,
        UP,
        DOWN,
        DYING,
        DEAD
    }

    /**
     * The player's unique identifier.
     */
    public final String name;
    
    /**
     * Movement controller for this player.
     */
    public final IController controller;

    /*
     * 
     */
    public Player(String name, IController controller)
    {
        assert name != null && controller != null;

        this.name = name;
        this.controller = controller;
    }
}
