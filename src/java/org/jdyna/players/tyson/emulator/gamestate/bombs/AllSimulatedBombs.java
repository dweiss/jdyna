package org.jdyna.players.tyson.emulator.gamestate.bombs;

import org.jdyna.Globals;
import org.jdyna.players.tyson.emulator.gamestate.Board;

/**
 * Extension to simulate bombs on players positions.
 * 
 * @author Michał Kozłowski
 */
public class AllSimulatedBombs extends SimulatedBombs
{
    public AllSimulatedBombs(Board board, Globals conf)
    {
        super(board, conf);
    }
}
