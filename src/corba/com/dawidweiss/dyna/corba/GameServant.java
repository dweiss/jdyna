package com.dawidweiss.dyna.corba;

import java.awt.Dimension;
import java.io.InputStreamReader;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dawidweiss.dyna.Board;
import com.dawidweiss.dyna.BoardIO;
import com.dawidweiss.dyna.Game;
import com.dawidweiss.dyna.Globals;
import com.dawidweiss.dyna.IGameListener;
import com.dawidweiss.dyna.Player;
import com.dawidweiss.dyna.corba.bindings.CBoardInfo;
import com.dawidweiss.dyna.corba.bindings.CBoardSnapshot;
import com.dawidweiss.dyna.corba.bindings.CPlayer;
import com.dawidweiss.dyna.corba.bindings.CStanding;
import com.dawidweiss.dyna.corba.bindings.ICGame;
import com.dawidweiss.dyna.corba.bindings.ICGameListener;
import com.dawidweiss.dyna.corba.bindings.ICGamePOA;
import com.dawidweiss.dyna.view.BoardInfo;
import com.dawidweiss.dyna.view.IBoardSnapshot;
import com.google.common.collect.Lists;

/**
 * Servant for {@link ICGame}. 
 */
class GameServant extends ICGamePOA
{
    /** Game server (to release players after game is over). */
    private final GameServerServant gameServer;

    /** Game server (to release players after game is over). */
    private final List<PlayerData> players;

    /** All listeners. */
    private final List<ICGameListener> listeners = Lists.newArrayList();

    private Logger logger = Logger.getLogger("game");

    private IGameListener gameListener = new IGameListener()
    {
        public void onNextFrame(int frame, IBoardSnapshot snapshot)
        {
            fireNextFrame(frame, Adapters.adapt(snapshot));
        }
    };
    
    /*
     * 
     */
    public GameServant(GameServerServant gameServer, List<PlayerData> gamePlayers)
    {
        this.gameServer = gameServer;
        this.players = gamePlayers;
    }

    /**
     * Add a listener.
     */
    @Override
    public void add(ICGameListener listener)
    {
        listeners.add(listener);
    }

    /**
     * Remove a listener.
     */
    @Override
    public void remove(ICGameListener listener)
    {
        listeners.remove(listener);
    }

    /**
     * Run the game.
     */
    @Override
    public void run()
    {
        try
        {
            /*
             * Load board data.
             */
            final ClassLoader cl = Thread.currentThread().getContextClassLoader();
            final List<Board> boards = BoardIO.readBoards(new InputStreamReader(cl
                .getResourceAsStream("boards.conf"), "UTF-8"));
            final Board board = boards.get(0);

            final BoardInfo boardInfo = new BoardInfo(
                new Dimension(board.width, board.height), Globals.DEFAULT_CELL_SIZE);

            /*
             * Prepare players and listeners.
             */

            for (PlayerData p: players) 
            {
                add(p.controller);
            }

            fireGameStart(Adapters.adapt(boardInfo), playerList());

            /*
             * Run the game loop.
             */
            final int frameRate = 25;
            final Player [] parray = new Player [this.players.size()];
            final RemotePlayerController [] controllers = new RemotePlayerController [parray.length];
            for (int i = 0; i < parray.length; i++)
            {
                final PlayerData pd = this.players.get(i);
                controllers[i] = new RemotePlayerController(pd);
                controllers[i].start();
                parray[i] = new Player(pd.info.name, controllers[i]);
            }

            final Game game = new Game(board, boardInfo, parray);
            game.setFrameRate(frameRate);
            game.addListener(gameListener);
            game.run();

            /*
             * Fire game end.
             * TODO: Propagate the results (winner, standings).
             */
            fireGameEnd(new CStanding [0]);
        }
        catch (Throwable t)
        {
            logger.log(Level.SEVERE, "Unhandled exception during game execution.", t);
            throw new RuntimeException();
        }
        finally
        {
            /*
             * Cleanup after the game and remove the game object.
             */
            gameServer.release(this.players);

            /*
             * Remove the servant from the POA.
             */
            try
            {
                _poa().deactivate_object(_object_id());
            }
            catch (Throwable t)
            {
                logger.severe("Could not deactivate game object.");
            }
        }
    }

    private void fireGameStart(CBoardInfo bi, CPlayer [] players)
    {
        for (ICGameListener l : listeners)
        {
            l.onStart(bi, players);
        }
    }

    private void fireGameEnd(CStanding [] standings)
    {
        for (ICGameListener l : listeners)
        {
            l.onEnd(standings);
        }
    }

    /*
     * 
     */
    protected void fireNextFrame(int frame, CBoardSnapshot snapshot)
    {
        for (ICGameListener l : listeners)
        {
            l.onNextFrame(frame, snapshot);
        }
    }

    private CPlayer [] playerList()
    {
        final CPlayer [] players = new CPlayer [this.players.size()];
        for (int i = 0; i < players.length; i++)
        {
            players[i] = this.players.get(i).info;
        }
        return players;
    }
}
