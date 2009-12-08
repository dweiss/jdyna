package org.jdyna.view.jme;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

import org.jdyna.view.resources.jme.DynaBomb;
import org.jdyna.view.resources.jme.DynaBonus;
import org.jdyna.view.resources.jme.DynaCrate;
import org.jdyna.view.resources.jme.DynaPlayer;

import com.jme.scene.Node;

public class BoardData {

	public final Node boardNode = new Node("board");

	public final Map<Point, DynaCrate> crates = new HashMap<Point, DynaCrate>();
	public final Map<Point, DynaBomb> bombs = new HashMap<Point, DynaBomb>();
	public final Map<Point, DynaBonus> bonuses = new HashMap<Point, DynaBonus>();
	public final Map<String, DynaPlayer> players = new HashMap<String, DynaPlayer>();
}