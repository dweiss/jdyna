package com.dawidweiss.dyna.corba;

import java.util.ArrayList;

import org.jdyna.*;
import org.jdyna.players.HumanPlayerFactory;
import org.jdyna.players.RabbitFactory;
import org.omg.CORBA.ORB;
import org.omg.CORBA.UserException;
import org.omg.PortableServer.POA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dawidweiss.dyna.corba.bindings.*;
import com.google.common.collect.BiMap;
import com.google.common.collect.Lists;

/**
 * A class for running repeated games between a set of players, recording each game, its
 * result and statistics. <b>All players remain inside the same JVM, to reduce network
 * latencies.</b>
 */
public final class TournamentRunner
{
    private final static Logger logger = LoggerFactory.getLogger(TournamentRunner.class);

    /** */
    private ICGameServer gameServer;

    /** */
    private ORB orb;

    /** */
    private POA playersPOA;

    /*
     * 
     */
    public void init() throws Exception
    {
        /*
         * ORB setup. Any host interface, any port.
         */
        final ORB orb = CorbaUtils.initORB(null, 0);
        final POA rootPOA = CorbaUtils.rootPOA(orb);

        /*
         * Create game manager, save its reference.
         */
        final ICGameServer gameServer = ICGameServerHelper.narrow(rootPOA
            .servant_to_reference(new GameServerServant()));

        this.gameServer = gameServer;
        this.orb = orb;
        this.playersPOA = rootPOA;

        /*
         * Start the ORB and run it in an infinite loop.
         */
        new Thread()
        {
            public void run()
            {
                logger.info("Game server started.");
                orb.run();
            }
        }.start();
    }
    
    /**
     * Starts the tournament with local players, wrapped to Corba using
     * {@link ICPlayerControllerAdapter}.
     */
    public void run(int rounds, int maxFramesPerRound, IPlayerFactory... playerFactories)
    {
        final ICPlayerFactory [] wrapped = new ICPlayerFactory [playerFactories.length];
        for (int i = 0; i < wrapped.length; i++)
        {
            wrapped[i] = new CPlayerFactoryAdapter(playerFactories[i]);
        }

        run(rounds, maxFramesPerRound, wrapped);
    }

    /**
     * Starts the tournament with Corba players.
     */
    public void run(int rounds, int maxFramesPerRound, ICPlayerFactory... playerFactories)
    {
        if (playerFactories.length < 2) throw new IllegalArgumentException(
            "At least two players required.");

        final ArrayList<ICPlayerController> adapters = Lists.newArrayList();
        final ArrayList<CPlayer> cplayers = Lists.newArrayList();

        try
        {
            int index = 0;
            for (ICPlayerFactory f : playerFactories)
            {
                final String playerName = f.getDefaultPlayerName() + "-" + index++; 
                final ICPlayerController playerAdapter = f.getController(playerName, playersPOA);

                cplayers.add(gameServer.register(playerName, playerAdapter));
            }

            final GameEventsProxy proxy = new GameEventsProxy();
            proxy.add(new LocalGameView());

            final ICGameListener view = ICGameListenerHelper.narrow(playersPOA
                .servant_to_reference(proxy));

            final BiMap<Integer, String> playerMapping = Adapters.create(cplayers);

            try
            {
                for (int round = 0; round < rounds; round++)
                {
                    logger.info("Round start: " + round);

                    final ICGame game = gameServer.create(0, cplayers
                        .toArray(new CPlayer [cplayers.size()]));
                    game.add(view);

                    final GameResult result = Adapters.adapt(playerMapping, game
                        .run(maxFramesPerRound));

                    logger.info("Round result: " + round + "\n" + result.toString());
                }
            }
            finally
            {
                // Remove players from the game server
                for (CPlayer player : cplayers)
                {
                    gameServer.unregister(player.name);
                }

                // Deallocate view.
                playersPOA.deactivate_object(playersPOA.reference_to_id(view));

                // Deallocate player adapters.
                for (ICPlayerController adapter : adapters)
                {
                    playersPOA.deactivate_object(playersPOA.reference_to_id(adapter));
                }
            }
        }
        catch (UserException e)
        {
            throw new RuntimeException(e);
        }
    }

    /*
     * 
     */
    public void cleanup()
    {
        this.orb.shutdown(true);
        logger.info("Game server stopped.");
    }

    /* Command-line entry point. */
    public static void main(String [] args) throws Exception
    {
        TournamentRunner runner = new TournamentRunner();
        runner.init();
        try
        {
            final int timeoutFrames = Globals.DEFAULT_FRAME_RATE * 3;
            final int rounds = 3;

            /*
             * Set up a competition between 2 rabbits and a human.
             */
            final RabbitFactory rf = new RabbitFactory();
            final HumanPlayerFactory human = new HumanPlayerFactory();

            runner.run(rounds, timeoutFrames, 
                human, rf, rf);
        }
        finally
        {
            runner.cleanup();
        }
    }
}