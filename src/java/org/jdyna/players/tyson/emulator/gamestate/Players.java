package org.jdyna.players.tyson.emulator.gamestate;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.jdyna.Globals;
import org.jdyna.IPlayerSprite;
import org.jdyna.players.tyson.emulator.gamestate.ExtendedCell.TypeChangedEvent;
import org.jdyna.players.tyson.emulator.gamestate.bombs.AllSimulatedBombs;
import org.jdyna.players.tyson.emulator.gamestate.bombs.OpponentsSimulatedBombs;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * <p>
 * Manages information about {@link ExtendedPlayer} objects.
 * </p>
 * 
 * @author Michał Kozłowski
 */
final class Players
{
    private final Map<String, ExtendedPlayer> players = Maps.newHashMap();
    private final List<GridCoord> opponentsGrids = Lists.newLinkedList();
    private final String currentPlayer;
    private final List<IPlayersInformationListener> playersPositionsListeners = Lists
        .newArrayList();
    private final List<IPlayersInformationListener> rangesListeners = Lists
        .newArrayList();
    private GridCoord playerGrid;
    private Globals conf;

    /**
     * @param playersSrc Source of information about players.
     * @param currentPlayer Name of current player.
     */
    public Players(Globals conf, final List<? extends IPlayerSprite> playersSrc,
        final String currentPlayer)
    {
        this.currentPlayer = currentPlayer;
        this.conf = conf;
        for (IPlayerSprite p : playersSrc)
        {
            final ExtendedPlayer exPl = new ExtendedPlayer(conf, p);
            players.put(p.getName(), exPl);
            final GridCoord grid = exPl.getCell();
            if (!exPl.getName().equals(currentPlayer))
            {
                opponentsGrids.add(grid);
            }
            else
            {
                playerGrid = grid;
            }
        }
    }

    /**
     * @param l Listener to receive information about changes of players positions.
     */
    public void addPlayersPositionsListener(final IPlayersInformationListener l)
    {
        if (!playersPositionsListeners.contains(l))
        {
            playersPositionsListeners.add(l);
        }
    }

    /**
     * @param l Listener to receive information about changes of players ranges.
     */
    public void addRangesListener(final IPlayersInformationListener l)
    {
        if (!rangesListeners.contains(l))
        {
            rangesListeners.add(l);
        }
    }

    public GridCoord getPlayerCell(String playerName)
    {
        final ExtendedPlayer player = players.get(playerName);
        if (player == null)
        {
            return null;
        }
        return player.getCell();
    }

    /**
     * @return Range of current player.
     */
    public int getRange()
    {
        if (players.get(currentPlayer) == null)
        {
            return 0;
        }
        else
        {
            return players.get(currentPlayer).getRange();
        }
    }

    /**
     * @param gs State of game.
     * @param playersSrc Source of information about players.
     */
    public void update(final GameState gs, final List<? extends IPlayerSprite> playersSrc)
    {
        opponentsGrids.clear();
        for (IPlayerSprite playerSprite : playersSrc)
        {
            if (playerSprite.isDead())
            {
                players.remove(playerSprite.getName());
            }
            else
            {
                ExtendedPlayer pl = players.get(playerSprite.getName());
                if (pl == null)
                {
                    pl = new ExtendedPlayer(conf, playerSprite);
                    players.put(playerSprite.getName(), pl);
                }
                // update players information
                pl.update(playerSprite);
                updatePlayersPositionsListeners(pl);

                // update bonuses and bombs
                updateTypeChaged(gs.getBoard(), pl);
                // update player's memory about set bombs
                pl.updateBombs(gs.getBombs().getReadyBombsWithTimers());
                // update players grids
                final GridCoord grid = pl.getCell();
                if (!pl.getName().equals(currentPlayer))
                {
                    opponentsGrids.add(grid);
                }
                else
                {
                    playerGrid = grid;
                }
            }
        }
    }

    boolean amIAlive()
    {
        return players.containsKey(currentPlayer);
    }

    Collection<ExtendedPlayer> getOpponents()
    {
        final List<ExtendedPlayer> opponents = Lists.newArrayList(players.values());
        for (int i = 0; i < opponents.size(); i++)
        {
            if (opponents.get(i).getName().equals(currentPlayer))
            {
                opponents.remove(i);
                break;
            }
        }
        return opponents;
    }

    List<GridCoord> getOpponentsCells()
    {
        return opponentsGrids;
    }

    GridCoord getMyCell()
    {
        return playerGrid;
    }

    PointCoord getPlayerPosition()
    {
        return players.get(currentPlayer).getPosition();
    }

    int getPlayerRange(final String name)
    {
        return players.get(name).getRange();
    }

    Collection<ExtendedPlayer> getPlayers()
    {
        return players.values();
    }

    boolean isPlayerAlive(final String name)
    {
        return players.containsKey(name);
    }

    private void updatePlayersPositionsListeners(final ExtendedPlayer exPl)
    {
        for (IPlayersInformationListener l : playersPositionsListeners)
        {
            if (l instanceof OpponentsSimulatedBombs)
            {
                if (!exPl.getName().equals(currentPlayer))
                {
                    l.update(exPl);
                }
            }
            else if (l instanceof AllSimulatedBombs)
            {
                l.update(exPl);
            }
        }
    }

    private void updateRangesPositionsListeners(final ExtendedPlayer exPl)
    {
        for (IPlayersInformationListener l : rangesListeners)
        {
            l.update(exPl);
        }
    }

    private void updateTypeChaged(final Board board, final ExtendedPlayer pl)
    {
        final ExtendedCell exCell = board.cellAt(pl.getCell());
        final TypeChangedEvent typeChanged = exCell.getTypeChanged();
        switch (typeChanged)
        {
            case BOMB_BONUS_TAKEN:
                pl.incBombsCount();
                break;
            case RANGE_BONUS_TAKEN:
                pl.incRange();
                break;
            case BOMB_SET:
                updateRangesPositionsListeners(pl);
                pl.addBomb();
                break;
        }
    }

}
