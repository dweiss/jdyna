package com.jdyna.emulator;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.dawidweiss.dyna.GameResult;
import com.dawidweiss.dyna.Globals;
import com.dawidweiss.dyna.Player;
import com.jdyna.DynaStarter;
import com.jdyna.GameParameters;
import com.jdyna.emulator.SmartBomber;
import com.jdyna.emulator.Victim;
import com.jdyna.pathfinder.Utils;

public class BonusTest {
	private Player player1;
	private Player player2;

	@Before
	public void setUp() {
		player1 = new Victim("victim").getPlayer();
		player2 = new SmartBomber("bomber").getPlayer();
	}

	@Test
	public void testDetour() {
		final GameParameters params = new GameParameters("BonusTest.conf", "detour", Globals.DEFAULT_FRAME_RATE * 11,
				player1, player2);
		final GameResult result = new DynaStarter(params).run();
		assertTrue(Utils.playerWon(result, player2));
	}

	@Test
	public void testTreasureHunt() {
		final GameParameters params = new GameParameters("BonusTest.conf", "treasure_hunt",
				Globals.DEFAULT_FRAME_RATE * 8, player1, player2);
		final GameResult result = new DynaStarter(params).run();
		assertTrue(Utils.playerWon(result, player2));
	}

	@Test
	public void testTrap() {
		final GameParameters params = new GameParameters("BonusTest.conf", "trap", Globals.DEFAULT_FRAME_RATE * 9,
				player1, player2);
		final GameResult result = new DynaStarter(params).run();
		assertTrue(Utils.playerWon(result, player2));
	}

}
