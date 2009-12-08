package org.jdyna.view.jme;

import java.awt.Point;

import org.jdyna.view.jme.adapter.AbstractGameAdapter.BonusType;
import org.jdyna.view.jme.adapter.AbstractGameAdapter.DynaCell;
import org.jdyna.view.resources.jme.DynaBomb;
import org.jdyna.view.resources.jme.DynaBonus;
import org.jdyna.view.resources.jme.DynaCrate;
import org.jdyna.view.resources.jme.DynaFloor;
import org.jdyna.view.resources.jme.DynaObject;
import org.jdyna.view.resources.jme.DynaWall;

public class DynaUtils {
	
	public static BoardData createBoard(DynaCell[][] cells)
    {
        BoardData data = new BoardData();
        
        int width = cells.length;
        int height = cells[0].length;
        
        for (int i = 0; i < width; i++)
        {
            for (int j = 0; j < height; j++)
            {
                Point pos = new Point(i, j);
                DynaCell cell = cells[i][j];
                if (cell==null)
                	continue;
                DynaObject obj;
                
                switch (cell) {
                    case WALL:
                        obj = new DynaWall(i, j);
                        break;
                    case EMPTY:
                        obj = new DynaFloor(i, j);
                        break;
                    case CRATE:
                        DynaCrate crate = new DynaCrate(i, j);
                        data.crates.put(pos, crate);
                        obj = crate;
                        break;
                    case BOMB:
                        DynaBomb bomb = new DynaBomb(i,j);
                        data.bombs.put(pos, bomb);
                        obj = bomb;
                        break;
                    case BONUS_BOMB:
                        obj = new DynaBonus(i, j,BonusType.EXTRA_BOMB);
                        break;
                    case BONUS_RANGE:
                        obj = new DynaBonus(i, j,BonusType.EXTRA_RANGE);
                        break;
                    case OTHER_CELL:
                    	obj = new DynaBonus(i, j, BonusType.OTHER_BONUS);
                    	break;
                    default:
                        throw new IllegalArgumentException("Unknown cell type");
                }
                
                if (cell != DynaCell.WALL && cell != DynaCell.EMPTY) {
                    data.boardNode.attachChild(new DynaFloor(i, j));
                }
                data.boardNode.attachChild(obj);
            }
        }
        
        return data;
    }
}
