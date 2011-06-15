package org.jdyna.players.tyson.emulator.gamestate.bombs;

import org.jdyna.GameConfiguration;
import org.jdyna.players.tyson.emulator.gamestate.Board;

/**
 * Extension to simulate bombs on players positions.
 * 
 * @author Michał Kozłowski
 */
public class AllSimulatedBombs extends SimulatedBombs
{
    public AllSimulatedBombs(Board board, GameConfiguration conf)
    {
        super(board, conf);
    }
}
