package com.jdyna.emulator;

import static org.junit.Assert.assertTrue;

import org.jdyna.*;
import org.junit.Before;
import org.junit.Test;

import com.jdyna.DynaStarter;
import com.jdyna.GameParameters;
import com.jdyna.emulator.SmartBomber;
import com.jdyna.emulator.Victim;
import com.jdyna.pathfinder.Utils;

public class PathfinderTest {
	private Player player1;
	private Player player2;

	@Before
	public void setUp() {
		player1 = new Victim("victim").getPlayer();
		player2 = new SmartBomber("bomber").getPlayer();
	}

	@Test
	public void test1() {
		final GameParameters params = new GameParameters("AStarTest.conf", "astar1", Globals.DEFAULT_FRAME_RATE * 10,
				player1, player2);
		final GameResult result = new DynaStarter(params).run();
		assertTrue(Utils.playerWon(result, player2));
	}

	@Test
	public void test2() {
		final GameParameters params = new GameParameters("AStarTest.conf", "astar2", Globals.DEFAULT_FRAME_RATE * 10,
				player1, player2);
		final GameResult result = new DynaStarter(params).run();
		assertTrue(Utils.playerWon(result, player2));
	}

	@Test
	public void test3() {
		final GameParameters params = new GameParameters("AStarTest.conf", "astar3", Globals.DEFAULT_FRAME_RATE * 9,
				player1, player2);
		final GameResult result = new DynaStarter(params).run();
		assertTrue(Utils.playerWon(result, player2));
	}

	@Test
	public void test4() {
		final GameParameters params = new GameParameters("AStarTest.conf", "astar4", Globals.DEFAULT_FRAME_RATE * 9,
				player1, player2);
		final GameResult result = new DynaStarter(params).run();
		assertTrue(Utils.playerWon(result, player2));
	}

	@Test
	public void test5() {
		final GameParameters params = new GameParameters("AStarTest.conf", "astar5", Globals.DEFAULT_FRAME_RATE * 12,
				player1, player2);
		final GameResult result = new DynaStarter(params).run();
		assertTrue(Utils.playerWon(result, player2));
	}

	@Test
	public void test6() {
		final GameParameters params = new GameParameters("AStarTest.conf", "astar6", Globals.DEFAULT_FRAME_RATE * 12,
				player1, player2);
		final GameResult result = new DynaStarter(params).run();
		assertTrue(Utils.playerWon(result, player2));
	}

	@Test
	public void test7() {
		final GameParameters params = new GameParameters("AStarTest.conf", "astar7", Globals.DEFAULT_FRAME_RATE * 10,
				player1, player2);
		final GameResult result = new DynaStarter(params).run();
		assertTrue(Utils.playerWon(result, player2));
	}

	@Test
	public void test8() {
		final GameParameters params = new GameParameters("AStarTest.conf", "astar8", Globals.DEFAULT_FRAME_RATE * 9,
				player1, player2);
		final GameResult result = new DynaStarter(params).run();
		assertTrue(Utils.playerWon(result, player2));
	}

	@Test
	public void test9() {
		final GameParameters params = new GameParameters("AStarTest.conf", "astar9", Globals.DEFAULT_FRAME_RATE * 12,
				player1, player2);
		final GameResult result = new DynaStarter(params).run();
		assertTrue(Utils.playerWon(result, player2));
	}

	@Test
	public void test10() {
		final GameParameters params = new GameParameters("AStarTest.conf", "astar10", Globals.DEFAULT_FRAME_RATE * 10,
				player1, player2);
		final GameResult result = new DynaStarter(params).run();
		assertTrue(Utils.playerWon(result, player2));
	}

	@Test
	public void test11() {
		final GameParameters params = new GameParameters("AStarTest.conf", "astar11", Globals.DEFAULT_FRAME_RATE * 10,
				player1, player2);
		final GameResult result = new DynaStarter(params).run();
		assertTrue(Utils.playerWon(result, player2));
	}

	@Test
	public void test12() {
		final GameParameters params = new GameParameters("AStarTest.conf", "astar12", Globals.DEFAULT_FRAME_RATE * 6,
				player1, player2);
		final GameResult result = new DynaStarter(params).run();
		assertTrue(Utils.playerWon(result, player2));
	}

	@Test
	public void testTunnel() {
		final GameParameters params = new GameParameters("AStarTest.conf", "tunnel", Globals.DEFAULT_FRAME_RATE * 7,
				player1, player2);
		final GameResult result = new DynaStarter(params).run();
		assertTrue(Utils.playerWon(result, player2));
	}

	@Test
	public void testBonus() {
		final GameParameters params = new GameParameters("AStarTest.conf", "bonus", Globals.DEFAULT_FRAME_RATE * 7,
				player1, player2);
		final GameResult result = new DynaStarter(params).run();
		assertTrue(Utils.playerWon(result, player2));
	}

	@Test
	public void testMaze() {
		final GameParameters params = new GameParameters("AStarTest.conf", "maze", Globals.DEFAULT_FRAME_RATE * 16,
				player1, player2);
		final GameResult result = new DynaStarter(params).run();
		assertTrue(Utils.playerWon(result, player2));
	}

}
