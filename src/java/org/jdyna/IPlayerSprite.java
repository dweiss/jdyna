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
    
    /**
     * @return The number of available bombs for this player.
     */
    int getBombCount();
    
    /**
     * @return The number of lives for this player.
     */
    int getLifeCount();
    
    /**
     * @return The bomb range for this player.
     */
    int getBombRange();
    
    /**
     * @return The number of last frame of diarrhea disease for this player.
     */
    int getDiarrheaEndsAtFrame();
    
    /**
     * @return The number of last frame of immortality bonus for this player.
     */
    int getImmortalityEndsAtFrame();
    
    /**
     * @return The number of last frame of max range bonus for this player.
     */
    int getMaxRangeEndsAtFrame();
    
    /**
     * @return The number of last frame of no bombs disease for this player.
     */
    int getNoBombsEndsAtFrame();
    
    /**
     * @return The number of last frame of speed up bonus for this player.
     */
    int getSpeedUpEndsAtFrame();
    
    /**
     * @return The number of last frame of slow down disease for this player.
     */
    int getSlowDownEndsAtFrame();
    
    /**
     * @return The number of last frame of crate walking bonus for this player.
     */
    int getCrateWalkingEndsAtFrame();
    
    /**
     * @return The number of last frame of bomb walking bonus for this player.
     */
    int getBombWalkingEndsAtFrame();
    
    /**
     * @return The number of last frame of controller reverse disease for this player.
     */
    int getControllerReverseEndsAtFrame();
    
    /**
     * @return The player has got Ahmed bonus.
     */
    boolean isAhmed();
}
