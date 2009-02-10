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

public class VariousTests {
	private Player player1;
	private Player player2;
	private Player player3;
	private Player player4;

	@Before
	public void setUp() {
		player1 = new Victim("victim1").getPlayer();
		player2 = new SmartBomber("bomber").getPlayer();
		player3 = new Victim("victim2").getPlayer();
		player4 = new Victim("victim3").getPlayer();
	}

	@Test
	public void test1() {
		final GameParameters params = new GameParameters("VariousTests.conf", "1", Globals.DEFAULT_FRAME_RATE * 6, player1,
				player2, player3, player4);
		final GameResult result = new DynaStarter(params).run();
		assertTrue(Utils.playerWon(result, player2));
	}

	@Test
	public void test2() {
		final GameParameters params = new GameParameters("VariousTests.conf", "2", Globals.DEFAULT_FRAME_RATE * 7, player1,
				player2, player3, player4);
		final GameResult result = new DynaStarter(params).run();
		assertTrue(Utils.playerWon(result, player2));
	}

	@Test
	public void test3() {
		final GameParameters params = new GameParameters("VariousTests.conf", "3", Globals.DEFAULT_FRAME_RATE * 6, player1,
				player2, player3, player4);
		final GameResult result = new DynaStarter(params).run();
		assertTrue(Utils.playerWon(result, player2));
	}
	
}
