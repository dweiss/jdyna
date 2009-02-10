package ai.bot;

import java.awt.Point;

import com.dawidweiss.dyna.Globals;
import com.dawidweiss.dyna.IPlayerSprite;

/**
 * Stores player attributes. Copy-Pasted and adapted from original code because of too many 
 * <code>final</code> (class) modifiers and hidden methods/fields visibility.
 */
public class PlayerAttributes {
	/** player's name*/
	String name;
	
	/** maximum player's bomb range */
	int maxBombRange = Globals.DEFAULT_BOMB_RANGE;
	
	/** maximum player's bomb number */
	int maxBombNum = Globals.DEFAULT_BOMB_COUNT;
	
	/** player's current bomb count */
	int bombCounter = Globals.DEFAULT_BOMB_COUNT;
	
	boolean isDead;
	
	boolean isImmortal;
	
	/** 
	 * approximation of player's direction that is heading accumulated over
	 * some period of time
	 */
	Point directionApproximation;
	
	/** player's current position in pixel coordinates */
	Point position;
	
	/**
	 * Constructs player attributes
	 * @param player adapted <code>IPlayerSprite</code>
	 */
	PlayerAttributes(IPlayerSprite player) {
		this.position = player.getPosition();
		this.name = player.getName();
		this.isDead = player.isDead();
		this.isImmortal = player.isImmortal();
	}
}
