package org.jdyna.players.tyson.emulator.gamestate.bombs;

import java.util.Map.Entry;

import org.jdyna.Globals;

import org.jdyna.players.tyson.emulator.gamestate.Board;
import org.jdyna.players.tyson.emulator.gamestate.ExtendedPlayer;
import org.jdyna.players.tyson.emulator.gamestate.GridCoord;
import org.jdyna.players.tyson.emulator.gamestate.IPlayersInformationListener;
import org.jdyna.players.tyson.emulator.gamestate.bombs.BombState.BombStatus;

/**
 * <p>
 * Makes possible simulation of bombs. Useful for improvement player intelligence while
 * avoiding opponents.
 * </p>
 * 
 * @author Michał Kozłowski
 */
public abstract class SimulatedBombs extends Bombs implements
    IPlayersInformationListener, IBombsListener
{

    public SimulatedBombs(Board board)
    {
        super(board);
    }

    @Override
    public void update(final ExtendedPlayer exPl)
    {
        bombs.put(exPl.getCell(), new BombState(Globals.DEFAULT_FUSE_FRAMES, exPl
            .getRange(), BombStatus.READY));
    }

    @Override
    public void update(final Bombs src)
    {
        bombs.clear();
        for (Entry<GridCoord, BombState> b : src.bombs.entrySet())
        {
            bombs.put(b.getKey(), new BombState(b.getValue()));
        }
    }
}
