package com.jdyna.emulator;

import static org.junit.Assert.assertTrue;

import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;

import com.dawidweiss.dyna.GameResult;
import com.dawidweiss.dyna.Player;
import com.google.common.collect.Lists;
import com.jdyna.DynaStarter;
import com.jdyna.GameParameters;
import com.jdyna.emulator.ControlledBomber.Move;
import com.jdyna.pathfinder.Utils;

public class ControlledBomberTest {
	private Player player2;

	@Before
	public void setUp() {
		player2 = new SmartBomber("bomber").getPlayer();
	}

	@Test
	public void test() {
		final LinkedList<Move> plan = Lists.newLinkedList();
		plan.add(Move.LEFT);
		plan.add(Move.BOMB);
		plan.add(Move.RIGHT);
		plan.add(Move.RIGHT);
		plan.add(Move.RIGHT);
		plan.add(Move.RIGHT);
		plan.add(Move.RIGHT);
		plan.add(Move.RIGHT);
		plan.add(Move.RIGHT);
		plan.add(Move.RIGHT);
		plan.add(Move.RIGHT);
		plan.add(Move.RIGHT);
		plan.add(Move.DOWN);
		plan.add(Move.DOWN);
		plan.add(Move.LEFT);
		plan.add(Move.LEFT);
		plan.add(Move.LEFT);
		plan.add(Move.LEFT);
		plan.add(Move.LEFT);
		plan.add(Move.LEFT);
		plan.add(Move.LEFT);
		plan.add(Move.LEFT);
		plan.add(Move.LEFT);
		plan.add(Move.LEFT);
		final Player controlledPlayer = new ControlledBomber("controlled", plan).getPlayer();
		final GameParameters params = new GameParameters("VariousTests.conf", "timers", controlledPlayer, player2);
		final GameResult result = new DynaStarter(params).run();
		assertTrue(Utils.playerWon(result, player2));
	}

}
