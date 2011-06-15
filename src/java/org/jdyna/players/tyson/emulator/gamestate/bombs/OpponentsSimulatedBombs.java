package org.jdyna.players.tyson.emulator.gamestate.bombs;

import org.jdyna.GameConfiguration;
import org.jdyna.players.tyson.emulator.gamestate.Board;

/**
 * Extension to simulate bombs on all players positions.
 * 
 * @author Michał Kozłowski
 */
public class OpponentsSimulatedBombs extends SimulatedBombs
{
    public OpponentsSimulatedBombs(Board board, GameConfiguration conf)
    {
        super(board, conf);
    }
}
