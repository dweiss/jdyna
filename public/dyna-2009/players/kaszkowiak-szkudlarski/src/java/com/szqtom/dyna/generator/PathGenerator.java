package com.szqtom.dyna.generator;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

import org.jdyna.Cell;
import org.jdyna.CellType;
import org.jdyna.IPlayerController.Direction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.szqtom.dyna.radar.BombsDetector;
import com.szqtom.dyna.generator.Point;

public class PathGenerator {
	
	private LinkedList<Point> path;
	
	private boolean first;
	
	private Direction direction;
	
	private Point currentPoint;
	
	private BombsDetector detector;
	
	private Point[][] available_direction;
	
	private static Logger log = LoggerFactory.getLogger(PathGenerator.class);
	
	public PathGenerator(BombsDetector Detector){
		detector = Detector;
	}

	/* 
	 * Returns size of shortest path between Points and sets direction, if path not exists return -1
	 */
	public int generatePath(Cell[][] cell, Point from, Point to, int actualFrame){
		
		path = new LinkedList<Point>();
		first = true;
		direction = null;
		path.add(getPoint(cell, from, actualFrame));
		generateMove(cell, from, to, actualFrame);
		currentPoint = new Point(from.x, from.y);

		while(direction != null){
			if(direction.equals(Direction.LEFT)){
				currentPoint.x --;				
			} else if(direction.equals(Direction.RIGHT)){
				currentPoint.x++;
			} else if(direction.equals(Direction.UP)){
				currentPoint.y--;
			} else if(direction.equals(Direction.DOWN)){
				currentPoint.y++;
			}
			generateMove(cell,currentPoint, to, actualFrame);
		}
			
		if(path.size() > 1){
			int i =0;
			while(removeLoopFromPath()){
				i++;
			}
			direction = generateDirection(path.get(0), path.get(1));
			return path.size();
		}else {
			return -1;
		}
	}
	
	private void generateMove(Cell[][] cell, Point from, Point to, int actualFrame){
		if(first){
			available_direction = new Point[cell.length][cell[0].length];
			first = false;
		}
		Point from_point = null;
		
		if(available_direction[from.x][from.y] != null){
			from_point = available_direction[from.x][from.y];
		}else{
			from_point = getPoint(cell, from, actualFrame);
		}

		if(from.equals(to)){
			direction = null;
		}else{
			direction = getBestDirection(from_point, to);
			
			if(direction == null){
				//must return
				if(path.size()>1){
					
					//delete current point from path
					path.remove(path.size()-1);
					
					Point point = path.get(path.size()-1);
					
					if(from.x - point.x == 1){
						direction = Direction.LEFT;
					}else if(from.x - point.x == -1){
						direction = Direction.RIGHT;
					}else if (from.y - point.y == 1){
						direction = Direction.UP;
					}else if (from.y - point.y == -1){
						direction = Direction.DOWN;
					}
				}
				
			} else {
				Point next_point = null;
				if(direction.equals(Direction.LEFT)){
					from_point.setUsed_left_direction(true);
					available_direction[from.x][from.y] = from_point;
					next_point = new Point(from_point.x - 1, from_point.y);
				}else if(direction.equals(Direction.RIGHT)){
					from_point.setUsed_right_direction(true);
					available_direction[from.x][from.y] = from_point;
					next_point = new Point(from_point.x + 1, from_point.y);
				}else if(direction.equals(Direction.UP)){
					from_point.setUsed_up_direction(true);
					available_direction[from.x][from.y] = from_point;
					next_point = new Point(from_point.x, from_point.y - 1);
				}else if(direction.equals(Direction.DOWN)){
					from_point.setUsed_down_direction(true);
					available_direction[from.x][from.y] = from_point;
					next_point = new Point(from_point.x, from_point.y + 1);
				}
				path.add(next_point);
			}

		}
	}
	
	/*
	 * Returns Point object with available direction
	 */
	private com.szqtom.dyna.generator.Point getPoint(Cell[][] cell, Point point, int actualFrame){
		boolean left = false;
		boolean right = false;
		boolean up = false;
		boolean down = false;
		
		if(((cell[point.x -1][point.y].type.equals(CellType.CELL_EMPTY)
				||cell[point.x -1][point.y].type.equals(CellType.CELL_BONUS_RANGE)
				||cell[point.x -1][point.y].type.equals(CellType.CELL_BONUS_BOMB))
				&&!detector.willExplode(new Point(point.x-1,point.y), cell, path.size(), actualFrame))
				){
			left = true;
		}
		if(((cell[point.x +1][point.y].type.equals(CellType.CELL_EMPTY)
				||cell[point.x +1][point.y].type.equals(CellType.CELL_BONUS_RANGE)
				||cell[point.x +1][point.y].type.equals(CellType.CELL_BONUS_BOMB))
				&&!detector.willExplode(new Point(point.x+1,point.y), cell, path.size(), actualFrame))
				){
			right = true;
		}
		if(((cell[point.x][point.y -1].type.equals(CellType.CELL_EMPTY)
				||cell[point.x][point.y -1].type.equals(CellType.CELL_BONUS_RANGE)
				||cell[point.x][point.y -1].type.equals(CellType.CELL_BONUS_BOMB))
				&&!detector.willExplode(new Point(point.x,point.y-1), cell, path.size(), actualFrame))
				){
			up = true;
		}
		if(((cell[point.x][point.y +1].type.equals(CellType.CELL_EMPTY)
				||cell[point.x][point.y +1].type.equals(CellType.CELL_BONUS_RANGE)
				||cell[point.x][point.y +1].type.equals(CellType.CELL_BONUS_BOMB))
				&&!detector.willExplode(new Point(point.x,point.y+1), cell, path.size(), actualFrame))
				){
			down = true;
		}
		return new Point(point,left ,right, up, down);
	}
	
	

	private Direction getBestDirection(Point from, Point to){
		
		ArrayList<Direction> available_direction = new ArrayList<Direction>();
		Direction best_direction = null;
		
		if(from.isAvailable_left_direction() && !from.isUsed_left_direction()) {
			if(path.size() == 1){
				available_direction.add(Direction.LEFT);
			} else {
				if(from.x - path.get(path.size()-2).x != 1){
					available_direction.add(Direction.LEFT);
				}
			}
		}
		
		if(from.isAvailable_right_direction() && !from.isUsed_right_direction()){
			if(path.size() == 1){
				available_direction.add(Direction.RIGHT);
			} else {
				if(path.get(path.size()-2).x - from.x != 1){
					available_direction.add(Direction.RIGHT);
				}
			}
		}
		
		if(from.isAvailable_up_direction() && !from.isUsed_up_direction()){
			if(path.size() == 1){
				available_direction.add(Direction.UP);
			} else {
				if(from.y - path.get(path.size()-2).y != 1){
					available_direction.add(Direction.UP);
				}
			}
		}
		
		if(from.isAvailable_down_direction() && !from.isUsed_down_direction()){
			if(path.size() == 1){
				available_direction.add(Direction.DOWN);
				} else {
				if(path.get(path.size()-2).y - from.y != 1){
					available_direction.add(Direction.DOWN);
				}
			}
		}
		
		int max = Integer.MIN_VALUE;
		for (Direction direction : available_direction) {
			if(Direction.LEFT.equals(direction)){
				if(max < from.x - to.x){
					max = from.x - to.x;
					best_direction = Direction.LEFT;
				}
			} else if(Direction.RIGHT.equals(direction)){
				if(max < to.x - from.x){
					max = to.x - from.x;
					best_direction = Direction.RIGHT;
				}
			} else if(Direction.UP.equals(direction)){
				if(max < from.y - to.y){
					max = from.y - to.y;
					best_direction = Direction.UP;
				}
			} else if(Direction.DOWN.equals(direction)){
				if(max < to.y - from.y){
					max = to.y - from.y;
					best_direction = Direction.DOWN;
				}
			}
		}
		
		return best_direction;
	}
		

	public Direction getDirection() {
		return direction;
	}

	public LinkedList<Point> getPath() {
		return path;
	}

	private Direction generateDirection(Point from, Point to){
		
		if(from.x - to.x == 1){
			return Direction.LEFT;
		} else if(from.x - to.x == -1){
			return Direction.RIGHT;
		} else if(from.y - to.y == -1){
			return Direction.DOWN;
		} else if(from.y - to.y == 1){
			return Direction.UP;
		}
		return null;

	}
	
	public void makeMoveFromPath(){
		if(path.size() > 1){
			direction = generateDirection(path.get(0), path.get(1));
			//log.debug("move from " + path.get(0) + " to " + path.get(1) + " " + direction.toString());
			path.removeFirst();
		} else {
			direction = null;
		}
	}
	
	private boolean removeLoopFromPath(){
		int start = 0;
		boolean remove_left_neighbour = false;
		boolean remove_right_neighbour = false;
		boolean remove_up_neighbour = false;
		boolean remove_down_neighbour = false;
		boolean remove_this = false;
		
		Point curr_point;
		if(path.size()<2){
			return false;
		}

		for (int i = 0; i < path.size()-2; i++) {

			curr_point = path.get(i);
			
			//point is in path
			if(path.subList(path.indexOf(curr_point)+1, path.size()-1).indexOf(curr_point) != -1){
				start = path.indexOf(curr_point);
				remove_this = true;
				break;
			}
			
			if(path.subList(path.indexOf(curr_point)+2, path.size()-1).indexOf(new Point(curr_point.x -1, curr_point.y)) != -1){
				start = path.indexOf(curr_point);
				remove_left_neighbour = true;
				break;
			}
			
			if(path.subList(path.indexOf(curr_point)+2, path.size()-1).indexOf(new Point(curr_point.x +1, curr_point.y)) != -1){
				start = path.indexOf(curr_point);
				remove_right_neighbour = true;
				break;
			}
			
			if(path.subList(path.indexOf(curr_point)+2, path.size()-1).indexOf(new Point(curr_point.x, curr_point.y-1)) != -1){
				start = path.indexOf(curr_point);
				remove_up_neighbour = true;
				break;
			}
			
			if(path.subList(path.indexOf(curr_point)+2, path.size()-1).indexOf(new Point(curr_point.x, curr_point.y+1)) != -1){
				start = path.indexOf(curr_point);
				remove_down_neighbour = true;
				break;
			}

		}


		if(remove_this){
			while(!path.get(start).equals(path.get(start+1))){
				path.remove(start+1);
			}
			path.remove(start+1);
			return true;
		}
		
		if(remove_left_neighbour){
			path.remove(start +1);
			while(!(new Point(path.get(start).x -1, path.get(start).y)).equals(path.get(start+1))){
				path.remove(start+1);
			}
			return true;
		}
		
		if(remove_right_neighbour){
			path.remove(start +1);
			while(!(new Point(path.get(start).x +1, path.get(start).y)).equals(path.get(start+1))){
				path.remove(start+1);
			}
			return true;
		}
		
		if(remove_up_neighbour){
			path.remove(start +1);
			while(!(new Point(path.get(start).x, path.get(start).y-1)).equals(path.get(start+1))){
				path.remove(start+1);
			}
			return true;
		}
		
		if(remove_down_neighbour){
			path.remove(start +1);
			while(!(new Point(path.get(start).x, path.get(start).y + 1)).equals(path.get(start+1))){
				path.remove(start+1);
			}
			return true;
		}
		return false;
	}
	
	public void resetPath(){
		path = new LinkedList<Point>();
		first = true;
		direction = null;
	}
	
	public Point getBestTarget(Cell[][] cell, Point startPoint, int frame){
		for (int i= 0;  i < cell.length; i++) {
			for (int j = 0; j < cell[0].length; j++) {
				if(cell[i][j].type.equals(CellType.CELL_BONUS_RANGE) || cell[i][j].type.equals(CellType.CELL_BONUS_BOMB)){
					if(generatePath(cell, startPoint, new Point(i,j), frame)!=-1){
						return new Point(i,j);
					}
				}
			}
		}
		return getRandomTarget(cell, startPoint, frame);
	}
	
	private Point getRandomTarget(Cell[][] cell, Point startPoint, int frame){
		
		Random rand = new Random();
		Point point = new Point(rand.nextInt(cell.length),rand.nextInt(cell.length));
		int i = 0;
		while(generatePath(cell, startPoint, point, frame)==-1){
			point = new Point(rand.nextInt(cell.length),rand.nextInt(cell.length));
			i++;
			if(i>cell.length*cell.length*10){
				point = startPoint;
				break;
			}
		}	
		return point;
	}
	
}