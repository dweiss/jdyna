package org.jdyna.view.jme.adapter;



public abstract class AbstractGameAdapter
{
    public enum BonusType
    {
        EXTRA_RANGE, EXTRA_BOMB, OTHER_BONUS
    };
    
    public enum DynaCell {
        EMPTY, WALL, CRATE, BOMB, BONUS_RANGE, BONUS_BOMB, OTHER_CELL
    }

    public abstract void dispatchEvents(GameListener gameListener, boolean wait);
}
