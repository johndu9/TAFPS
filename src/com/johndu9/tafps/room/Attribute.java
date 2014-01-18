package com.johndu9.tafps.room;

public class Attribute {

	public static final class Type {
		public static final Attribute HALL = new Attribute(new String[]{
				"You see a hallway before you. ",
				"This hallway is likely hard to dodge in. ",
				"You enter a long hall. ",
				"You walk into a hall. "
			},
			-1
		);
		public static final Attribute OPEN = new Attribute(new String[]{
				"This room is completely open. ",
				"The room is wide open for all sorts of activity. ",
				"You enter a vast, open room. ",
				"You walk into a large, bare room. "
			},
			0
		);
		public static final Attribute CLUTTERED = new Attribute(new String[]{
				"The room is littered with all sorts of clutter. ",
				"It is messy in here, and the mess may likely provide cover. ",
				"There is trash everywhere. Great for catching wayward bullets. ",
				"Tacical messiness. "
			},
			2
		);
		public static final Attribute[] ATTRIBUTES = new Attribute[]{HALL, OPEN, CLUTTERED};
	}
	
	public static final class Feature {
		public static final Attribute DARK = new Attribute(new String[]{
				"The room is dark. ",
				"It is pitch black. You are likely to be shot. ",
				"You stumble a little as you enter the dark room. ",
				"It is nearly impossible to see anything. "
			},
			2
		);
		public static final Attribute BRIGHT = new Attribute(new String[]{
				"The room is well lit. ",
				"The flickering lights light the room fairly well. ",
				"The scattered light bulbs somehow completely illuminate the room. ",
				"You see the room's contents fairly well. "
			},
			0
		);
		public static final Attribute DIM = new Attribute(new String[]{
				"The room is dim. ",
				"Some lights flicker, but most stay off. ",
				"There are more broken bulbs on the ground than there are on the ceiling. ",
				"You need to squint to see. "
			},
			0
		);
		public static final Attribute[] ATTRIBUTES = new Attribute[]{DARK, BRIGHT, DIM};
	}
	
	public static final class Physical {
		public static final Attribute DWARF = new Attribute(new String[]{
				"You happen to be very short. ",
				"You possess a beard and a short stature. "
			},
			1
		);
		public static final Attribute TALL = new Attribute(new String[]{
				"You appear gigantic. ",
				"You are very tall. ",
				"You make short people feel very bad. "
			},
			-1
		);
		public static final Attribute QUICK = new Attribute(new String[]{
				"You are very agile. ",
				"You are quick to your feet in bad situations. "
			},
			1
		);
		public static final Attribute GLUTTON = new Attribute(new String[]{
				"You appear gigantic around the waist. ",
				"You cannot see your toes. "
			},
			-1
		);
		public static final Attribute[] ATTRIBUTES = new Attribute[]{DWARF, TALL, QUICK, GLUTTON};
	}
	
	public static final class Personality {
		public static final Attribute SHOWBOAT = new Attribute(new String[]{
				"You love to show off. ",
				"Your love of life is almost as intense as your love for attention. "
			},
			-1
		);
		public static final Attribute ANTISOCIAL = new Attribute(new String[]{
				"You are often alone and happen to be good at hiding from others. ",
				"You like to avoid people. "
			},
			1
		);
		public static final Attribute LAZY = new Attribute(new String[]{
				"You enjoy taking your time. ",
				"You like to take leisurely strolls. "
			},
			-1
		);
		public static final Attribute WITTY = new Attribute(new String[]{
				"You are a quick thinker. ",
				"You have an agile mind. "
			},
			1
		);
		public static final Attribute[] ATTRIBUTES = new Attribute[]{SHOWBOAT, ANTISOCIAL, LAZY, WITTY};
	}
	
	public static final Attribute[][] ROOM_ATTRIBUTES = new Attribute[][]{Type.ATTRIBUTES, Feature.ATTRIBUTES};
	public static final Attribute[][] PLAYER_ATTRIBUTES = new Attribute[][]{Physical.ATTRIBUTES, Personality.ATTRIBUTES};
	private String[] descriptions;
	private int advantage;
	
	private Attribute(String[] descriptions, int advantage) {
		this.descriptions = descriptions;
		this.advantage = advantage;
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
