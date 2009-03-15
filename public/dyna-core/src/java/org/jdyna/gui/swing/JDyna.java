package org.jdyna.gui.swing;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.*;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.TreeSet;

import javax.swing.*;

import org.jdyna.*;
import org.jdyna.audio.jxsound.GameSoundEffects;
import org.jdyna.players.HumanPlayerFactory;
import org.jdyna.players.RabbitFactory;
import org.jdyna.view.resources.ImageUtilities;
import org.jdyna.view.swing.BoardFrame;
import org.jdyna.view.swing.SwingUtils;

import com.google.common.collect.Maps;
import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.looks.LookUtils;
import com.jgoodies.looks.Options;

/**
 * Simple Swing GUI to JDyna.
 */
public final class JDyna
{
    /**
     * Default GUI elements spacing.
     */
    private final static int SPACING = 4;

    /**
     * Main launcher frame.
     */
    private JFrame frame;

    /**
     * All available boards.
     */
    private Boards boards;

    /**
     * Enable sounds effects?
     */
    private boolean sound = false;

    /**
     * Remember most recent board selection.
     */
    private String mostRecentBoard = "classic";

    /**
     * Bots.
     */
    private static final HashMap<String, IPlayerFactory> bots;
    static
    {
        bots = Maps.newHashMap();
        
        final IPlayerFactory [] factories = new IPlayerFactory [] {
            new RabbitFactory(),
        };

        for (IPlayerFactory i : factories)
            bots.put(i.getDefaultPlayerName(), i);
    }
    
    /*
     * 
     */
    private void start()
    {
        /*
         * Load board configurations.
         */
        try
        {
            final ClassLoader cl = Thread.currentThread().getContextClassLoader();
            this.boards = Boards.read(
                new InputStreamReader(cl.getResourceAsStream("boards.conf"), "UTF-8"));
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }

        /*
         * Initialize the main GUI.
         */
        frame = new JFrame("JDyna.com");
        frame.getContentPane().add(createMainPanelGUI());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationByPlatform(true);
        try
        {
            frame.setIconImage(ImageUtilities.loadResourceImage("icons/window-icon.png"));
        }
        catch (IOException e)
        {
            // Ignore if icon not found.
        }

        frame.pack();
        frame.setVisible(true);
    }

    /*
     * 
     */
    private JComponent createMainPanelGUI()
    {
        final JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(SPACING, SPACING, SPACING, SPACING));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(createSectionTitleGUI("Local"));
        panel.add(createLocalModeGUI());

        panel.add(Box.createVerticalStrut(10));
        panel.add(Box.createGlue());
        panel.add(new JSeparator());
        panel.add(Box.createVerticalStrut(4));
        panel.add(createBottomButtonsGUI());
        return panel;
    }

    /*
     * 
     */
    private JComponent createSectionTitleGUI(String text)
    {
        DefaultComponentFactory f = new DefaultComponentFactory();
        return f.createSeparator(text);
    }
    
    /*
     * 
     */
    private JComponent createBottomButtonsGUI()
    {
        final ButtonBarBuilder builder = new ButtonBarBuilder();
        
        final JButton quitButton = new JButton("Quit");
        quitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                frame.dispose();
            }
        });

        builder.addGlue();
        builder.addGridded(quitButton);
        return builder.getPanel();
    }

    /*
     * 
     */
    private Component createLocalModeGUI()
    {
        final JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

        final JButton twoPlayersGameButton = new JButton("Human vs. Human");
        twoPlayersGameButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                runTwoPlayersGame();
            }
        });
        panel.add(twoPlayersGameButton);

        panel.add(Box.createHorizontalStrut(SPACING));

        final JButton onePlayerGameButton = new JButton("Human vs. Computer");
        onePlayerGameButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                runOnePlayerGame();
            }
        });        
        panel.add(onePlayerGameButton);

        return panel;
    }

    /**
     * Run a local game between two players. 
     */
    private void runTwoPlayersGame()
    {
        assert SwingUtilities.isEventDispatchThread();

        final Board board = selectBoard();
        if (board == null) return;

        hideMainGUI();
        runLocalGame(
            board,
            new HumanPlayerFactory(Globals.getDefaultKeyboardController(0), "Player 1"),
            new HumanPlayerFactory(Globals.getDefaultKeyboardController(1), "Player 2"));
    }

    /**
     * Run one player game (vs. bot(s)). 
     */
    private void runOnePlayerGame()
    {
        assert SwingUtilities.isEventDispatchThread();

        final Board board = selectBoard();
        if (board == null) return;

        final String bot = Dialogs.selectOneFromList(frame,
            "Select opponent", "Select opponent",
            null, "Rabbit");
        if (bot == null) return;

        hideMainGUI();
        runLocalGame(
            board,
            new HumanPlayerFactory(Globals.getDefaultKeyboardController(0), "Player"),
            getBot(bot));        
    }

    /**
     * Get bot for a given name.
     */
    private IPlayerFactory getBot(String bot)
    {
        return bots.get(bot);
    }

    /**
     * Run a local game.
     */
    private void runLocalGame(Board board, IPlayerFactory... players)
    {
        final BoardInfo boardInfo = new BoardInfo(
            new Dimension(board.width, board.height), Globals.DEFAULT_CELL_SIZE);

        final Game game = new Game(board, boardInfo);
        game.setFrameRate(Globals.DEFAULT_FRAME_RATE);

        for (IPlayerFactory pf : players)
        {
            final String name = pf.getDefaultPlayerName();
            game.addPlayer(new Player(name, pf.getController(name)));
        }

        /*
         * Attach sound effects view to the game.
         */
        if (sound) game.addListener(new GameSoundEffects());

        /*
         * Attach a swing display view to the game.
         */
        final BoardFrame frame = new BoardFrame();
        game.addListener(frame);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        final Thread gameThread = new Thread() {
            @SuppressWarnings("unused")
            public void run()
            {
                final GameResult result = game.run(Game.Mode.INFINITE_DEATHMATCH);
                SwingUtils.dispose(frame);
            }
        };

        frame.addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent e)
            {
                game.interrupt();
                try
                {
                    gameThread.join();
                }
                catch (InterruptedException e1)
                {
                    throw new RuntimeException();
                }

                showMainGUI();
            }
        });
        frame.setVisible(true);
        gameThread.start();
    }

    /**
     * Prompt the user to select one of the boards.
     */
    private Board selectBoard()
    {
        final TreeSet<String> boardNames = 
            new TreeSet<String>(boards.getBoardNames());

        String boardName = Dialogs.selectOneFromList(frame,
            "Select game board", "Select game board",
            mostRecentBoard,
            (String []) boardNames.toArray(new String [boardNames.size()]));
        
        if (boardName == null) return null;
        mostRecentBoard = boardName;
        return boards.get(boardName);
    }

    /*
     * 
     */
    private void hideMainGUI()
    {
        assert SwingUtilities.isEventDispatchThread();
        frame.setVisible(false);
    }

    /*
     * 
     */
    private void showMainGUI()
    {
        assert SwingUtilities.isEventDispatchThread();
        frame.setVisible(true);
    }

    /**
     * Command line entry point.
     */
    public static void main(String [] args)
    {
        try
        {
            final String lafName = LookUtils.IS_OS_WINDOWS_XP ? Options
                .getCrossPlatformLookAndFeelClassName() : Options
                .getSystemLookAndFeelClassName();

            UIManager.setLookAndFeel(lafName);
        }
        catch (Exception e)
        {
            // Ignore if not found.
        }

        new JDyna().start();
    }
}
