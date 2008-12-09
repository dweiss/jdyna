package com.dawidweiss.dyna;

import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import com.dawidweiss.dyna.view.BoardInfo;
import com.dawidweiss.dyna.view.swing.BoardFrame;

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
         * Set up a single game between two players.
         */
        final Board board = boards.get(1);

        final IController c1 = new KeyboardController(KeyEvent.VK_UP, KeyEvent.VK_DOWN,
            KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_CONTROL);

        final IController c2 = new KeyboardController(KeyEvent.VK_R, KeyEvent.VK_F,
            KeyEvent.VK_D, KeyEvent.VK_G, KeyEvent.VK_Z);

        final BoardInfo boardInfo = new BoardInfo(
            new Dimension(board.width, board.height), 16);
        final Player p1 = new Player("Player 1", c1);
        final Player p2 = new Player("Player 2", c2);
        final Player p3 = new Player("Player 2", c2);
        final Player p4 = new Player("Player 2", c2);
        final Game game = new Game(board, boardInfo, p1, p2, p3, p4);
        game.setFrameRate(25);

        /*
         * Create and attach a view to the game.
         */

        final BoardFrame frame = new BoardFrame(boardInfo);
        game.addListener(frame);
        frame.setLocationByPlatform(true);
        frame.setIgnoreRepaint(true);
        frame.pack();
        frame.setFocusTraversalKeysEnabled(false);
        frame.setVisible(true);

        final GameResult result = game.run();
        Logger.getAnonymousLogger().info(result.toString());

        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                frame.dispose();
            }
        });
    }
}