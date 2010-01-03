package org.jdyna.launchers;

import java.io.*;
import java.util.List;

import javax.swing.JFrame;

import org.jdyna.*;
import org.jdyna.serialization.*;
import org.jdyna.view.swing.ReplayFrame;
import org.kohsuke.args4j.Argument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * Replay a game saved previously with {@link GameWriter}.
 *
 * @see GameReplay
 * @see GameWriter
 */
public final class ReplaySavedGame
{
    private final static Logger logger = LoggerFactory.getLogger(ReplaySavedGame.class);

    @Argument(index = 0, metaVar = "file", required = true, usage = "Game log file.")
    private File gameLog;

    private ReplayFrame board;

    /**
     * Replay a stream of saved events.
     */
    private void start(File gameLog, List<IHighlightDetector.FrameRange> highlights)
    {
        try
        {
            /*
             * Preindex frames.
             */
            logger.info("Indexing frames.");
            GameReader reader = new GameReader(new FileInputStream(gameLog));
            final List<FrameData> frames = Lists.newArrayList();
            while (reader.nextFrame())
            {
                frames.add(new FrameData(reader.getFrame(), Lists.newArrayList(
                    reader.getEvents())));
            }

            logger.info("Frames: " + frames.size());

            logger.info("Replaying...");

            if (highlights != null) 
                this.board = new ReplayFrame(
                findGameConfiguration(frames), findBoardInfo(frames), frames, highlights);
            else 
                this.board = new ReplayFrame(findGameConfiguration(frames),
                findBoardInfo(frames), frames);
            board.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            board.setVisible(true);
        }
        catch (IOException e)
        {
            logger.error(e.getMessage(), e);
        }
    }

    /*
     * 
     */
    private BoardInfo findBoardInfo(List<FrameData> frames) throws IOException
    {
        for (FrameData fd : frames)
        {
            for (GameEvent ge : fd.events)
            {
                if (ge instanceof GameStartEvent)
                {
                    return ((GameStartEvent) ge).getBoardInfo();
                }
            }
        }
        throw new IOException("No board info in the replay stream.");
    }

    /*
     * 
     */
    private static GameConfiguration findGameConfiguration(List<FrameData> frames) throws IOException
    {
        for (FrameData fd : frames)
        {
            for (GameEvent ge : fd.events)
            {
                if (ge instanceof GameStartEvent)
                {
                    return ((GameStartEvent) ge).getConfiguration();
                }
            }
        }
        throw new IOException("No game configuration in the replay stream.");
    }

    /* Command-line entry point. */
    public static void main(String [] args) throws IOException
    {
        final ReplaySavedGame launcher = new ReplaySavedGame();
        if (CmdLine.parseArgs(launcher, args))
        {
            launcher.start(launcher.gameLog, null);
        }
    }
    
    /** 
     * Replays game from log file.
     * @param filename Path to log file
     * @param highlights Highlights data, if null highlights feature will be disabled
     */
    public static void ReplayGame(String filename,
        List<IHighlightDetector.FrameRange> highlights)
    {
        final ReplaySavedGame launcher = new ReplaySavedGame();
        launcher.start(new File(filename), highlights);
    }
}