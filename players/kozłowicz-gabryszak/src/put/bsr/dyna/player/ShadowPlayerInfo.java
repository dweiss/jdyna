/**
 * 
 */
package put.bsr.dyna.player;

import java.awt.Point;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.dawidweiss.dyna.BoardInfo;
import com.dawidweiss.dyna.Cell;
import com.dawidweiss.dyna.Globals;
import com.dawidweiss.dyna.IPlayerSprite;

/**
 * 
 * Stores in-game information about Players.
 * 
 * @author marcin
 * 
 */
public class ShadowPlayerInfo {

	private final String name;
	private int bombRange = Globals.DEFAULT_BOMB_RANGE;
	private int bombCount = Globals.DEFAULT_BOMB_COUNT;
	private int lives = Globals.DEFAULT_LIVES;
	private boolean killed = false;
	private boolean immortal = false;
	private Point gridPosition;

	private static Map<String, ShadowPlayerInfo> playersInfo = new LinkedHashMap<String, ShadowPlayerInfo>();

	/**
	 * Update player information about all players.
	 * 
	 * @param players
	 *            List of all players.
	 * @param oldCells
	 *            Most previous board snapshot.
	 * @param bInfo
	 *            Provide borad information.
	 */
	public static void collectPlayersInfo(
			List<? extends IPlayerSprite> players, Cell[][] oldCells,
			BoardInfo bInfo) {
		for (IPlayerSprite sprite : players) {
			final ShadowPlayerInfo info = playersInfo.get(sprite.getName());
			if (info != null) {
				info.update(sprite, oldCells, bInfo);
			} else {
				ShadowPlayerInfo newInfo = new ShadowPlayerInfo(sprite);
				newInfo.update(sprite, oldCells, bInfo);
				playersInfo.put(sprite.getName(), newInfo);
			}
		}
	}

	public static ShadowPlayerInfo getInfoByName(final String name) {
		return playersInfo.get(name);
	}

	/**
	 * 
	 * @param gridPoint
	 *            Checked point.s
	 * @return Information aobut player standing on passed location. Null if
	 *         there is no player on the checked location.
	 */
	public static ShadowPlayerInfo getPlayerInfoByPosition(Point gridPoint) {
		for (ShadowPlayerInfo info : playersInfo.values()) {
			if (info.getGridPosition().equals(gridPoint)) {
				return info;
			}
		}
		return null;
	}

	protected ShadowPlayerInfo(final IPlayerSprite sprite) {
		this.name = sprite.getName();
	}

	public int getBombRange() {
		return bombRange;
	}

	public void setBombRange(int bombRange) {
		this.bombRange = bombRange;
	}

	public int getBombCount() {
		return bombCount;
	}

	public void setBombCount(int bombCount) {
		this.bombCount = bombCount;
	}

	public String getName() {
		return name;
	}

	/**
	 * Updates information about single player.
	 * 
	 * @param sprite
	 * @param oldCells
	 *            Most previous board snapshot.
	 * @param info
	 *            Information about board.
	 */
	protected void update(IPlayerSprite sprite, Cell[][] oldCells,
			BoardInfo info) {
		immortal = sprite.isImmortal();

		if (sprite.getPosition() != null) {
			// if player is on the board
			final Point newPos = info.pixelToGrid(sprite.getPosition());
			if (!immortal) {
				//check if player has collected bonus
				switch (oldCells[newPos.x][newPos.y].type) {
				case CELL_BONUS_BOMB:
					bombCount++;
					break;
				case CELL_BONUS_RANGE:
					bombRange++;
					break;
				}
			}
//			this.pixelPosition = newPos;
			this.gridPosition = newPos;
//			info.pixelToGrid(newPos);
		} else {
//			this.pixelPosition = null;
			this.gridPosition = null;
		}
		// if player new contained BONUS on previous frame
		// it means that that player has acquired the bonus
		// immortal players can't pick up a bonus

		if (!killed && sprite.isDead()) {
			kill();
		}
		killed = sprite.isDead();
	}

	private void kill() {
		lives--;
		bombCount = Globals.DEFAULT_BOMB_COUNT;
		bombRange = Globals.DEFAULT_BOMB_RANGE;
	}

	public int getLives() {
		return lives;
	}

//	public Point getPixelPosition() {
//		return pixelPosition;
//	}
	public Point getGridPosition() {
		return gridPosition;
	}

	public boolean isImmortal() {
		return immortal;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("Player " + name + "\n");
		builder.append("Lives " + lives + "\n");
		builder.append("BombCount " + bombCount + "\n");
		builder.append("BombRange " + bombRange + "\n");
		builder.append("Position " + gridPosition.toString());
		builder.append("Immortal " + immortal);
		builder.append("----------------------------");
		return builder.toString();
	}

	public static void debug() {
		for (ShadowPlayerInfo info : playersInfo.values()) {
			System.out.println(info);
		}
	}

}
