package com.szqtom.dyna.generator;


public class Point extends java.awt.Point {

	private static final long serialVersionUID = -2419103104039001025L;

	private boolean available_left_direction;
	
	private boolean available_right_direction;
	
	private boolean available_up_direction;
	
	private boolean available_down_direction;
	
	private boolean used_left_direction = false;
	
	private boolean used_right_direction = false;
	
	private boolean used_up_direction = false;
	
	private boolean used_down_direction = false;
	
	public Point(java.awt.Point point, boolean left, boolean right, boolean up, boolean down){
		super(point);
		available_left_direction = left;
		available_right_direction = right;
		available_up_direction = up;
		available_down_direction = down;
	}
	
	public Point(int x, int y, boolean left, boolean right, boolean up, boolean down){
		super(x,y);
		available_left_direction = left;
		available_right_direction = right;
		available_up_direction = up;
		available_down_direction = down;
	}
	
	public Point(int x, int y){
		super(x,y);
	}
	
	public Point(java.awt.Point point){
		super(point.x, point.y);
	}
	
	public Point(){
		
	}
	

	public boolean isAvailable_left_direction() {
		return available_left_direction;
	}
	
	public void setAvailable_left_direction(boolean available_left_direction) {
		this.available_left_direction = available_left_direction;
	}

	public boolean isAvailable_right_direction() {
		return available_right_direction;
	}

	public void setAvailable_right_direction(boolean available_right_direction) {
		this.available_right_direction = available_right_direction;
	}

	public boolean isAvailable_up_direction() {
		return available_up_direction;
	}

	public void setAvailable_up_direction(boolean available_up_direction) {
		this.available_up_direction = available_up_direction;
	}

	public boolean isAvailable_down_direction() {
		return available_down_direction;
	}

	public void setAvailable_down_direction(boolean available_down_direction) {
		this.available_down_direction = available_down_direction;
	}

	public boolean isUsed_left_direction() {
		return used_left_direction;
	}

	public void setUsed_left_direction(boolean used_left_direction) {
		this.used_left_direction = used_left_direction;
	}

	public boolean isUsed_right_direction() {
		return used_right_direction;
	}

	public void setUsed_right_direction(boolean used_right_direction) {
		this.used_right_direction = used_right_direction;
	}

	public boolean isUsed_up_direction() {
		return used_up_direction;
	}

	public void setUsed_up_direction(boolean used_up_direction) {
		this.used_up_direction = used_up_direction;
	}

	public boolean isUsed_down_direction() {
		return used_down_direction;
	}

	public void setUsed_down_direction(boolean used_down_direction) {
		this.used_down_direction = used_down_direction;
	}	
}
