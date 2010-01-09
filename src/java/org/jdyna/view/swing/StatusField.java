package org.jdyna.view.swing;


/**
 * A single status field.
 */
enum StatusField
{
    LIVES("Lives"),
    BOMBS("Bombs"),
    BOMB_RANGE("Bomb range"),
    MAX_RANGE("Max range"),
    AHMED("Ahmed"),
    SPEED_UP("Speed up"),
    CRATE_WALKING("Crate walking"),
    BOMB_WALKING("Bomb walking"),
    IMMORTALITY("Immortality"),
    DIARRHOEA("Diarrhoea"),
    NO_BOMBS("No bombs"),
    SLOW_DOWN("Slow down"),
    CTRL_REVERSE("Controller reversed");
    
    /** Label for the status field. Could be a resource handle for i18n later on. */
    private final String label;
    
    /* */
    private StatusField(String label)
    {
        this.label = label;
    }
    
    /* */
    @Override
    public String toString()
    {
        return label;
    }
}
