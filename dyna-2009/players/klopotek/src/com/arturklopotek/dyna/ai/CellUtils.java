package com.arturklopotek.dyna.ai;

import java.awt.Point;

import org.jdyna.*;


/** A utility class providing static methods performing some 
 *  useful board-evaluation tasks.
 */
public class CellUtils
{
    /**Checks weather the given position is 'safe', ie. if there is no danger that the cell
     * will blow up soon.  
     * @param cells cells data
     * @param bombs bombs data
     * @param pos the analyzed position
     * @return true if the cell at pos is safe
     */
    public static boolean isSafe(Cell[][] cells,BombInfo bombs[][],Point pos) {
        
        if (cells[pos.x][pos.y].type.isLethal())
            return false;
        
        boolean fire[][] = new boolean[cells.length][cells[0].length];
        //for (long[] row : fire)
          //  Arrays.fill(row, Long.MAX_VALUE);
        
        //for (int x=0;x<cells.length;x++)
            //fire[x] = new long[cells[0].length];

        for (int x=0;x<cells.length;x++) {
            for (int y=0;y<cells[0].length;y++) {
                
                if (CellType.CELL_BOMB.equals(cells[x][y].type)) {
                    
                    //FIXED ON THE LAB!
                    int bombRange = bombs[x][y] != null ? bombs[x][y].range : Globals.DEFAULT_BOMB_RANGE;

                    fire[x][y] = true;
                    for (int i=1;i<=bombRange;i++) {
                        if (!cells[x-i][y].type.isWalkable())
                            break;
                        //if (fire[x-i][y] == null || bombs[x-i][y].plantFrame > fire[x-i][y])
                            fire[x-i][y] = true;
                    }
                    for (int i=1;i<=bombRange;i++) {
                        if (!cells[x+i][y].type.isWalkable())
                            break;
                        //if (fire[x+i][y] == null || bombs[x+i][y].plantFrame > fire[x+i][y])
                            fire[x+i][y] = true;
                    }
                    for (int i=1;i<=bombRange;i++) {
                        if (!cells[x][y-i].type.isWalkable())
                            break;
                        //if (fire[x][y-i] == null || bombs[x][y-i].plantFrame > fire[x][y-i])
                            fire[x][y-i] = true;
                    }
                    for (int i=1;i<=bombRange;i++) {
                        if (!cells[x][y+i].type.isWalkable())
                            break;
                        //if (fire[x][y+i] == null || bombs[x][y+i].plantFrame > fire[x][y+i])
                            fire[x][y+i] = true;
                    }
                }
            }
        }
        return !fire[pos.x][pos.y];
    }
    
    /**Checks weather the two given points are in-sight. The two points are in-sight 
     * if there is no obstacle between them and they are no farther than the given distance. 
     * @param cells cell data
     * @param src the first point
     * @param dst the second point
     * @param dist the maximum distance
     * @return true if the points are in-sight
     */
    public static boolean isVisible(Cell[][] cells,Point src,Point dst,int dist) {
        
        if (src.x == dst.x) { //check vertical visibility
            int min,max;
            if (src.y < dst.y) {
                min = src.y;
                max = dst.y;
            } else {
                max = src.y;
                min = dst.y;
            }
            for (int i=min+1;i<=max-1;i++) {
                if (!cells[src.x][i].type.isWalkable())
                    return false;
            }
            return max-min <= dist;
        }
        
        if (src.y == dst.y) { //check horizontal visibility
            int min,max;
            if (src.x < dst.x) {
                min = src.x;
                max = dst.x;
            } else {
                max = src.x;
                min = dst.x;
            }
            for (int i=min+1;i<=max-1;i++) {
                if (!cells[i][src.y].type.isWalkable())
                    return false;
            }
            return max-min <= dist;
        }
        
        return false; //neither horizontally nor vertically visible
    }
}
