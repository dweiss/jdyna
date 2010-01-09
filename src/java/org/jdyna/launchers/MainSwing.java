package org.jdyna.launchers;

import java.awt.Dimension;
import java.io.*;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.jdyna.*;
import org.jdyna.audio.jxsound.JavaSoundSFX;
import org.jdyna.serialization.GameWriter;
import org.jdyna.view.swing.AWTKeyboardController;
import org.jdyna.view.swing.BoardFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * Start a game using a Swing view and JavaSFX:
 * <ul>
 *  <li>two players,</li>
 *  <li>one game,</li>
 *  <li>both players controlled via keyboard (default mappings).</li>
 * </ul>
 */
public final class MainSwing
{
    private final static Logger logger = LoggerFactory.getLogger(MainSwing.class);

    final static List<IHighlightDetector.FrameRange> highlights = Lists.newArrayList();

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

        final IPlayerController c1 = AWTKeyboardController.getDefaultKeyboardController(0);
        final IPlayerController c2 = AWTKeyboardController.getDefaultKeyboardController(1);

        final BoardInfo boardInfo = new BoardInfo(
            new Dimension(board.width, board.height), Constants.DEFAULT_CELL_SIZE);

        final GameConfiguration conf = new GameConfiguration();
        final Game game = new Game(conf, board, boardInfo);

        final Player p1 = new Player("Player 1", c1);
        final Player p2 = new Player("Player 2", c2);
        final Player p3 = new Player("Player 3", c2);
        final Player p4 = new Player("Player 4", c2);

        final IPlayerSprite [] infos = new IPlayerSprite [] {
            game.addPlayer(p1), 
            game.addPlayer(p2),
            game.addPlayer(p3),
            game.addPlayer(p4),
        };

        /*
         * Attach sounds view to the game.
         */
        game.addListener(new JavaSoundSFX());

        /*
         * Attach game progress saver.
         */
        File temporaryLogFile = File.createTempFile("game", ".log");
        game.addListener(new GameWriter(new FileOutputStream(temporaryLogFile)));

        /*
         * Attach explosion location logger.
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
                    if (e instanceof HighlightEvent) {
                        highlights.add(((HighlightEvent) e).getFrameRange());
                    }
                }
            }
        });

        /*
         * Attach a display view to the game.
         */
        final BoardFrame frame = new BoardFrame();
        frame.showStatusFor(infos);
        game.addListener(frame);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        final GameResult result = game.run(Game.Mode.DEATHMATCH);
        logger.info(result.toString());
        ReplaySavedGame.ReplayGame(temporaryLogFile.getAbsolutePath(), highlights);

        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                frame.dispose();
            }
        });
    }
}