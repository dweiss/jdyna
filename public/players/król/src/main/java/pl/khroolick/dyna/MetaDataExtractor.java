package pl.khroolick.dyna;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.dawidweiss.dyna.BoardInfo;
import com.dawidweiss.dyna.Cell;
import com.dawidweiss.dyna.GameStateEvent;
import com.dawidweiss.dyna.Globals;
import com.dawidweiss.dyna.IPlayerSprite;

public class MetaDataExtractor
{
    private final Dimension gridSize;
    private final BoardInfo boardInfo;
    protected CellInfo [][] metaData;
    private int lastUpdateFrame;
    private String hydraName;
    private Hydra hydra;
    HashMap<String, PlayerInfo> opponents = new HashMap<String, PlayerInfo>();
    PlayerInfo hydraPlayer = new PlayerInfo();
    ArrayList<Point> bonuses = new ArrayList<Point>();
    Point desiredTarget = null;
    private KDirection [] directions = new KDirection [KDirection.values().length];

    public MetaDataExtractor(Hydra hydra, BoardInfo boardInfo)
    {
        this.hydraName = hydra.myName;
        this.hydra = hydra;
        this.boardInfo = boardInfo;
        this.gridSize = boardInfo.gridSize;
        metaData = new CellInfo [gridSize.width] [gridSize.height];
        for (int y = gridSize.height - 1; y >= 0; y--)
            for (int x = gridSize.width - 1; x >= 0; x--)
                metaData[x][y] = new CellInfo();
        lastUpdateFrame = 0;
    }

    private int updateFrame(int frame)
    {
        final int delta = frame - lastUpdateFrame;
        lastUpdateFrame = frame;
        return delta;
    }

    public void bombPlanted(int x, int y, int range)
    {
        final int timeToExplosion = Math.min(Globals.DEFAULT_FUSE_FRAMES, metaData[x][y]
            .getTimeToExplosion());

        final int xmin = Math.max(0, x - range);
        final int xmax = Math.min(gridSize.width - 1, x + range);
        final int ymin = Math.max(0, y - range);
        final int ymax = Math.min(gridSize.height - 1, y + range);

        metaData[x][y].setTimeToExplosion(timeToExplosion);
        for (KDirection direction : KDirection.values())
        {
            for (int dist = 1; dist <= range; dist++)
            {
                int xpos = x + dist * direction.getXSign();
                int ypos = y + dist * direction.getYSign();

                if (xpos > xmax || xpos < xmin || ypos > ymax || ypos < ymin)
                {
                    break;
                }
                if (!metaData[xpos][ypos].type.isWalkable())
                {
                    break;
                }
                metaData[xpos][ypos].setTimeToExplosion(timeToExplosion);
            }
        }

    }

    public CellInfo [][] updateMetaData(int frame, GameStateEvent gse)
    {
        Cell [][] cells = gse.getCells();
        boolean isChooseDirectionNeeded = false;

        //
        // check frame no
        //
        final int delta = updateFrame(frame);

        // remember hydra's position
        Point oldHydrasPos = hydraPlayer.boardPoss;

        //
        // update players
        //
        updatePlayersPossitions(gse.getPlayers());

        if (oldHydrasPos != hydraPlayer.boardPoss)
        {
            isChooseDirectionNeeded = true;
        }

        //
        // update time to explosion
        //
        for (int y = gridSize.height - 1; y >= 0; y--)
        {
            for (int x = gridSize.width - 1; x >= 0; x--)
            {
                metaData[x][y].updateTimeToExplosion(delta);
            }
        }

        //
        // check for events and update state appropriate
        //
        for (int y = gridSize.height - 1; y >= 0; y--)
        {
            for (int x = gridSize.width - 1; x >= 0; x--)
            {
                switch (metaData[x][y].updateState(cells[x][y].type))
                {
                    case BOMB_PLANTED:
                        bombPlanted(x, y, Globals.DEFAULT_BOMB_RANGE);
                        isChooseDirectionNeeded = true;
                        break;
                    case BONUS_APPEAR:
                        addBonusPos(new Point(x, y));
                        isChooseDirectionNeeded = true;
                        break;
                    case BONUS_DISAPPEAR:
                        removeBonusPos(new Point(x, y));
                        isChooseDirectionNeeded = true;
                        break;
                    case NONE:
                }
            }
        }

        if (isChooseDirectionNeeded)
        {
            chooseDirection();
        }

        return metaData;
    }

    /**
     * Determine where players are.
     */
    private void updatePlayersPossitions(List<? extends IPlayerSprite> players)
    {
        for (IPlayerSprite ps : players)
        {
            //
            // get the Player
            //
            String actualName = ps.getName();
            PlayerInfo actual = null;
            if (hydraName.equals(actualName))
            {
                actual = hydraPlayer;
            }
            else
            {
                actual = opponents.get(actualName);
                if (actual == null)
                {
                    actual = new PlayerInfo();
                    opponents.put(actualName, actual);
                }
            }
            //
            // update his data
            //
            actual.boardPoss = boardInfo.pixelToGrid(ps.getPosition());
            actual.pixelPoss = ps.getPosition();
            actual.isDead = ps.isDead();
            actual.isImmortal = ps.isImmortal();
        }
    }

    private void addBonusPos(Point bonusPoss)
    {
        bonuses.add(bonusPoss);
        if (desiredTarget == null)
        {
            // catch this bonus
            desiredTarget = bonusPoss;
        }
        else
        {
            // we are going for another bonus
            if (hydraPlayer.boardPoss.distanceSq(desiredTarget) > hydraPlayer.boardPoss
                .distanceSq(bonusPoss))
            {
                // if new one is nearest
                desiredTarget = bonusPoss;
            }
        }
    }

    private void removeBonusPos(Point bonusPoss)
    {
        bonuses.remove(bonusPoss);
        if (desiredTarget.equals(bonusPoss))
        {
            // shit! my bonus just disappear
            if (bonuses.size() > 0)
            {
                // pick up another bonus
                double minDistSq = Double.MAX_VALUE;
                double dist;
                for (Point p : bonuses)
                {
                    if (minDistSq > (dist = hydraPlayer.boardPoss.distanceSq(p)))
                    {
                        minDistSq = dist;
                        desiredTarget = p;
                    }
                }
            }
            else
            {
                // there are no other bonusses
                desiredTarget = null;
            }
        }
    }

    Point getDesiredTarget()
    {
        // if we are going to collect bonusses
        if (desiredTarget != null)
        {
            // go there as soon as it is possible
            return desiredTarget;
        }

        //
        // there are no bonuses at board
        // attack somebody who has the smallest range
        //
        int minRange = Integer.MAX_VALUE;
        PlayerInfo looser = null;
        for (PlayerInfo p : opponents.values())
        {
            if (!p.isDead)
            {
                if (minRange > p.range)
                {
                    minRange = p.range;
                    looser = p;
                }
                else if (minRange == p.range)
                {
                    // and if theirs range is the same, bomb count has matter
                    if (looser.bombs > p.bombs)
                    {
                        looser = p;
                    }
                }
            }
        }

        //
        // obtain target
        //
        if (looser == null)
        {
            // we can have no alive opponents
            // target => board center
            return new Point(boardInfo.gridSize.width / 2, boardInfo.gridSize.height / 2);
        }
        else
        {
            // target => looser ];-> //niach niach niach!
            return looser.boardPoss;
        }

    }

  
    void chooseDirection()
    {
        // think where you should go
        Point target = new Point(getDesiredTarget());
        Point offset = new Point(target);
  
        // obtain how to get there avoiding bombs
        Point myPoss = hydraPlayer.boardPoss;
        offset.x -= myPoss.x;
        offset.y -= myPoss.y;

        KDirection.getDesiredDirs(offset, directions);
        com.dawidweiss.dyna.IPlayerController.Direction currentDirection = null;

        int min = Integer.MAX_VALUE;
        for (KDirection direction : directions)
        {
            int actualDirDist = lookAround(1, myPoss, target, direction);
            if (min > actualDirDist)
            {
                min = actualDirDist;
                currentDirection = direction.getDirection();
            }
        }
        hydra.setCurentDirection(currentDirection);
    }

    private int lookAround(int range, Point poss, Point target, KDirection dir)
    {
        //check if cell is safe
        CellInfo cell = metaData[poss.x + dir.getXSign()][poss.y
        + dir.getYSign()];
        if (cell.getTimeToExplosion() < range*8 || !cell.type.isWalkable()){
            //cant get there!
            return Integer.MAX_VALUE;
        }

        poss.translate(dir.getXSign(), dir.getYSign());
        int _ret = 0;
        if (range++ >= 5)
        {
            _ret = (int) poss.distanceSq(target);
        }
        else
        {
            int minDst = Integer.MAX_VALUE;
            for (KDirection d : directions)
            {
                minDst = Math.min(minDst, lookAround(range, poss, target, d));
            }

            _ret = minDst;
        }
        poss.translate(-dir.getXSign(), -dir.getYSign());
        return _ret;
    }
}
