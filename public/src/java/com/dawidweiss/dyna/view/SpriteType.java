package com.dawidweiss.dyna.view;

/**
 * All sprite types available in the game.
 */
public enum SpriteType
{
    PLAYER_1, PLAYER_2, PLAYER_3, PLAYER_4;

    private static final SpriteType [] PLAYER_SPRITES = new SpriteType []
    {
        PLAYER_1, PLAYER_2, PLAYER_3, PLAYER_4
    };

    /**
     * All "player" sprites.
     */
    public static SpriteType [] getPlayerSprites()
    {
        return PLAYER_SPRITES;
    }
}
