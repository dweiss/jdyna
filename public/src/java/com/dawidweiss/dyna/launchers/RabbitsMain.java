package com.dawidweiss.dyna.launchers;

import java.awt.Dimension;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.dawidweiss.dyna.Board;
import com.dawidweiss.dyna.BoardInfo;
import com.dawidweiss.dyna.Boards;
import com.dawidweiss.dyna.Game;
import com.dawidweiss.dyna.GameResult;
import com.dawidweiss.dyna.Globals;
import com.dawidweiss.dyna.IPlayerController;
import com.dawidweiss.dyna.Player;
import com.dawidweiss.dyna.audio.jxsound.GameSoundEffects;
import com.dawidweiss.dyna.players.Rabbit;
import com.dawidweiss.dyna.serialization.GameWriter;
import com.dawidweiss.dyna.view.swing.BoardFrame;

/**
 * Start a game between a {@link Rabbit} and a human user.
 */
public final class RabbitsMain
{
    /* Command-line entry point. */
    public static void main(String [] args) throws IOException
    {
        /*
         * Load board configurations.
         */
        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        final Boards boards = Boards.read(new InputStreamReader(cl
            .getResourceAsStream("boards.conf"), "UTF-8"));

        final Board board = boards.get("small");

        final IPlayerController c1 = Globals.getDefaultKeyboardController(0);

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
        game.addListener(new GameSoundEffects());

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