package com.dawidweiss.dyna;

import java.awt.GraphicsConfiguration;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import javax.swing.JFrame;

/**
 * The <b>Dyna Blaster</b> game. Oh, yes.
 * <p>
 * This class starts a local two-player game, where players are controlled via the same
 * keyboard.
 */
public final class Main
{
    /* Command-line entry point. */
    public static void main(String [] args) throws IOException
    {
        /*
         * Load board configurations.
         */
        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        final List<Board> boards = Board.readBoards(new InputStreamReader(cl
            .getResourceAsStream("boards.conf"), "UTF-8"));

        /*
         * Load resources required for visualization and start board visualization. The
         * graphics is pretty much the original Dyna Blaster files, converted from Amiga
         * IFF to PNG and with manually set translucent color index.
         */

        final GraphicsConfiguration conf = ImageUtilities.getGraphicsConfiguration();
        final CellInfo mapping = CellInfoFactory.DYNA_CLASSIC;
        final BoardPanelResources resources = new BoardPanelResources(conf, mapping);

        final JFrame frame = new JFrame();
        final BoardPanel gamePanel = new BoardPanel(resources, boards.get(0));
        frame.getContentPane().add(gamePanel);
        frame.pack();
        frame.setLocationByPlatform(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setVisible(true);
    }
}