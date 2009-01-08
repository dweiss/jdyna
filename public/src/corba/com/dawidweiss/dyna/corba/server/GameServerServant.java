package com.dawidweiss.dyna.corba.server;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.omg.CORBA.Policy;
import org.omg.PortableServer.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dawidweiss.dyna.corba.bindings.*;
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
            POA gamePOA = null;
            try
            {
                gamePOA = createGamePOA();

                final GameServant gameServant = new GameServant(this, board, gamePlayers);
                final ICGame game = ICGameHelper.narrow(
                    gamePOA.servant_to_reference(gameServant));

                logger.info("New game created.");
                return game;
            }
            catch (Exception e)
            {
                logger.error("Unexpected game startup problem.", e);

                if (gamePOA != null) gamePOA.destroy(true, false);
                throw new RuntimeException(e);
            }
        }
    }

    /*
     * Create a temporary POA for the game and its resources. 
     */
    private POA createGamePOA()
        throws Exception
    {
        final POA parentPOA = _poa();
        final Policy [] filePoaPolicies = new Policy [] {
            parentPOA.create_id_uniqueness_policy(IdUniquenessPolicyValue.UNIQUE_ID),
            parentPOA.create_id_assignment_policy(IdAssignmentPolicyValue.SYSTEM_ID),
            parentPOA.create_lifespan_policy(LifespanPolicyValue.TRANSIENT),
            parentPOA.create_servant_retention_policy(ServantRetentionPolicyValue.RETAIN),
            parentPOA.create_request_processing_policy(RequestProcessingPolicyValue.USE_ACTIVE_OBJECT_MAP_ONLY),
            parentPOA.create_implicit_activation_policy(ImplicitActivationPolicyValue.IMPLICIT_ACTIVATION)
        };

        final POA poa = parentPOA.create_POA("GamePOA", parentPOA.the_POAManager(), filePoaPolicies);

        return poa;
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
    public void release(GameServant servant, List<PlayerData> players)
    {
        synchronized (this)
        {
            for (PlayerData p : players)
            {
                this.players.get(p.info.id).idle = true;
            }
        }

        servant._poa().destroy(true, false);
    }
}
