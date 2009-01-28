package org.jdyna.network.test;

import org.jdyna.network.sockets.GameServer;
import org.jdyna.network.sockets.BotClient;
import org.slf4j.LoggerFactory;

import com.dawidweiss.dyna.players.HumanPlayerFactory;
import com.dawidweiss.dyna.players.RabbitFactory;

/**
 * Set up a server and two clients locally.
 */
public class MainTeamTest
{
    public static void main(final String [] args) throws Exception
    {
        LoggerFactory.getLogger(MainTeamTest.class).debug("Discovering servers...");

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
                    "--player-name", "humans:player1",
                    "--board", "bigclassic",
                    "--no-sound",
                    HumanPlayerFactory.class.getName()
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
                    "--player-name", "humans:player2",
                    "--no-sound", "--no-view",
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

                BotClient.main(new String [] {
                    "--game", "testgame",
                    "--player-name", "cpu:rabbit",
                    "--no-sound", "--no-view",
                    RabbitFactory.class.getName()
                });
            };
        }.start();        
    }
}
