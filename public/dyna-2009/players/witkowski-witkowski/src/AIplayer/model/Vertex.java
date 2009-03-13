package AIplayer.model;

import java.awt.Point;

import org.jdyna.IPlayerController.Direction;


/**
 * Representation of vertex on BFS available fields queue.
 * @author Marcin Witkowski
 *
 */
public class Vertex implements Comparable<Vertex>{

	/* link to lat vertex */ 
	private Vertex last;
	/* current point */ 
	private Point coordinates;
	/* time on BFS search */ 
	private int time;
	/* last chosen direction */ 
	private Direction moveDirection;

	public Vertex(Vertex last, Point coordinates, int time, Direction move) {
		this.last = last;
		this.coordinates = coordinates;
		this.time = time;		
		this.moveDirection = move;
	}
	
	public int getTime() {
		return time;
	}
	
	public Vertex getLast() {
		return last;
	}
	
	public Direction getMoveDirection() {
		return moveDirection;
	}
	
	public Point getCoordinates() {
		return coordinates;
	}
	
	public void setCoordinates(Point coordinates) {
		this.coordinates = coordinates;
	}

	/**
	 * Compare due to time of achievements this point or point coordinations
	 */
	public int compareTo(Vertex o) {
		if (this.time == o.time) { 
			return(this.coordinates.x+this.coordinates.y*14*16 - (o.coordinates.x + o.coordinates.y*14*16)); 
		}
		return (this.time - o.time) ;
	}
	
}
