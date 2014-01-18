package com.johndu9.tafps;

import java.util.Random;

import com.johndu9.tafps.room.Attribute;
import com.johndu9.tafps.room.Room;

public class Map {
	
	private Room[][] rooms;
	private Random rng;
	public final int size;
	
	public Map(int size, Random rng) {
		this.size = size;
		rooms = new Room[size][size];
		this.rng = rng;
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				rooms[i][j] = buildRoom();
			}
		}
	}
	
	public Room getRoom(int x, int y) {
		return rooms[x][y];
	}
	
	private Room buildRoom() {
		Attribute[] attributes = new Attribute[Attribute.ROOM_ATTRIBUTES.length];
		for (int i = 0; i < Attribute.ROOM_ATTRIBUTES.length; i++) {
			attributes[i] = Attribute.ROOM_ATTRIBUTES[i][rng.nextInt(Attribute.ROOM_ATTRIBUTES[i].length)];
		}
		return new Room(attributes, rng);
	}
}