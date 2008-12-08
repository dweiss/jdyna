package com.dawidweiss.dyna.corba;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    /** Identifier generator. */
    private final AtomicInteger idgen = new AtomicInteger(0);

    /**
     * A map of registered players.
     */
    private final Map<Integer, PlayerData> players = Maps.newHashMap();

    /**
     * Game server logger.
     */
    private final static Logger logger = Logger.getLogger("gameserver");

    /*
     * Create a new game. 
     */
    @Override
    public ICGame create(CPlayer [] players)
    {
        synchronized (this)
        {
            // Check if all the players exist and are idle.
            final List<PlayerData> gamePlayers = Lists.newArrayList();
            for (CPlayer p : players)
            {
                final PlayerData data = this.players.get(p.id);
                if (data == null)
                {
                    logger.warning("Player does not exist: " 
                        + p.id + " (" + p.name + ")");
                    throw new IllegalArgumentException();
                }

                if (!data.idle)
                {
                    logger.warning("Player is not idle: " 
                        + p.id + " (" + p.name + ")");
                    throw new IllegalArgumentException();
                }

                data.idle = false;
                gamePlayers.add(data);
            }

            // Pass the startup information to all the controllers.
            try
            {
                final GameServant gameServant = new GameServant(this, gamePlayers);
                final ICGame game = ICGameHelper.narrow(
                    _poa().servant_to_reference(gameServant));
                logger.info("New game created.");
                return game;
            }
            catch (Exception e)
            {
                logger.log(Level.SEVERE, "Unexpected game startup problem.", e);
                throw new RuntimeException(e);
            }
        }
    }

    /*
     * Idle players. 
     */
    @Override
    public CPlayer [] players()
    {
        synchronized (this)
        {
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
    @Override
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
