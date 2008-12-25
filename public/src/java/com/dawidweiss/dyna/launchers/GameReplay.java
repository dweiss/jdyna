package com.dawidweiss.dyna.launchers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.apache.commons.io.IOUtils;
import org.h2.compress.LZFInputStream;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import com.dawidweiss.dyna.GameEvent;
import com.dawidweiss.dyna.GameStartEvent;
import com.dawidweiss.dyna.GameTimer;
import com.dawidweiss.dyna.Globals;
import com.dawidweiss.dyna.IGameEventListener;
import com.dawidweiss.dyna.GameEvent.Type;
import com.dawidweiss.dyna.audio.jxsound.GameSoundEffects;
import com.dawidweiss.dyna.serialization.GameWriter;
import com.dawidweiss.dyna.view.swing.BoardFrame;
import com.google.common.collect.Lists;

/**
 * Replay a game saved previously with {@link GameWriter}.
 * 
 * @see GameWriter
 */
public final class GameReplay
{
    @Option(required = false, name = "-r", aliases =
    {
        "--frame-rate"
    }, metaVar = "fps", usage = "Frames per second (default: "
        + Globals.DEFAULT_FRAME_RATE + ")")
    private double frameRate = Globals.DEFAULT_FRAME_RATE;

    @Argument(index = 0, metaVar = "file", required = true, usage = "Game log file.")
    private File gameLog;

    /**
     * Replay a stream of saved events at the given frame rate.
     */
    public void replay(double frameRate, InputStream stream)
    {
        BoardFrame board = null;
        GameSoundEffects sound = null;
        ObjectInputStream ois = null;

        try
        {
            ois = new ObjectInputStream(new LZFInputStream(stream));

            final GameTimer timer = new GameTimer(frameRate);
            final List<IGameEventListener> listeners = Lists.newArrayList();
            final List<GameEvent> events = Lists.newArrayList();
            boolean gameOver = false;
            do
            {
                // Wait for frame.
                timer.waitForFrame();

                /*
                 * Deserialize frame data.
                 */
                final int frame = ois.readInt();
                events.clear();
                short eventCount = ois.readShort();
                while (eventCount-- > 0)
                {
                    final GameEvent e = (GameEvent) ois.readObject();

                    if (e.type == Type.GAME_START)
                    {
                        board = new BoardFrame(((GameStartEvent) e).getBoardInfo());
                        board.setVisible(true);
                        board.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                        listeners.add(board);

                        sound = new GameSoundEffects();
                        listeners.add(sound);
                    }
                    
                    if (e.type == Type.GAME_OVER)
                    {
                        gameOver = true;
                    }

                    events.add(e);
                }

                // Dispatch events.
                for (IGameEventListener l : listeners)
                {
                    l.onFrame(frame, events);
                }
            }
            while (!gameOver);
        }
        catch (IOException e)
        {
            Logger.getAnonymousLogger().log(Level.SEVERE, e.getMessage(), e);
        }
        catch (ClassNotFoundException e)
        {
            Logger.getAnonymousLogger().severe(
                "Class not found when deserializing: " + e.getMessage());
        }
        finally
        {
            IOUtils.closeQuietly(ois);
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
        final GameReplay launcher = new GameReplay();
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

        launcher.replay(launcher.frameRate, new FileInputStream(launcher.gameLog));
    }
}