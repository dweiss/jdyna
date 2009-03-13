package org.jdyna;

import java.awt.Point;

/**
 * A sprite is an object that is superimposed on top of {@link Board}'s cells. The name
 * is traditional.
 * <p>
 * All animation sequences for each {@link SpriteType} are split into "states". Each state
 * may have one or more image frames. The controller manages animation states and frames,
 * although their interpretation and mapping to actual images is up to the view.
 */
public interface ISprite
{
    /**
     * All sprite types available in the game.
     */
    public static enum Type
    {
        PLAYER_1, PLAYER_2, PLAYER_3, PLAYER_4;

        private static final Type [] PLAYER_SPRITES = new Type []
        {
            PLAYER_1, PLAYER_2, PLAYER_3, PLAYER_4
        };

        /**
         * All "player" sprites.
         */
        public static Type [] getPlayerSprites()
        {
            return PLAYER_SPRITES;
        }
    }

    /**
     * The type of this sprite.
     */
    Type getType();

    /**
     * Current animation state (managed for each {@link SpriteType} by the controller, but
     * interpreted by views.
     */
    int getAnimationState();

    /**
     * Current animation frame (managed for each {@link SpriteType} by the controller, but
     * interpreted by views.
     */
    int getAnimationFrame();

    /**
     * Current position of the sprite's centerpoint (the sprite is at this position, the
     * view needs to determine image offsets).
     */
    Point getPosition();
}
