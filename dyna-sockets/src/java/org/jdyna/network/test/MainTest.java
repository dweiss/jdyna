package org.jdyna.network.test;

import org.jdyna.network.sockets.GameServer;
import org.slf4j.LoggerFactory;

import com.dawidweiss.dyna.players.HumanPlayerFactory;
import com.dawidweiss.dyna.players.RabbitFactory;

/**
 * Set up a server and two clients locally.
 */
public class MainTest
{
    public static void main(final String [] args) throws Exception
    {
        LoggerFactory.getLogger(MainTest.class).debug("Discovering servers...");

        /*
         * Start the game server. 
         */
        new Thread()
        {
            public void run()
            {
                GameServer.main(new String [] {});
            };
        }.start();

        /*
         * Start a human client.
         */
        new Thread()
        {
            public void run()
            {
                PlayerFactoryClient.main(new String [] {
                    "--game", "testgame",
                    "--player-name", "player1",
                    "--board", "classic",
                    "--no-sound",
                    HumanPlayerFactory.class.getName()
                });
            };
        }.start();

        /*
         * Start a rabbit.
         */
        new Thread()
        {
            public void run()
            {
                System.setProperty("rabbit.slowdown", "250");

                PlayerFactoryClient.main(new String [] {
                    "--game", "testgame",
                    "--player-name", "rabbit",
                    "--no-sound",
                    RabbitFactory.class.getName()
                });
            };
        }.start();        
    }
}
