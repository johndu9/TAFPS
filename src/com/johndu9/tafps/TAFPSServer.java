package com.johndu9.tafps;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.johndu9.tafps.Network.ActionMessage;
import com.johndu9.tafps.Network.DescriptionMessage;
import com.johndu9.tafps.Network.Join;

public class TAFPSServer {
	
	private Server server;
	private final Random rng;
	private Map map;
	private List<Player> players = new LinkedList<Player>();
	private static final String[] MISS = new String[]{
		"You missed your shot.",
		"You shoot. And you miss.",
		"Your target eludes your bullet.",
		"Your prey escapes death."
	};
	private static final String[] SHOT_AT = new String[]{
		"You are shot at.",
		"A bullet whizzes by your head.",
		"You barely dodge death and a bullet.",
		"A bullet grazes your shoulder."
	};
	private static final String[] WALL = new String[]{
		"You run into a dead end.",
		"There is a wall in front of you. Nowhere to go.",
		"It's a dead end.",
		"You walk into a wall. How embarassing.",
		"You stub your toe on a wall. Dead end.",
		"The wall halts your progress.",
		"You attempt to stubbornly walk into the wall. It doesn't work.",
		"Walking into the wall doesn't work out.",
		"You attempt to phase through the wall. It doesn't work"
	};
	private static final String[] PRESENT = new String[]{
		"You sense others. You see them, too.",
		"There are others present.",
		"You found survivors. Not for long."
	};
	private static final String[] EMPTY = new String[]{
		"You're out of shots.",
		"You attempt to fire only to realize you've run out of ammunition.",
		"You shoot. Or try to. You're out of ammo."
	};
	private static final String[] KILL = new String[]{
		"You shoot at and kill the other survivor.",
		"The other survivor collapses, dead as stone.",
		"One shot, one kill"
	};
	private static final String[] DEATH = new String[]{
		"You have been shot and reincarnate elsewhere.",
		"You die. However, you have been reborn somewhere else.",
		"The gods gave and the gods have taken away. But they are merciful, and grant you new life."
	};
	private static final String[] NOBODY = new String[]{
		"You fire into the empty room.",
		"At least no one saw you shoot the wall.",
		"You shoot the ceiling. Do you hate ceilings?"
	};
	
	public TAFPSServer(int size, String seed) throws IOException {
		server = new Server() {
			protected Connection newConnection() {
				return new ClientConnection();
			}
		};
		rng = new Random(seed.hashCode());
		map = new Map(size, rng);
		Network.register(server);
		server.addListener(new Listener() {
			public void received (Connection c, Object object) {
				if (object instanceof Join) {
					Player newPlayer = buildPlayer(c.getID());
					players.add(newPlayer);
					return;
				}
				if (object instanceof ActionMessage) {
					String action = ((ActionMessage)object).message;
					Player player = findPlayer(c.getID());
					switch (action) {
					case "tr":
						player.turnRight();
						break;
					case "tl":
						player.turnLeft();
						break;
					case "mf":
						movePlayer(player, 0);
						break;
					case "ml":
						movePlayer(player, 1);
						break;
					case "mb":
						movePlayer(player, 2);
						break;
					case "mr":
						movePlayer(player, 3);
						break;
					case "f":
						combat(player);
						break;
					}
					return;
				}
			}
			public void disconnected(Connection c){
				players.remove(findPlayer(c.getID()));
			}
		});
		server.bind(Network.TCPPORT, Network.UDPPORT);
		server.start();
	}
	
	public void combat(Player attacker) {
		List<Player> playersAtDestination = getPlayersOn(attacker.getX(), attacker.getY());
		if (attacker.getAmmo() == 0) {
			sendDescription(attacker, EMPTY[rng.nextInt(EMPTY.length)]);
		} else {
			attacker.shoot();
			sendDescription(attacker, "You have " + attacker.getAmmo() + " shots left.");
		}
		if (playersAtDestination.size() == 1) {
			sendDescription(attacker, NOBODY[rng.nextInt(NOBODY.length)]);
		}
		for (Player player : playersAtDestination) {
			int x = player.getX();
			int y = player.getY();
			if (!player.equals(attacker) && x == attacker.getX() && y == attacker.getY()) {
				if (attacker.getAmmo() != 0 &&
					attacker.getAdvantage() + rng.nextInt(6) <=
					player.getAdvantage() + map.getRoom(x, y).getAdvantage() + rng.nextInt(6)) {
					sendDescription(attacker, MISS[rng.nextInt(MISS.length)]);
					sendDescription(player, SHOT_AT[rng.nextInt(SHOT_AT.length)]);
				} else {
					sendDescription(attacker, KILL[rng.nextInt(KILL.length)]);
					sendDescription(player, DEATH[rng.nextInt(DEATH.length)]);
					int playerID = player.id;
					player = buildPlayer(playerID);
				}
			}
		}
	}
	
	public void movePlayer(Player player, int movementDirection) {
		double direction = ((player.getDirection() + movementDirection) * Math.PI / 2) % (2 * Math.PI);
		int x = (int)Math.cos(direction);
		int y = (int)Math.sin(direction);
		boolean walled = false;
		if ((x == 1 && player.getX() == map.size - 1) || (x == -1 && player.getX() == 0)) {
			x = 0;
			walled = true;
		}
		if ((y == 1 && player.getY() == map.size - 1) || (y == -1 && player.getY() == 0)) {
			y = 0;
			walled = true;
		}
		if (walled) {
			sendDescription(player, WALL[rng.nextInt(WALL.length)]);
		} else {
			sendDescription(player, map.getRoom(player.getX() + x, player.getY() + y).getDescription());
		}
		player.move(x, y);
		List<Player> playersAtDestination = getPlayersOn(player.getX(), player.getY());
		if (playersAtDestination.size() > 1) {
			for (Player roommate : playersAtDestination) {
				sendDescription(roommate, PRESENT[rng.nextInt(PRESENT.length)]);
			}
		}
		System.out.println(
			player.getDirection() + "," + movementDirection + "," + Math.toDegrees(direction) +
			"(" + x + "," + y + ")" +
			"/(" + player.getX() + "," + player.getY() + ")");
	}
	
	public List<Player> getPlayersOn(int x, int y) {
		List<Player> roommates = new LinkedList<Player>();
		for (Player player : players) {
			if (player.getX() == x && player.getY() == y) {
				roommates.add(player);
			}
		}
		return roommates;
	}
	
	public Player buildPlayer(int id) {
		Player newPlayer = new Player(rng.nextInt(map.size), rng.nextInt(map.size), id, rng);
		sendDescription(newPlayer, "You enter a new void.\n" + newPlayer.getDescription());
		return newPlayer;
	}
	
	public Player findPlayer(int id) {
		for (Player player : players) {
			if (player.id == id) {
				return player;
			}
		}
		return null;
	}
	
	public void sendDescription(Player player, String description) {
		DescriptionMessage send = new DescriptionMessage();
		send.message = description;
		server.sendToTCP(player.id, send);
	}
	
	public static class ClientConnection extends Connection {
		public String name;
	}
	
	public static void main(String[] args) throws IOException {
		System.out.println("Server begin");
		if (args.length == 2) {
			new TAFPSServer(Integer.parseInt(args[0]), args[1]);
		} else {
			new TAFPSServer(5, "TAFPS");
		}
	}
}