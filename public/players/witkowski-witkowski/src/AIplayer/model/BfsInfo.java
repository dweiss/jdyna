package AIplayer.model;

import java.util.List;

import org.jdyna.IPlayerController.Direction;


/**
 * Store information about BFS searching for closest bonuses, enemies or bricks
 * 
 * @author Marcin Witkowski
 * 
 */
public class BfsInfo {

	/* Whether to search for bonus */
	private boolean bonus;
	/* Whether to search for brick */
	private boolean brick;
	/* Whether to search for enemy */
	private boolean enemy;
	/* Whether to put a bomb */
	private boolean bomb;
	/* Inform whether BFS runs more than time of maximal bomb explosion */
	private boolean checkAvoid;

	/* Path to the closest bonus */
	List<Direction> bonusPath;
	/* Path to the closest brick */
	List<Direction> brickPath;
	/* Path to the closest enemy */
	List<Direction> enemyPath;

	public BfsInfo() {
		bonus = false;
		brick = false;
		bomb = false;
		enemy = false;
		this.checkAvoid = false;
		bonusPath = null;
		enemyPath = null;
		brickPath = null;
	}

	public void setBonusPath(List<Direction> bonusPath) {
		this.bonusPath = bonusPath;
	}

	public void setBrickPath(List<Direction> brickPath) {
		this.brickPath = brickPath;
	}

	public List<Direction> getBonusPath() {
		return this.bonusPath;
	}

	public List<Direction> getBrickPath() {
		return this.brickPath;
	}

	public void setBonus(boolean bonus) {
		this.bonus = bonus;
	}

	public void setBomb(boolean bomb) {
		this.bomb = bomb;
	}

	public void setBrick(boolean brick) {
		this.brick = brick;
	}

	public void setCheckAvoid(boolean checkAvoid) {
		this.checkAvoid = checkAvoid;
	}

	public boolean isBonus() {
		return bonus;
	}

	public boolean isEnemy() {
		return enemy;
	}

	public boolean isBrick() {
		return brick;
	}

	public boolean isCheckAvoid() {
		return checkAvoid;
	}

	public boolean isBomb() {
		return bomb;
	}

	public void setEnemy(boolean enemy) {
		this.enemy = enemy;
	}

	public void setEnemyPath(List<Direction> path) {
		this.enemyPath = path;
	}

	public List<Direction> getEnemyPath() {
		return enemyPath;
	}
}
