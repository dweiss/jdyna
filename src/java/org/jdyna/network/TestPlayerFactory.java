package org.jdyna.network;

import org.jdyna.players.CustomControllerPlayerFactory;
import org.jdyna.view.swing.AWTKeyboardController;

/**
 * Test controller bound to AWT keyboard hooks.
 */
final class TestPlayerFactory extends CustomControllerPlayerFactory
{
    public TestPlayerFactory()
    {
        super(AWTKeyboardController.getDefaultKeyboardController(0));
    }
}
