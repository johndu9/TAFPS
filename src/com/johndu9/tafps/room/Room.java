package com.johndu9.tafps.room;

import java.util.Random;

public class Room {
	
	private Attribute[] attributes;
	private Random rng;
	private String description = "";
	private int advantage = 0;
	
	public Room(Attribute[] attributes, Random rng) {
		this.attributes = attributes;
		this.rng = rng;
		build();
	}
	
	private void build() {
		for (Attribute attribute : attributes) {
			description += attribute.getDescription(rng.nextInt(attribute.getDesciptionCount()));
			advantage += attribute.getAdvantage();
		}
	}
	
	public String getDescription() {
		return description;
	}
	
	public int getAdvantage() {
		return advantage;
	}
}