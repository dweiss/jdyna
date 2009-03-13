package AIplayer.ai.in71391;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.jdyna.CellType;
import org.jdyna.Globals;

import AIplayer.model.BoardInformationClass;
import AIplayer.model.BombInfo;
import AIplayer.model.GameStateInformation;
import AIplayer.model.PlayerInfo;
import AIplayer.model.SuperCell;


/**
 * Class responsible for parse map receive from Game controller and pass board
 * with full information to AI thread.
 * 
 * @author Łukasz Witowski
 * 
 */
public class PlayerContollerThread extends Thread {

	private final static Logger logger = Logger
			.getLogger("PlayerControlledThread");

	/** Time of fuses on board after bomb blow up */
	private static final int explosionTime = 14;

	private final int cellSize = Globals.DEFAULT_CELL_SIZE;

	/** how long the board cell is unavailable */
	ArrayList<BombInfo> bombFusesFlames = new ArrayList<BombInfo>();

	/** list of frames not handle yet */
	ArrayList<GameStateInformation> list = new ArrayList<GameStateInformation>();

	/**
	 * Information from playerServant about a frame change is passing through
	 * this queue
	 */
	private LinkedBlockingQueue<GameStateInformation> frameGameInfo;
	/** Information to aiThread is passing through this queue */
	private LinkedBlockingQueue<BoardInformationClass> aiGameInfo;
	/** last know information about board by frame */
	private GameStateInformation lastframeGameInfo = null;
	/** last know information about board by supercell */
	private BoardInformationClass lastBoardGameInfo = null;
	/** what should be pass by aiGameInfo queue */
	private BoardInformationClass newBoardGameInfo = null;

	/** default range for bomb */
	private int maxKnownRange = Globals.DEFAULT_BOMB_RANGE;

	/** information about last players positions */
	public ArrayList<PlayerInfo> playersOnBoard;
	
	/** Variable to stop the thread */
	private volatile boolean threadDone = false;

	public PlayerContollerThread(
			LinkedBlockingQueue<GameStateInformation> frameGameInfo,
			LinkedBlockingQueue<BoardInformationClass> aiGameInfo) {
		this.frameGameInfo = frameGameInfo;
		this.aiGameInfo = aiGameInfo;
	}

	public void done() {
		threadDone = true;
	}
	
	public void run() {
		try {
			while (!threadDone) {
				list.clear();
				// take out form queue
				int number = frameGameInfo.drainTo(list);
				if (number == 0) {
					GameStateInformation state_0 = frameGameInfo.poll(5,
							TimeUnit.SECONDS);
					frameGameInfo.drainTo(list);
					if (state_0 != null)
						list.add(0, state_0);
				}
				if (list.size() > 0) {
					if (lastframeGameInfo == null) {
						firstInvoke();
					} else {
						regularInvoke();
					}
				} else {
					if (!threadDone) logger.info("No Frame received");
					break;
				}
				aiGameInfo.put(newBoardGameInfo.clone());
				newBoardGameInfo = null;
			}
		} catch (InterruptedException e) {
			logger.error("PlayerContollerThread - interrupted - "
					+ e.getMessage());
		}
	}

	/**
	 * Refresh information about board
	 */
	private void regularInvoke() {
		GameStateInformation helpframeGameInfo = list.get(0);
		newBoardGameInfo = new BoardInformationClass();
		playersOnBoard = lastBoardGameInfo.playersOnBoard;

		for (int s = 0; s < list.size(); s++) {
			helpframeGameInfo = list.get(s);

			for (int t = 0; t < helpframeGameInfo.players.size(); t++) {
				PlayerInfo pI = helpframeGameInfo.players.get(t);
				boolean byl = false;
				for (int k = 0; k < playersOnBoard.size(); k++)
					if (playersOnBoard.get(k).getName().equals(pI.getName()))
						byl = true;
				if (!byl) {
					pI.setBombsNumber(Globals.DEFAULT_BOMB_COUNT);
					pI.setRange(Globals.DEFAULT_BOMB_RANGE);
					playersOnBoard.add(pI);
				}
			}

			newBoardGameInfo.setFrameNumber(helpframeGameInfo.getFrameNumber());

			for (int t = 0; t < helpframeGameInfo.getCells().length; t++) {
				ArrayList<SuperCell> superCellList = new ArrayList<SuperCell>();
				for (int tt = 0; tt < helpframeGameInfo.getCells()[t].length; tt++) {

					CellType typ = helpframeGameInfo.getCells()[t][tt].type;

					// the same type rewrite
					if (typ.equals(lastBoardGameInfo.cells.get(t).get(tt).type)) {
						superCellList.add(lastBoardGameInfo.cells.get(t)
								.get(tt));

					} else {

						int findNum = 0;
						// someone put a bomb
						if (typ.equals(CellType.CELL_BOMB)) {
							BombInfo bombInfo = new BombInfo("", 0, 0, 0, 0);
							int find = 0;
							String name = "";
							for (int h = 0; h < helpframeGameInfo.players
									.size(); h++) {
								if (helpframeGameInfo.players.get(h)
										.getPosition_x()
										/ cellSize == t
										&& helpframeGameInfo.players.get(h)
												.getPosition_y()
												/ cellSize == tt) {
									name = helpframeGameInfo.players.get(h)
											.getName();
									find++;
									for (int l = 0; l < playersOnBoard.size(); l++) {
										if (name.equals(playersOnBoard.get(l)
												.getName())) {
											findNum = l;
											bombInfo.setRange(Math.max(bombInfo
													.getRange(), playersOnBoard
													.get(l).getRange()));
										}
									}
								}
							}

							if (find == 1) {
								playersOnBoard.get(findNum).setBombsNumber(
										playersOnBoard.get(findNum)
												.getBombsNumber() - 1);
								bombInfo.setOwnerName(name);
							} else {
								// not known who put that bomb
								bombInfo.setRange(maxKnownRange);
								// more then one could put a bomb
								bombInfo.setOwnerName("");
							}
							bombInfo.setPosition_x(t);
							bombInfo.setPosition_y(tt);
							bombInfo.setTimeToBlow(Globals.DEFAULT_FUSE_FRAMES
									+ lastframeGameInfo.getFrameNumber() + 1);

							superCellList.add(new SuperCell(typ,
									lastBoardGameInfo.cells.get(t).get(tt)
											.getNumPlayers(), true, bombInfo));
						} else {
							// someone or something take a bonus
							if (lastBoardGameInfo.cells.get(t).get(tt).type
									.equals(CellType.CELL_BONUS_BOMB)
									|| lastBoardGameInfo.cells.get(t).get(tt).type
											.equals(CellType.CELL_BONUS_RANGE)) {
								String name = "";
								for (int h = 0; h < helpframeGameInfo.players
										.size(); h++) {
									if (helpframeGameInfo.players.get(h)
											.getPosition_x()
											/ cellSize == t
											&& helpframeGameInfo.players.get(h)
													.getPosition_y()
													/ cellSize == tt) {
										name = helpframeGameInfo.players.get(h)
												.getName();
										for (int l = 0; l < playersOnBoard
												.size(); l++) {
											if (name.equals(playersOnBoard.get(
													l).getName())) {
												if (lastBoardGameInfo.cells
														.get(t).get(tt).type
														.equals(CellType.CELL_BONUS_BOMB))
													playersOnBoard
															.get(l)
															.setBombsNumber(
																	playersOnBoard
																			.get(
																					l)
																			.getBombsNumber() + 1);
												if (lastBoardGameInfo.cells
														.get(t).get(tt).type
														.equals(CellType.CELL_BONUS_RANGE)) {
													playersOnBoard
															.get(l)
															.setRange(
																	playersOnBoard
																			.get(
																					l)
																			.getRange() + 1);
													if (playersOnBoard.get(l)
															.getRange() > maxKnownRange)
														maxKnownRange = playersOnBoard
																.get(l)
																.getRange();
												}
											}
										}
									}
								}
								superCellList.add(new SuperCell(typ,
										lastBoardGameInfo.cells.get(t).get(tt)
												.getNumPlayers(),
										lastBoardGameInfo.cells.get(t).get(tt)
												.isBomb(),
										lastBoardGameInfo.cells.get(t).get(tt)
												.getBombinfo()));
							} else
							// bomb blow up
							if (lastBoardGameInfo.cells.get(t).get(tt).type
									.equals(CellType.CELL_BOMB)
									&& lastBoardGameInfo.cells.get(t).get(tt)
											.getBombinfo().getTimeToBlow() >= newBoardGameInfo
											.getFrameNumber() - 1) {
								String name = lastBoardGameInfo.cells.get(t)
										.get(tt).getBombinfo().getOwnerName();
								for (int l = 0; l < playersOnBoard.size(); l++) {
									if (name.equals(playersOnBoard.get(l)
											.getName())) {
										playersOnBoard.get(l).setBombsNumber(
												playersOnBoard.get(l)
														.getBombsNumber() + 1);
									}
								}
								bombFusesFlames.add(lastBoardGameInfo.cells
										.get(t).get(tt).getBombinfo());
								superCellList.add(new SuperCell(typ,
										lastBoardGameInfo.cells.get(t).get(tt)
												.getNumPlayers(), false, null));
							} else {
								// wall blow up
								if (typ.equals(CellType.CELL_CRATE_OUT)) {
									BombInfo bIn = new BombInfo("",
											newBoardGameInfo.getFrameNumber(),
											-1, t, tt);
									bombFusesFlames.add(bIn);
									superCellList.add(new SuperCell(typ,
											lastBoardGameInfo.cells.get(t).get(
													tt).getNumPlayers(), false,
											bIn));
								} else {
									// something else change
									superCellList.add(new SuperCell(typ,
											lastBoardGameInfo.cells.get(t).get(
													tt).getNumPlayers(), false,
											null));
								}
							}
						}
					}
				}
				newBoardGameInfo.cells.add(superCellList);
			}
			lastBoardGameInfo = newBoardGameInfo;
		}

		// rewrite players to know positions
		for (int t = 0; t < playersOnBoard.size(); t++) {
			PlayerInfo plI = playersOnBoard.get(t);
			SuperCell sC = lastBoardGameInfo.cells.get(
					plI.getPosition_x() / cellSize).get(
					plI.getPosition_y() / cellSize);
			PlayerInfo pIB = new PlayerInfo(plI.getName(),
					plI.getBombsNumber(), plI.getRange(), -1, -1);
			// old one destroy information about player
			sC.setNumPlayers(sC.getNumPlayers() - 1);
			for (int l = 0; l < helpframeGameInfo.players.size(); l++) {
				if (pIB.getName().equals(
						helpframeGameInfo.players.get(l).getName())) {
					PlayerInfo phI = helpframeGameInfo.players.get(l);
					SuperCell sC2 = lastBoardGameInfo.cells.get(
							phI.getPosition_x() / cellSize).get(
							phI.getPosition_y() / cellSize);
					pIB.setPosition_x(phI.getPosition_x());
					pIB.setPosition_y(phI.getPosition_y());
					sC2.setNumPlayers(sC2.getNumPlayers() + 1);
					pIB.setDead(phI.isDead());
					pIB.setImmortal(phI.isImmortal());
					newBoardGameInfo.playersOnBoard.add(pIB);
				}
			}
		}

		// rewrite unreal bombs
		ArrayList<BombInfo> newBombFusses = new ArrayList<BombInfo>();
		for (int t = 0; t < bombFusesFlames.size(); t++) {
			BombInfo bI = bombFusesFlames.get(t);
			if (newBoardGameInfo.getFrameNumber() < bI.getTimeToBlow()) {
				bI.setTimeToBlow(newBoardGameInfo.getFrameNumber());
			}
			if (newBoardGameInfo.getFrameNumber() - bI.getTimeToBlow() <= explosionTime) {
				newBombFusses.add(bombFusesFlames.get(t));
				SuperCell sC2 = newBoardGameInfo.cells.get(bI.getPosition_x())
						.get(bI.getPosition_y());
				if (bI.getRange() > 0) {
					newBoardGameInfo.cells.get(bI.getPosition_x()).remove(
							bI.getPosition_y());
					sC2 = new SuperCell(CellType.CELL_BOMB,
							sC2.getNumPlayers(), true, bI);
					newBoardGameInfo.cells.get(bI.getPosition_x()).add(
							bI.getPosition_y(), sC2);
				}
			}
		}
		bombFusesFlames = new ArrayList<BombInfo>(newBombFusses);

		lastBoardGameInfo = newBoardGameInfo;
		lastframeGameInfo = helpframeGameInfo;
	}

	/**
	 * First information about board
	 */
	private void firstInvoke() {
		lastframeGameInfo = list.get(0);
		lastBoardGameInfo = new BoardInformationClass();
		lastBoardGameInfo.setFrameNumber(lastframeGameInfo.getFrameNumber());
		for (int t = 0; t < lastframeGameInfo.players.size(); t++) {
			PlayerInfo pI = lastframeGameInfo.players.get(t);
			pI.setBombsNumber(Globals.DEFAULT_BOMB_COUNT);
			pI.setRange(Globals.DEFAULT_BOMB_RANGE);
			lastBoardGameInfo.playersOnBoard.add(pI);
		}

		// put bombs to supercells
		for (int t = 0; t < lastframeGameInfo.getCells().length; t++) {
			ArrayList<SuperCell> superCellList = new ArrayList<SuperCell>();
			for (int tt = 0; tt < lastframeGameInfo.getCells()[t].length; tt++) {
				CellType typ = lastframeGameInfo.getCells()[t][tt].type;
				if (typ.equals(CellType.CELL_BOMB)) {
					BombInfo bombInfo = new BombInfo("",
							Globals.DEFAULT_FUSE_FRAMES
									+ lastframeGameInfo.getFrameNumber(),
							Globals.DEFAULT_BOMB_RANGE, t, tt);
					superCellList.add(new SuperCell(typ, 0, true, bombInfo));
				} else {
					if (typ.equals(CellType.CELL_CRATE_OUT)) {
						BombInfo bIn = new BombInfo("", 0, -1, t, tt);
						bombFusesFlames.add(bIn);
						superCellList.add(new SuperCell(typ, 0, false, bIn));
					} else {
						superCellList.add(new SuperCell(typ));
					}
				}
			}
			lastBoardGameInfo.cells.add(superCellList);
		}
		// put users to supercells
		for (int t = 0; t < lastBoardGameInfo.playersOnBoard.size(); t++) {
			PlayerInfo pI = lastBoardGameInfo.playersOnBoard.get(t);
			SuperCell sC = lastBoardGameInfo.cells.get(
					pI.getPosition_x() / cellSize).get(
					pI.getPosition_y() / cellSize);
			sC.setNumPlayers(sC.getNumPlayers() + 1);
		}

		list.remove(0);
		if (list.size() > 0) {
			regularInvoke();
		} else {
			newBoardGameInfo = lastBoardGameInfo;
		}
	}
}
