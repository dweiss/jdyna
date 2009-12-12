package org.jdyna.launchers;

import java.awt.Dimension;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.jdyna.*;
import org.jdyna.audio.jxsound.JavaSoundSFX;
import org.jdyna.players.HumanPlayerFactory;
import org.jdyna.players.Rabbit;
import org.jdyna.serialization.GameWriter;
import org.jdyna.view.swing.BoardFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Start a game between a {@link Rabbit} and a human user.
 */
public final class RabbitsMain
{
    private final static Logger logger = LoggerFactory.getLogger(RabbitsMain.class);

    /* Command-line entry point. */
    public static void main(String [] args) throws IOException
    {
        logger.info("Starting the game.");

        /*
         * Load board configurations.
         */
        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        final Boards boards = Boards.read(new InputStreamReader(cl
            .getResourceAsStream("boards.conf"), "UTF-8"));

        final Board board = boards.get("classic-empty");

        final IPlayerController c1 = HumanPlayerFactory.getDefaultKeyboardController(0);

        final BoardInfo boardInfo = new BoardInfo(
            new Dimension(board.width, board.height), Globals.DEFAULT_CELL_SIZE);

        final Game game = new Game(board, boardInfo, 
            new Player("Player 1", c1),
            Rabbit.createPlayer("Rabbit 1"),
            Rabbit.createPlayer("Rabbit 2"),
            Rabbit.createPlayer("Rabbit 3")
        );
        game.setFrameRate(25);

        /*
         * Attach sounds view to the game.
         */
        game.addListener(new JavaSoundSFX());

        /*
         * Attach game progress saver.
         */
        game.addListener(new GameWriter(new FileOutputStream("game.log")));

        /*
         * Attach a display view to the game.
         */
        final BoardFrame frame = new BoardFrame();
        game.addListener(frame);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        final GameResult result = game.run(Game.Mode.DEATHMATCH);
        logger.info(result.toString());

        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                frame.dispose();
            }
        });
    }
}