package com.kozmich.dyna.ai;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.dawidweiss.dyna.Cell;
import com.dawidweiss.dyna.GameEvent;
import com.dawidweiss.dyna.GameStateEvent;
import com.dawidweiss.dyna.Globals;
import com.dawidweiss.dyna.IGameEventListener;
import com.dawidweiss.dyna.IPlayerSprite;
import com.dawidweiss.dyna.GameEvent.Type;

/**
 * Implementation of Floyda-Warshall alghoritm.
 * 
 * @author Lukasz Kozminski
 * @author Tomasz Michalak
 *
 */
public class PathFinder implements IGameEventListener {

	Node[] nodes;
	Edge[] edges;
	int[][] D;
	Node[][] P;
	
	private String playerName;

	public PathFinder(String playerName) {
		this.playerName = playerName;
	}
	
	@Override
	public void onFrame(int frame, List<? extends GameEvent> events) {
		GameStateEvent gse = null;
		for (Iterator<? extends GameEvent> iterator = events.iterator(); iterator.hasNext();) {
			GameEvent gameEvent = iterator.next();
			if (gameEvent.type == Type.GAME_STATE) {
				gse = (GameStateEvent) gameEvent;
				break;
			}
		}
		if (gse != null) {
			Cell[][] cells = gse.getCells();
			nodes = PathFinderUtils.convertToGraphNodes(cells);
			edges = PathFinderUtils.convertToGraphEdges(cells, nodes, getPlayer(gse.getPlayers()));
			calcShortestPaths();
		}
	}
	
	public Node getNode(int row, int column) {
		for (int k = 0; k < nodes.length; k++) {
			if (nodes[k].row == row && nodes[k].column == column) {
				return nodes[k];
			}
		}
		return null;
	}

	private Node getPlayer(List<? extends IPlayerSprite> players) {
		for (IPlayerSprite player : players) {
			if (player.getName() == playerName) {
				return getNode(player.getPosition().x / Globals.DEFAULT_CELL_SIZE, player.getPosition().y / Globals.DEFAULT_CELL_SIZE);
			}
		}
		return null;
	}

	private void calcShortestPaths() {
		D = initializeWeight(nodes, edges);
		P = new Node[nodes.length][nodes.length];
		for (int k = 0; k < nodes.length; k++) {
			for (int i = 0; i < nodes.length; i++) {
				for (int j = 0; j < nodes.length; j++) {
					if (D[i][k] != Integer.MAX_VALUE
							&& D[k][j] != Integer.MAX_VALUE
							&& D[i][k] + D[k][j] < D[i][j]) {
						D[i][j] = D[i][k] + D[k][j];
						P[i][j] = nodes[k];
					}
				}
			}
		}
	}

	public int getShortestDistance(Node source, Node target) {
		return D[source.name][target.name];
	}

	public NodeList getShortestPath(Node source, Node target) {
		if (D[source.name][target.name] == Integer.MAX_VALUE) {
			return new NodeList();
		}
		List<Node> path = getIntermediatePath(source, target);
		path.add(0, source);
		path.add(target);
		return new NodeList(path);
	}

	private List<Node> getIntermediatePath(Node source, Node target) {
		if (D == null) {
			throw new IllegalArgumentException(
					"Must call calcShortestPaths(...) before attempting to obtain a path.");
		}
		if (P[source.name][target.name] == null) {
			return new ArrayList<Node>();
		}
		ArrayList<Node> path = new ArrayList<Node>();
		path.addAll(getIntermediatePath(source, P[source.name][target.name]));
		path.add(P[source.name][target.name]);
		path.addAll(getIntermediatePath(P[source.name][target.name], target));
		return path;
	}

	private int[][] initializeWeight(Node[] nodes, Edge[] edges) {
		int[][] weight = new int[nodes.length][nodes.length];
		for (int i = 0; i < nodes.length; i++) {
			Arrays.fill(weight[i], Integer.MAX_VALUE);
		}
		for (Edge e : edges) {
			weight[e.from.name][e.to.name] = e.weight;
		}
		return weight;
	}

	public Node getNode(Cell[][] cells, Point point) {
		int index = 0;
		for (int i = 0; i < point.x; i++) {
			index += cells[i].length;
		}
		index += point.y;
		return nodes[index];
	}

	public Point getPoint(Node node) {
		return new Point(node.row, node.column);
	}


}
