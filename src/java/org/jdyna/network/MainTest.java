package org.jdyna.network;

import org.jdyna.network.sockets.BotClient;
import org.jdyna.network.sockets.GameServer;
import org.jdyna.players.rabbit.RabbitFactory;
import org.slf4j.LoggerFactory;


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
                BotClient.main(new String [] {
                    "--game", "testgame",
                    "--player-name", "player1",
                    "--board", "bigclassic",
                    "--no-sound",
                    TestPlayerFactory.class.getName()
                });
            };
        }.start();

        /*
         * Start a human client.
         */
        new Thread()
        {
            public void run()
            {
                BotClient.main(new String [] {
                    "--game", "testgame",
                    "--player-name", "player2",
                    "--no-sound", "--no-view",
                    TestPlayerFactory.class.getName()
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
                // System.setProperty("rabbit.slowdown", "250");

                BotClient.main(new String [] {
                    "--game", "testgame",
                    "--player-name", "rabbit",
                    "--no-sound", "--no-view",
                    RabbitFactory.class.getName()
                });
            };
        }.start();        
    }
}
