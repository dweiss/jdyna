package com.jdyna;

import java.io.InputStreamReader;

import org.jdyna.*;

import com.google.common.collect.ImmutableList;

/**
 * Parameters to run the game with. This class is very useful in tests.
 * 
 * @author Michał Kozłowski
 * @author Bartosz Wesołowski
 */
public final class GameParameters {
	private final boolean graphicMode;
	private final Board board;
	private final ImmutableList<Player> players;
	private int framesLimit;

	public GameParameters(final boolean graphicMode, final String boardFile, final int framesLimit,
			final Player... players) {
		this.graphicMode = graphicMode;
		this.players = ImmutableList.of(players);
		this.board = readBoard(boardFile, 0);
		this.framesLimit = framesLimit;
	}

	public GameParameters(final boolean graphicMode, final String boardFile, final int boardIndex,
			final int framesLimit, final Player... players) {
		this.graphicMode = graphicMode;
		this.players = ImmutableList.of(players);
		this.board = readBoard(boardFile, boardIndex);
		this.framesLimit = framesLimit;
	}

	public GameParameters(final boolean graphicMode, final String boardFile, final String boardName,
			final int framesLimit, final Player... players) {
		this.graphicMode = graphicMode;
		this.players = ImmutableList.of(players);
		this.board = readBoard(boardFile, boardName);
		this.framesLimit = framesLimit;
	}

	public GameParameters(final String boardFile, final int framesLimit, final Player... players) {
		this.graphicMode = true;
		this.players = ImmutableList.of(players);
		this.board = readBoard(boardFile, 0);
		this.framesLimit = framesLimit;
	}

	public GameParameters(final String boardFile, final int boardIndex, final int framesLimit, final Player... players) {
		this.graphicMode = true;
		this.players = ImmutableList.of(players);
		this.board = readBoard(boardFile, boardIndex);
		this.framesLimit = framesLimit;
	}

	public GameParameters(final String boardFile, final String boardName, final int framesLimit,
			final Player... players) {
		this.graphicMode = true;
		this.players = ImmutableList.of(players);
		this.board = readBoard(boardFile, boardName);
		this.framesLimit = framesLimit;
	}

	public GameParameters(final String boardFile, final String boardName, final Player... players) {
		this.graphicMode = true;
		this.players = ImmutableList.of(players);
		this.board = readBoard(boardFile, boardName);
	}

	public boolean isGraphicMode() {
		return graphicMode;
	}

	public Player[] getPlayers() {
		return players.toArray(new Player[0]);
	}

	public Board getBoard() {
		return board;
	}

	public int getFramesLimit() {
		return framesLimit;
	}

	private Board readBoard(final String boardFile, final int boardIndex) {
		final ClassLoader cl = Thread.currentThread().getContextClassLoader();
		try {
			return Boards.read(new InputStreamReader(cl.getResourceAsStream(boardFile), "UTF-8")).get(boardIndex);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private Board readBoard(final String boardFile, final String boardName) {
		final ClassLoader cl = Thread.currentThread().getContextClassLoader();
		try {
			return Boards.read(new InputStreamReader(cl.getResourceAsStream(boardFile), "UTF-8")).get(boardName);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
