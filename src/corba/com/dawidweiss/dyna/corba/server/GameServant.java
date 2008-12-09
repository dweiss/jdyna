package com.dawidweiss.dyna.corba.server;

import java.awt.Dimension;
import java.io.InputStreamReader;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dawidweiss.dyna.Board;
import com.dawidweiss.dyna.BoardIO;
import com.dawidweiss.dyna.Game;
import com.dawidweiss.dyna.GameResult;
import com.dawidweiss.dyna.Globals;
import com.dawidweiss.dyna.IGameListener;
import com.dawidweiss.dyna.Player;
import com.dawidweiss.dyna.Standing;
import com.dawidweiss.dyna.corba.Adapters;
import com.dawidweiss.dyna.corba.bindings.CBoardInfo;
import com.dawidweiss.dyna.corba.bindings.CBoardSnapshot;
import com.dawidweiss.dyna.corba.bindings.CPlayer;
import com.dawidweiss.dyna.corba.bindings.CStanding;
import com.dawidweiss.dyna.corba.bindings.ICControllerCallback;
import com.dawidweiss.dyna.corba.bindings.ICControllerCallbackHelper;
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
    public void add(ICGameListener listener)
    {
        listeners.add(listener);
    }

    /**
     * Remove a listener.
     */
    public void remove(ICGameListener listener)
    {
        listeners.remove(listener);
    }

    /**
     * Run the game.
     */
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
            final Board board = boards.get(1);

            final BoardInfo boardInfo = new BoardInfo(
                new Dimension(board.width, board.height), Globals.DEFAULT_CELL_SIZE);

            /*
             * Add all players as listeners of this game, since they are listeners anyway.
             */
            for (PlayerData p: players) 
            {
                add(p.controller);
            }
            
            /*
             * Create controller callbacks and start the game. 
             */
            final Player [] parray = new Player [this.players.size()];
            for (int i = 0; i < parray.length; i++)
            {
                final PlayerData pd = this.players.get(i);
                final PlayerCallbackServant servant = new PlayerCallbackServant();
                final ICControllerCallback callback = ICControllerCallbackHelper.narrow(
                    _poa().servant_to_reference(servant));
                pd.controller.onControllerSetup(callback);
                parray[i] = new Player(pd.info.name, servant);
            }

            fireGameStart(Adapters.adapt(boardInfo), playerList());

            /*
             * Run the game loop.
             */
            final int frameRate = 25;
            final Game game = new Game(board, boardInfo, parray);
            game.setFrameRate(frameRate);
            game.addListener(gameListener);
            final GameResult result = game.run();

            /*
             * Fire game end.
             */
            final CPlayer winner = lookup(result.winner.name);
            final List<Standing> standings = result.standings;
            final CStanding [] results = new CStanding[standings.size()];
            for (int i = 0; i < standings.size(); i++)
            {
                final CPlayer p = lookup(standings.get(i).player.name);
                results[i] = new CStanding(p.id, standings.get(i).victimNumber);
            }

            fireGameEnd(winner, results);
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

    /**
     * Lookup player by his or her name.
     */
    private CPlayer lookup(String name)
    {;
        CPlayer player = null;
        for (PlayerData pd : this.players)
        {
            if (pd.info.name.equals(name))
            {
                player = pd.info;
                break;
            }   
        }

        return player;
    }

    private void fireGameStart(CBoardInfo bi, CPlayer [] players)
    {
        for (ICGameListener l : listeners)
        {
            l.onStart(bi, players);
        }
    }

    private void fireGameEnd(CPlayer winner, CStanding [] standings)
    {
        for (ICGameListener l : listeners)
        {
            l.onEnd(winner, standings);
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
