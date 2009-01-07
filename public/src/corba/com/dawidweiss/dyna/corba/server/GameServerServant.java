package com.dawidweiss.dyna.corba.server;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dawidweiss.dyna.corba.bindings.CPlayer;
import com.dawidweiss.dyna.corba.bindings.ICGame;
import com.dawidweiss.dyna.corba.bindings.ICGameHelper;
import com.dawidweiss.dyna.corba.bindings.ICGameServer;
import com.dawidweiss.dyna.corba.bindings.ICGameServerPOA;
import com.dawidweiss.dyna.corba.bindings.ICPlayerController;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Servant for {@link ICGameServer}.
 */
class GameServerServant extends ICGameServerPOA
{
    private final static Logger logger = LoggerFactory.getLogger("corba.gameserver");

    /** Identifier generator. */
    private final AtomicInteger idgen = new AtomicInteger(0);

    /**
     * A map of registered players.
     */
    private final Map<Integer, PlayerData> players = Maps.newHashMap();

    /*
     * Create a new game. 
     */
    public ICGame create(int board, CPlayer [] players)
    {
        synchronized (this)
        {
            pruneDead();

            // Check if all the players exist and are idle.
            final List<PlayerData> gamePlayers = Lists.newArrayList();
            for (CPlayer p : players)
            {
                final PlayerData data = this.players.get(p.id);
                if (data == null)
                {
                    logger.warn("Player does not exist: " 
                        + p.id + " (" + p.name + ")");
                    throw new IllegalArgumentException();
                }

                if (!data.idle)
                {
                    logger.warn("Player is not idle: " 
                        + p.id + " (" + p.name + ")");
                    throw new IllegalArgumentException();
                }

                data.idle = false;
                gamePlayers.add(data);
            }

            // Pass the startup information to all the controllers.
            try
            {
                /*
                 * TODO: Each game should be created on its own POA which should be destroyed
                 * after the game is over.
                 */

                final GameServant gameServant = new GameServant(this, board, gamePlayers);
                final ICGame game = ICGameHelper.narrow(
                    _poa().servant_to_reference(gameServant));
                logger.info("New game created.");
                return game;
            }
            catch (Exception e)
            {
                logger.error("Unexpected game startup problem.", e);
                throw new RuntimeException(e);
            }
        }
    }

    /*
     * 
     */
    private void pruneDead()
    {
        logger.info("Pruning dead players.");

        Iterator<PlayerData> i = players.values().iterator();
        while (i.hasNext())
        {
            final PlayerData p = i.next();
            try
            {
                if (p.controller._non_existent())
                {
                    logger.info("Pruning dead: " + p.info.name);
                    i.remove();
                }
            }
            catch (Throwable t)
            {
                logger.info("Pruning inaccessible: " + p.info.name);
                i.remove();
            }
        }
    }

    /*
     * Idle players. 
     */
    public CPlayer [] players()
    {
        synchronized (this)
        {
            pruneDead();
            
            final ArrayList<CPlayer> idle = Lists.newArrayList();
            for (PlayerData p : players.values())
            {
                if (p.idle) idle.add(p.info);
            }
            return idle.toArray(new CPlayer [idle.size()]);
        }
    }

    /*
     * 
     */
    public CPlayer register(String name, ICPlayerController controller)
    {
        synchronized (this)
        {
            final PlayerData p = new PlayerData(
                new CPlayer(idgen.getAndIncrement(), name), controller);
            players.put(p.info.id, p);
            logger.info("Registered new player: " + name);
            return p.info;
        }
    }

    /*
     * 
     */
    public void release(List<PlayerData> players)
    {
        synchronized (this)
        {
            for (PlayerData p : players)
            {
                this.players.get(p.info.id).idle = true;
            }
        }
    }
}
