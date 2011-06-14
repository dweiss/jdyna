package org.jdyna;


/**
 * Sound effect event.
 */
public class SoundEffectEvent extends GameEvent
{
    /**
     * @see GameEvent
     */
    private static final long serialVersionUID = 0x200812241355L;

    /** Which sound effect should we play? */
    public final SoundEffect effect;
    
    /** How many sound effects occurred in the frame? */
    public final int count;

    public SoundEffectEvent(SoundEffect effect, int count)
    {
        super(GameEvent.Type.SOUND_EFFECT);
        
        this.effect = effect;
        this.count = count;
    }
}
