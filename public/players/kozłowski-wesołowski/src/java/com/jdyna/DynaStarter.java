package com.jdyna;

import java.awt.Dimension;
import java.io.FileOutputStream;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.dawidweiss.dyna.Board;
import com.dawidweiss.dyna.BoardInfo;
import com.dawidweiss.dyna.Game;
import com.dawidweiss.dyna.GameResult;
import com.dawidweiss.dyna.Globals;
import com.dawidweiss.dyna.Player;
import com.dawidweiss.dyna.Game.Mode;
import com.dawidweiss.dyna.serialization.GameWriter;
import com.dawidweiss.dyna.view.swing.BoardFrame;
import com.google.common.collect.ImmutableList;
import com.jdyna.emulator.AbstractPlayerEmulator;
import com.jdyna.emulator.CarefulRandomWalker;
import com.jdyna.emulator.SmartBomber;

/**
 * The <b>Dyna Blaster</b> game starter used for testing objects inherited from {@link AbstractPlayerEmulator}.
 * 
 * @author Michał Kozłowski
 * @author Bartosz Wesołowski
 */
public final class DynaStarter {
	private static final String SAVED_GAME_FILE_NAME = "last_game.sav";
	private final Game game;

	public DynaStarter(final GameParameters params) {
		final Board board = params.getBoard();
		final BoardInfo boardInfo = new BoardInfo(new Dimension(board.width, board.height), Globals.DEFAULT_CELL_SIZE);

		// create game
		game = new Game(board, boardInfo, params.getPlayers());
		game.setFrameRate(25);
		if (params.getFramesLimit() > 0) {
			game.setFrameLimit(params.getFramesLimit());
		}
	}

	public GameResult run(final boolean graphicMode) {
		// create {@link BoardFrame} if in graphic mode
		final BoardFrame frame;
		if (graphicMode) {
			frame = createFrame();
			game.addListener(frame);
		} else {
			frame = null;
		}

		// create GameWriter
		try {
			game.addListener(new GameWriter(new FileOutputStream(SAVED_GAME_FILE_NAME)));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		// start game
		final GameResult result = game.run(Mode.LAST_MAN_STANDING);

		// dispose {@link BoardFrame} if in graphic mode
		if (graphicMode) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					frame.dispose();
				}
			});
		}

		return result;
	}

	/**
	 * Graphics mode is enabled by default.
	 */
	public GameResult run() {
		// create {@link BoardFrame} if in graphic mode
		final BoardFrame frame;
		frame = createFrame();
		game.addListener(frame);

		// create GameWriter
		try {
			game.addListener(new GameWriter(new FileOutputStream(SAVED_GAME_FILE_NAME)));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		// start game
		final GameResult result = game.run(Mode.LAST_MAN_STANDING);

		// dispose {@link BoardFrame} if in graphic mode
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				frame.dispose();
			}
		});

		return result;
	}

	private BoardFrame createFrame() {
		final BoardFrame frame = new BoardFrame();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		return frame;
	}

	/* Command-line entry point. */
	public static void main(String[] args) throws Exception {
		final ImmutableList<Player> players = ImmutableList.of(
				new Player("Ja", Globals.getDefaultKeyboardController(0)),
				new SmartBomber("B 1").getPlayer(), 
				new CarefulRandomWalker("CW 1").getPlayer());

		final GameParameters params = new GameParameters("boards.conf", "test", players.toArray(new Player[0]));
		final DynaStarter starter = new DynaStarter(params);
		starter.run();
	}

}
