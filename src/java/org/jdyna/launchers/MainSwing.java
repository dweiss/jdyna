package org.jdyna.launchers;

import java.awt.Dimension;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.jdyna.*;
import org.jdyna.audio.jxsound.JavaSoundSFX;
import org.jdyna.frontend.swing.Configuration.ViewType;
import org.jdyna.players.HumanPlayerFactory;
import org.jdyna.serialization.GameWriter;
import org.jdyna.view.swing.BoardFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Start a game using local Swing view:
 * <ul>
 *  <li>two players,</li>
 *  <li>one game,</li>
 *  <li>both players controlled via keyboard (default mappings).</li>
 * </ul>
 */
public final class MainSwing
{
    private final static Logger logger = LoggerFactory.getLogger(Main3D.class);

    /* Command-line entry point. */
    public static void main(String [] args) throws IOException
    {
        /*
         * Load board configurations.
         */
        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        final Boards boards = Boards.read(new InputStreamReader(cl
            .getResourceAsStream("boards.conf"), "UTF-8"));

        /*
         * Set up a single game between two players.
         */
        final Board board = boards.get("classic-random");

        final IPlayerController c1 = HumanPlayerFactory.getDefaultKeyboardController(0, ViewType.SWING_VIEW);
        final IPlayerController c2 = HumanPlayerFactory.getDefaultKeyboardController(1, ViewType.SWING_VIEW);

        final BoardInfo boardInfo = new BoardInfo(
            new Dimension(board.width, board.height), Constants.DEFAULT_CELL_SIZE);

        final GameConfiguration conf = new GameConfiguration();
        final Game game = new Game(conf, board, boardInfo);

        final Player p1 = new Player("Player 1", c1);
        final Player p2 = new Player("Player 2", c2);
        new Thread() {
            public void run()
            {
                try
                {
                    Thread.sleep(2000);
                }
                catch (InterruptedException e) 
                {
                    // Ignore.
                }

                game.addPlayer(p1);
                game.addPlayer(p2);
            }
        }.start();

        /*
         * Attach sounds view to the game.
         */
        game.addListener(new JavaSoundSFX());

        /*
         * Attach game progress saver.
         */
        game.addListener(new GameWriter(new FileOutputStream("game.log")));

        /*
         * Attach exlosion location logger.
         */
        game.addListener(new IGameEventListener()
        {
            public void onFrame(int frame, List<? extends GameEvent> events)
            {
                for (GameEvent e : events)
                {
                    if (e instanceof ExplosionEvent)
                    {
                        for (ExplosionMetadata m : ((ExplosionEvent) e).getMetadata())
                        {
                            logger.info("Explosion at frame: "
                                + frame + ", position: "
                                + m.getPosition() + ", range: "
                                + m.getRange());
                        }
                    }
                }
            }
        });
        
        /*
         * Attach a display view to the game.
         */
        final BoardFrame frame = new BoardFrame();
        game.addListener(frame);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        final GameResult result = game.run(Game.Mode.INFINITE_DEATHMATCH);
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