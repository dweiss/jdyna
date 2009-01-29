package com.dawidweiss.dyna.launchers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dawidweiss.dyna.Globals;
import com.dawidweiss.dyna.audio.jxsound.GameSoundEffects;
import com.dawidweiss.dyna.serialization.GameReplay;
import com.dawidweiss.dyna.serialization.GameWriter;
import com.dawidweiss.dyna.view.swing.BoardFrame;

/**
 * Replay a game saved previously with {@link GameWriter}.
 *
 * @see GameReplay
 * @see GameWriter
 */
public final class ReplaySavedGame
{
    private final static Logger logger = LoggerFactory.getLogger(ReplaySavedGame.class);

    @Option(required = false, name = "-r", aliases =
    {
        "--frame-rate"
    }, metaVar = "fps", usage = "Frames per second.")
    private double frameRate = Globals.DEFAULT_FRAME_RATE;

    @Argument(index = 0, metaVar = "file", required = true, usage = "Game log file.")
    private File gameLog;

    private BoardFrame board;
    private GameSoundEffects sound;

    /**
     * Replay a stream of saved events at the given frame rate.
     */
    private void start()
    {
        try
        {
            this.sound = new GameSoundEffects();
            this.board = new BoardFrame();
            board.setVisible(true);
            board.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            final GameReplay replay = new GameReplay();
            replay.addListener(board);
            replay.addListener(sound);
            replay.replay(frameRate, new FileInputStream(gameLog));
        }
        catch (IOException e)
        {
            logger.error(e.getMessage(), e);
        }
        finally
        {
            if (board != null) 
            {
                final BoardFrame b = board;
                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        b.dispose();
                    }
                });
            }

            if (sound != null)
            {
                sound.dispose();
            }
        }
    }

    /* Command-line entry point. */
    public static void main(String [] args) throws IOException
    {
        final ReplaySavedGame launcher = new ReplaySavedGame();
        final CmdLineParser parser = new CmdLineParser(launcher);
        parser.setUsageWidth(80);

        try
        {
            parser.parseArgument(args);
        }
        catch (CmdLineException e)
        {
            PrintStream ps = System.out;
            ps.print("Usage: ");
            parser.printSingleLineUsage(ps);
            ps.println();
            parser.printUsage(ps);

            ps.println("\n" + e.getMessage());
            return;
        }

        launcher.start();
    }
}