package com.dawidweiss.dyna;

import java.awt.GraphicsConfiguration;
import java.awt.event.KeyEvent;
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
        final BoardData resources = BoardDataFactory.getDynaClassic(conf);

        /*
         * Set up a single game between two players. 
         */
        final Board board = boards.get(0);
        final IController c1 = new KeyboardController(
                KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT);
        final Player p1 = new Player("1", c1);
        final Game game = new Game(board, resources, p1,
            new Player("2", c1), new Player("3", c1), new Player("4", c1));
        game.setFrameRate(10);

        /* 
         * Create and attach a view to the game.
         */

        final JFrame frame = new JFrame();
        final BoardPanel gamePanel = new BoardPanel(resources, game);
        frame.getContentPane().add(gamePanel);
        frame.pack();
        frame.setLocationByPlatform(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        game.run();
    }
}