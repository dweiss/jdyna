package com.kozmich.dyna.ai;

import java.awt.Dimension;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.jdyna.*;
import org.jdyna.audio.jxsound.GameSoundEffects;
import org.jdyna.serialization.GameWriter;
import org.jdyna.view.swing.BoardFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Only for test.
 * 
 * @author Lukasz Kozminski
 * @author Tomasz Michalak
 * 
 */
public class Main {
	private final static Logger logger = LoggerFactory.getLogger(Main.class);

	/* Command-line entry point. */
	public static void main(String[] args) throws IOException {
		/*
		 * Load board configurations.
		 */
		final ClassLoader cl = Thread.currentThread().getContextClassLoader();
		final Boards boards = Boards.read(new InputStreamReader(cl.getResourceAsStream("boards.conf"), "UTF-8"));

		/*
		 * Set up a single game between two players.
		 */
		final Board board = boards.get("classic");

//		final IPlayerController c1 = Globals.getDefaultKeyboardController(0);
		// final IPlayerController c2 = Globals.getDefaultKeyboardController(1);
		final AiPlayer player1 = new AiPlayer("Tomek");
		final AiPlayer player2 = new AiPlayer("Lukasz");
		final AiPlayer player3 = new AiPlayer("Lukasz2");

		final BoardInfo boardInfo = new BoardInfo(new Dimension(board.width, board.height), Globals.DEFAULT_CELL_SIZE);

		final Game game = new Game(board, boardInfo);
		game.setFrameRate(25);

		// final Player p1 = new Player("Player 1", c1);
		final Player p2 = new Player(player1.getName(), player1);
		final Player p3 = new Player(player2.getName(), player2);
		final Player p4 = new Player(player3.getName(), player3);
		new Thread() {
			public void run() {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// Ignore.
				}
				game.addPlayer(p2);
				game.addPlayer(p3);
				game.addPlayer(p4);
				// game.addPlayer(p3);
			}
		}.start();

		/*
		 * Attach sounds view to the game.
		 */
		game.addListener(new GameSoundEffects());

		/*
		 * Attach game progress saver.
		 */
		game.addListener(new GameWriter(new FileOutputStream("game.log")));

		/*
		 * Attach a display view to the game.
		 */
		final BoardFrame frame = new BoardFrame();
		game.addListener(frame);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		final GameResult result = game.run(Game.Mode.INFINITE_DEATHMATCH);
		logger.info(result.toString());

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				frame.dispose();
			}
		});
	}
}
