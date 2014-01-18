package com.johndu9.tafps;

import java.util.Random;

import com.johndu9.tafps.room.Attribute;

public class Player {
	
	public static final int MAX_AMMO = 24;
	private int x;
	private int y;
	private int direction;
	private int ammo;
	public long lastMoveTime;
	public boolean waiting = false;
	private Attribute[] attributes;
	private String description = "";
	private int advantage = 0;
	private Random rng;
	public final int id;
	public static final int EAST = 0;
	public static final int NORTH = 1;
	public static final int WEST = 2;
	public static final int SOUTH = 3;
	public static final String[] DIRECTIONS = new String[]{"EAST", "NORTH", "WEST", "SOUTH"};
	
	public Player(int x, int y, int id, Random rng) {
		move(x, y);
		ammo = MAX_AMMO;
		direction = NORTH;
		this.rng = rng;
		build();
		this.id = id;
		lastMoveTime = System.currentTimeMillis();
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public int getAmmo() {
		return ammo;
	}
	
	public int getDirection() {
		return direction;
	}
	
	public void setDirection(int direction) {
		this.direction = direction;
	}

	public void turnRight() {
		direction = (direction == EAST) ? (SOUTH) : (direction - 1);
	}
	
	public void turnLeft() {
		direction = (direction == SOUTH) ? (EAST) : (direction + 1);
	}
	
	public void move(int x, int y) {
		this.x += x;
		this.y += y;
	}
	
	public void charge(int x, int y) {
		move(x, y);
		shoot();
	}
	
	public void shoot() {
		if (ammo > 0) {
			ammo--;
		}
	}
	
	public String getDescription() {
		return description + "You have " + ammo + " shots left in your gun. ";
	}
	
	public int getAdvantage() {
		return advantage;
	}
	
	private void build() {
		attributes = Attribute.getAttributes(Attribute.PLAYER_ATTRIBUTES, rng);
		for (Attribute attribute : attributes) {
			description += attribute.getDescription(rng.nextInt(attribute.getDesciptionCount()));
			advantage += attribute.getAdvantage();
		}
	}
	
}