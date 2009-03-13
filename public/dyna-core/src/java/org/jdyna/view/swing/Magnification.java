package org.jdyna.view.swing;

/**
 * Fixed-threshold magnification levels for {@link BoardPanel}.
 */
public enum Magnification
{
    TIMES_1(1),
    TIMES_2(2),
    TIMES_3(3),
    TIMES_4(4);

    public final double scaleFactor;
    
    private Magnification(double scaleFactor)
    {
        this.scaleFactor = scaleFactor;
    }
}
