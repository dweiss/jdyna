package org.jdyna;

/**
 * Public view of each player's dynamic data.
 */
public interface IPlayerSprite extends ISprite
{
    /**
     * @return Unique player name.
     */
    String getName();

    /**
     * @return The player is (temporarily or permanently) dead.
     */
    boolean isDead();

    /**
     * @return The player is immortal.
     */
    boolean isImmortal();
}
