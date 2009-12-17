package org.jdyna.view.jme;

import java.awt.Point;

import org.jdyna.CellType;
import org.jdyna.view.jme.resources.DynaBomb;
import org.jdyna.view.jme.resources.DynaBonus;
import org.jdyna.view.jme.resources.DynaCrate;
import org.jdyna.view.jme.resources.DynaFloor;
import org.jdyna.view.jme.resources.DynaObject;
import org.jdyna.view.jme.resources.DynaWall;

public class DynaUtils
{
    public static BoardData createBoard(CellType [][] cells)
    {
        BoardData data = new BoardData();

        int width = cells.length;
        int height = cells[0].length;

        for (int i = 0; i < width; i++)
        {
            for (int j = 0; j < height; j++)
            {
                Point pos = new Point(i, j);
                CellType cell = cells[i][j];
                if (cell == null) continue;
                DynaObject obj;

                switch (cell)
                {
                    case CELL_WALL:
                        obj = new DynaWall(i, j);
                        break;
                    case CELL_EMPTY:
                        obj = new DynaFloor(i, j);
                        break;
                    case CELL_CRATE:
                        obj = new DynaCrate(i, j);
                        data.crates.put(pos, (DynaCrate) obj);
                        break;
                    case CELL_BOMB:
                        obj = new DynaBomb(i, j);
                        data.bombs.put(pos, (DynaBomb) obj);
                        break;
                    case CELL_BONUS_BOMB:
                    case CELL_BONUS_RANGE:
                    case CELL_BONUS_NO_BOMBS:
                    case CELL_BONUS_IMMORTALITY:
                    case CELL_BONUS_CRATE_WALKING:
                    case CELL_BONUS_CONTROLLER_REVERSE:
                    case CELL_BONUS_AHMED:
                    case CELL_BONUS_MAXRANGE: 
                    case CELL_BONUS_SPEED_UP: 
                    case CELL_BONUS_DIARRHEA:
                    case CELL_BONUS_SLOW_DOWN:
                    case CELL_BONUS_BOMB_WALKING:
                    case CELL_BONUS_SURPRISE:
                        obj = new DynaBonus(i, j, cell);
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown cell type");
                }

                if (cell != CellType.CELL_WALL && cell != CellType.CELL_EMPTY)
                {
                    data.boardNode.attachChild(new DynaFloor(i, j));
                }
                data.boardNode.attachChild(obj);
            }
        }
        return data;
    }
}
