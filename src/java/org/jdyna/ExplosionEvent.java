package org.jdyna;

import java.util.List;


public class ExplosionEvent extends GameEvent
{
    /**
     * @see GameEvent#serialVersionUID
     */
    private static final long serialVersionUID = 0x200912130232L;

    /**
     * Explosion metadata.
     */
    private List<ExplosionMetadata> metadata;

    /* */
    protected ExplosionEvent(List<ExplosionMetadata> meta)
    {
        super(GameEvent.Type.EXPLOSION_METADATA);
        this.metadata = meta;
    }

    /**
     * @return Return the explosion metadata.
     */
    public List<ExplosionMetadata> getMetadata()
    {
        return metadata;
    }
}
