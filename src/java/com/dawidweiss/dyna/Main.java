package com.dawidweiss.dyna;

import java.awt.GraphicsConfiguration;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.dawidweiss.dyna.view.swing.BoardPanel;

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
        final List<Board> boards = BoardIO.readBoards(new InputStreamReader(cl
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
                KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT,
                KeyEvent.VK_CONTROL);

        final IController c2 = new KeyboardController(
            KeyEvent.VK_R, KeyEvent.VK_F, KeyEvent.VK_D, KeyEvent.VK_G,
            KeyEvent.VK_Z);

        final Player p1 = new Player("Player 1", c1);
        final Player p2 = new Player("Player 2", c2);
        final Game game = new Game(board, resources, p1, p2);
        game.setFrameRate(25);

        /*
         * Create and attach a view to the game.
         */

        final JFrame frame = new JFrame(conf);
        final BoardPanel gamePanel = new BoardPanel(resources, game);
        frame.getContentPane().add(gamePanel);
        frame.setLocationByPlatform(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setIgnoreRepaint(true);
        frame.pack();
        frame.setFocusTraversalKeysEnabled(false);
        frame.setVisible(true);

        GameResult result = game.run();

        System.out.println(result);
        SwingUtilities.invokeLater(new Runnable() {
            public void run()
            {
                frame.dispose();
            }
        });
    }
}