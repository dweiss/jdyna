package org.jdyna;

/**
 * A factory for constructing {@link IPlayerController}. The factory will be used to
 * create computer players in the following way:
 * <ol>
 * <li>{@link #getDefaultPlayerName()} is called and an index of <code>-NUM</code> is
 * appended to it, where <code>NUM</code> is the index of the subsequent player from the
 * same factory,</li>
 * <li>{@link #getController(String)} is called for the player name created in the step
 * above.</li>
 * </ol>
 * 
 * <p>
 * Students, please use the following vendor syntax in {@link #getVendorName()}:
 * <pre>
 * First Last, First Last, [...]
 * </pre>
 * so, for example:
 * <pre>
 * Bill Bryson, Louis de Bernières
 * </pre>
 * would indicate two authors of the player's controller implementation.
 */
public interface IPlayerFactory
{
    /**
     * Constructs and returns a controller for the given player name.
     */
    IPlayerController getController(String playerName);

    /**
     * Return the default player name for this factory. See class documentation.
     */
    String getDefaultPlayerName();

    /**
     * Return the implementation author(s), see class documentation.
     */
    String getVendorName();
}
