package com.johndu9.tafps.room;

import java.util.Random;

import com.johndu9.tafps.Data;

public class Attribute {
	
	private String name;
	private int advantage;
	private String[] descriptions;

	public static Attribute[][] ROOM_ATTRIBUTES = null;
	public static Attribute[][] PLAYER_ATTRIBUTES = null;
	
	public static void build() {
		Attribute[] roomType = getAttributesFromFile("RoomType.txt");
		Attribute[] roomFeature = getAttributesFromFile("RoomFeature.txt");
		Attribute[] roomLight = getAttributesFromFile("RoomLight.txt");
		ROOM_ATTRIBUTES = new Attribute[][]{roomType, roomFeature, roomFeature, roomLight};
		Attribute[] playerPhysical = getAttributesFromFile("PlayerPhysical.txt");
		Attribute[] playerMental = getAttributesFromFile("PlayerMental.txt");
		PLAYER_ATTRIBUTES = new Attribute[][]{playerPhysical, playerPhysical, playerMental};
	}
	
	private static Attribute[] getAttributesFromFile(String file) {
		Data data = new Data("res/attributes/" + file);
		Attribute[] attributes = new Attribute[data.countName("name")];
		for (int i = 0; i < attributes.length; i++) {
			attributes[i] = new Attribute(
				data.getValue("name" + i),
				Integer.parseInt(data.getValue("advantage" + i)),
				data.getElementsFromValue(data.getIndexOfName("description" + i)));
		}
		return attributes;
	}
	
	private Attribute(String name, int advantage, String[] descriptions) {
		this.name = name;
		this.descriptions = descriptions;
		this.advantage = advantage;
	}
	
	public static Attribute[] getAttributes(Attribute[][] category, Random rng) {
		Attribute[] attributes = new Attribute[category.length];
		for (int i = 0; i < attributes.length; i++) {
			boolean repeated = true;
			repeatCheckLoop: while (repeated) {
				Attribute attribute = category[i][rng.nextInt(category[i].length)];
				for (Attribute trait : attributes) {
					if (attribute.equals(trait)) {
						repeated = true;
						continue repeatCheckLoop;
					}
				}
				attributes[i] = attribute;
				break;
			}
		}
		return attributes;
	}
	
	public String getName() {
		return name;
	}
	
	public int getAdvantage() {
		return advantage;
	}
	
	public int getDesciptionCount() {
		return descriptions.length;
	}
	
	public String getDescription(int index) {
		return descriptions[index];
	}
}
