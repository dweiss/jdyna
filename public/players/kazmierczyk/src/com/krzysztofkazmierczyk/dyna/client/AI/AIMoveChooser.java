/**
 * This package contains all classes related to AI to choose what to do in the game of my virtual player
 */
package com.krzysztofkazmierczyk.dyna.client.AI;

import java.awt.Point;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jdyna.BoardInfo;

import com.dawidweiss.dyna.corba.bindings.CControllerState;
import com.dawidweiss.dyna.corba.bindings.CDirection;
import com.krzysztofkazmierczyk.dyna.GameStateEventUpdater;
import com.krzysztofkazmierczyk.dyna.client.game.Utilities;
import com.krzysztofkazmierczyk.dyna.client.game.wayFinder.WayCell;
import com.krzysztofkazmierczyk.dyna.client.game.wayFinder.WayFinder;

/**
 * This clazz implements choosing move by computer.
 * 
 * @author kazik
 */
public class AIMoveChooser implements MoveChooserInterface
{

    private final static Logger logger = Logger.getLogger("AIMoveChooser");

    /** For more info what this variable means, visit page: linux-lammers.com :) */
    private final int myId;

    // Last time of dropping bomb. Initial value is experimental :)
    private int maxBombDropFrame = -6;

    // Maximal distance from opponent when I want to attack him (put the bomb */
    private static final int DISTANCE_OF_ATTACK = 20;

    // Only lamer does not know what this constant means :)
    private static final int MIN_TIME_BETWEEN_BOMBS = 16;

    public AIMoveChooser(int myId)
    {
        this.myId = myId;
    }

    @Override
    public CControllerState move(GameStateEventUpdater event)
    {
        try
        {
            final BoardInfo boardInfo = event.getBoardInfo();
            Point myPoint = event.getPlayers().get(myId).getCell(boardInfo);
            WayCell [][] wayCells = WayFinder.getWaysTable(event, myPoint);
            List<Integer> [][] explosionFrames = Utilities.getExplosionFrames(event);

            Point targetCell = null;
            final Integer attackedPlayerNO = Utilities.getNearestPlayerNO(event,
                wayCells, myId);
            final Point attackedPlayerCell = attackedPlayerNO != null ? event
                .getPlayers().get(attackedPlayerNO).getCell(boardInfo) : null;
            final int attackedPlayerFrameNO = attackedPlayerCell != null ? wayCells[attackedPlayerCell.x][attackedPlayerCell.y]
                .getTimeOfArrive()
                : Integer.MAX_VALUE;

            final Point nearestBonusCell = Utilities.getNearestBonusPoint(event
                .getCells(), wayCells);

            if (Utilities.isPlayerSafe(event, explosionFrames, wayCells, myId))
            {
                if (nearestBonusCell != null)
                {
                    targetCell = nearestBonusCell;
                }
                else
                {
                    if (attackedPlayerNO != null)
                    {
                        targetCell = attackedPlayerCell;
                    }
                    else
                    {
                        targetCell = myPoint;// stay and do nothing
                    }

                }
                logger.log(Level.FINEST, "Safe. Walking from " + myPoint + " to "
                    + targetCell);
            }
            else
            {
                targetCell = Utilities.findNearestSafePlace(event, wayCells,
                    explosionFrames, myId);
                logger.log(Level.FINEST, "Not Safe. Walking from " + myPoint + " to "
                    + targetCell);
            }

            CDirection direction = CDirection.NONE;
            boolean dropBomb = false;

            final int frameNO = event.getFrameNO();
            final int timeDelta = frameNO - maxBombDropFrame;

            if ((timeDelta > MIN_TIME_BETWEEN_BOMBS)
                && ((attackedPlayerFrameNO - frameNO) < DISTANCE_OF_ATTACK)
                && Utilities.safeToDropBomb(event, myId))
            {
                maxBombDropFrame = frameNO;
                dropBomb = true;
            }

            if (wayCells[targetCell.x][targetCell.y].isReachable())
            {
                direction = WayFinder.getDirection(wayCells, myPoint, targetCell);
            }
            return new CControllerState(direction, dropBomb);
        }
        catch (Exception e)
        {
            /*
              logger.log(Level.WARNING, "Got exception. Not running any move. Exception:"
              e.getMessage());
             */
            e.printStackTrace(); // - Lets write part of lammers code :)
        }
        return new CControllerState(CDirection.NONE, false);
    }

}
