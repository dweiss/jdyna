package two.bsr.dyna;

import java.awt.Point;

import org.jdyna.Globals;
import org.jdyna.IPlayerSprite;


class TZPlayerInfo {
	String name;
	Point position;
	IPlayerSprite playerSprite;
	int bombCount = Globals.DEFAULT_BOMB_COUNT;
	int bombRange = Globals.DEFAULT_BOMB_RANGE;

	TZPlayerInfo(String name, Point position) {
//		playerSprite = player;
		this.name = name;
		this.position = position;
	}

	public String toString() {
		return new StringBuilder().append(name).append(
				" :: bombCount: ").append(bombCount).append(" bombRange: ")
				.append(bombRange).toString();
	}
}

class TZBombInfo {
	Point position;
	int range;

	TZBombInfo(Point position, int range) {
		this.position = position;
		this.range = range;
	}
}
