package org.jdyna.frontend.swing;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * Dialog-display utilities.
 */
public final class Dialogs
{
    /**
     * Select one from a list of options.
     */
    public static Object selectOneFromList(JFrame frame, 
        String title, String message, Object selected, Object... options)
    {
        if (options.length == 0) return null;
        if (options.length == 1) return options[0];

        return (String) JOptionPane.showInputDialog(frame, message,
            title, JOptionPane.QUESTION_MESSAGE,
            null, options, selected);
    }
    
    /**
     * Select one from a list of options.
     */
    public static String selectOneFromList(JFrame frame, 
        String title, String message, String selected, String... options)
    {
        if (options.length == 0) return null;
        if (options.length == 1) return options[0];

        return (String) JOptionPane.showInputDialog(frame, message,
            title, JOptionPane.QUESTION_MESSAGE,
            null, options, selected);
    }
}
