package org.jdyna.players.tyson.emulator.gamestate.bombs;

import org.jdyna.Globals;
import org.jdyna.players.tyson.emulator.gamestate.Board;

/**
 * Extension to simulate bombs on all players positions.
 * 
 * @author Michał Kozłowski
 */
public class OpponentsSimulatedBombs extends SimulatedBombs
{
    public OpponentsSimulatedBombs(Board board, Globals conf)
    {
        super(board, conf);
    }
}
