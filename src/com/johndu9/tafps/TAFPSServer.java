package com.johndu9.tafps;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;
import com.johndu9.tafps.Network.ActionMessage;
import com.johndu9.tafps.Network.Death;
import com.johndu9.tafps.Network.DescriptionMessage;
import com.johndu9.tafps.Network.InfoMessage;
import com.johndu9.tafps.Network.Join;
import com.johndu9.tafps.Network.Resume;
import com.johndu9.tafps.Network.Wait;

public class TAFPSServer {
	
	private Server server;
	private final Random rng;
	private Map map;
	private List<Player> players = new LinkedList<Player>();
	private List<Player> savages = new LinkedList<Player>();
	private final int turnTime;
	private static final String LOG_CATEGORY = "TAFPS";
	private static final int SAVAGE_ID = -1;
	private static final String[] SHOT_AT = new String[]{
		"You are shot at.",
		"A bullet whizzes by your head.",
		"You barely dodge death and a bullet.",
		"A bullet grazes your shoulder."
	};
	private static final String[] PUNCHED_AT = new String[]{
		"You take a punch, but it isn't enough to knock you out.",
		"You get kicked in the face, but you get back up.",
		"A punch flies at your face, but you block it.",
		"You dodge a series of attacks."
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
		"You sense others. You see them, too. And they see you.",
		"There are others present.",
		"You found survivors. Not for long.",
		"You hear footsteps and encounter survivors."
	};
	private static final String[] KILL_SHOT = new String[]{
		"You shoot at and kill the other survivor.",
		"The other survivor collapses, dead as stone.",
		"One shot, one kill.",
		"The survivor takes the last shot to the heart and falls to the ground."
	};
	private static final String[] KILL_PUNCH = new String[]{
		"With a crack of his neck, the other survivor falls dead.",
		"Your final blow knocks your enemy against the wall. He doesn't get up.",
		"One last kick breaks your foe's spine and kills the survivor.",
		"The survivor crumples under your punch."
	};
	private static final String[] DEATH = new String[]{
		"You have been killed and reincarnate elsewhere.",
		"You die. However, you have been reborn somewhere else.",
		"The gods gave and the gods have taken away. But they are merciful, and grant you new life."
	};
	private static final String[] NOBODY = new String[]{
		"You fire into the empty room.",
		"At least no one saw you shoot the wall.",
		"You shoot the ceiling. Do you hate ceilings?",
		"Your paranoia gets to you."
	};
	
	public TAFPSServer(int size, int turnDelay, String seed) throws IOException {
		server = new Server() {
			protected Connection newConnection() {
				return new ClientConnection();
			}
		};
		rng = new Random(seed.hashCode());
		turnTime = turnDelay;
		map = new Map(size, rng);
		for (int i = 0; i < size * 2; i++) {
			Player savage = buildPlayer(SAVAGE_ID);
			savage.setAmmo(3);
			savages.add(savage);
		}
		Network.register(server);
		server.addListener(new Listener() {
			public void received (Connection c, Object object) {
				if (object instanceof Join) {
					Player newPlayer = buildPlayer(c.getID());
					players.add(newPlayer);
					sendDescription(newPlayer, "You enter a new void.\n" + newPlayer.getDescription());
					sendInfo(newPlayer);
					return;
				}
				if (object instanceof ActionMessage) {
					String action = ((ActionMessage)object).message;
					Player player = findPlayer(c.getID());
					Log.info(LOG_CATEGORY, "Player " + c.getID() + ": " + action);
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
					for (Player worldmate : players) {
						sendInfo(worldmate);
					}
					server.sendToTCP(player.id, new Wait());
					player.waiting = true;
					player.lastMoveTime = System.currentTimeMillis();
					return;
				}
				if (object instanceof Resume) {
					Player player = findPlayer(c.getID());
					if (player.waiting && System.currentTimeMillis() >= player.lastMoveTime + turnTime) {
						server.sendToTCP(player.id, new Resume());
						player.waiting = false;
					} else if (player.waiting) {
						server.sendToTCP(player.id, new Wait());
						player.waiting = true;
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
		int x = attacker.getX();
		int y = attacker.getY();
		Player defender = null;
		List<Player> savages = getSavagesOn(x, y);
		if (savages.size() > 0) {
			defender = savages.get(0);
		} else {
			List<Player> players = getPlayersOn(x, y);
			for (Player player : players) {
				if (player != attacker) {
					defender = player;
					break;
				}
			}
		}
		if (defender == null) {
			attacker.shoot();
			sendDescription(attacker, NOBODY[rng.nextInt(NOBODY.length)]);
			return;
		}
		boolean attackShot = attacker.getAmmo() > 0;
		boolean defendShot = defender.getAmmo() > 0;
		if (attackShot) {
			attacker.shoot();
		}
		if (defendShot) {
			defender.shoot();
		}
		int attackAdvantage =
			attacker.getAdvantage() + rng.nextInt(4) + ((attackShot) ? (2) : (0));
		int defendAdvantage =
			defender.getAdvantage() + map.getRoom(x, y).getAdvantage() + rng.nextInt(4) + ((defendShot) ? (2) : (0)) +
			((defender.id == SAVAGE_ID) ? (-2) : (0));
		Log.info(LOG_CATEGORY, 
			"Player " + attacker.id + " (" + attackAdvantage + ") against " +
			"Player " + defender.id + " (" + defendAdvantage + ")");
		if (attackAdvantage > defendAdvantage + 2) {
			if (attackShot) {
				sendDescription(attacker, KILL_SHOT[rng.nextInt(KILL_SHOT.length)]);
			} else {
				sendDescription(attacker, KILL_PUNCH[rng.nextInt(KILL_SHOT.length)]);
			}
			sendDescription(defender, DEATH[rng.nextInt(DEATH.length)]);
			attacker.setAmmo(attacker.getAmmo() + defender.getAmmo());
			if (defender.id == SAVAGE_ID) {
				this.savages.remove(defender);
			} else {
				server.sendToTCP(defender.id, new Death());
				players.remove(defender);
			}
			sendInfo(attacker);
		} else if (defendAdvantage > attackAdvantage + 2) {
			if (defendShot) {
				sendDescription(defender, KILL_SHOT[rng.nextInt(KILL_SHOT.length)]);
			} else {
				sendDescription(defender, KILL_PUNCH[rng.nextInt(KILL_SHOT.length)]);
			}
			sendDescription(attacker, DEATH[rng.nextInt(DEATH.length)]);
			defender.setAmmo(attacker.getAmmo() + defender.getAmmo());
			server.sendToTCP(attacker.id, new Death());
			players.remove(attacker);
			sendInfo(defender);
		} else {
			if (attackShot) {
				sendDescription(defender, SHOT_AT[rng.nextInt(SHOT_AT.length)]);
			} else {
				sendDescription(defender, PUNCHED_AT[rng.nextInt(PUNCHED_AT.length)]);
			}
			if (defendShot) {
				sendDescription(attacker, SHOT_AT[rng.nextInt(SHOT_AT.length)]);
			} else {
				sendDescription(attacker, PUNCHED_AT[rng.nextInt(PUNCHED_AT.length)]);
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
		Log.info(LOG_CATEGORY,
			"Player " + player.id + ": move (" + player.getX() + "," + player.getY() + ") to "
			+ "(" + (player.getX() + x) + "," + (player.getY() + y) + ")");
		player.move(x, y);
		List<Player> playersAtDestination = getPlayersOn(player.getX(), player.getY());
		if (playersAtDestination.size() > 1 || getSavagesOn(player.getX(), player.getY()).size() > 0) {
			for (Player roommate : playersAtDestination) {
				sendDescription(roommate, PRESENT[rng.nextInt(PRESENT.length)]);
			}
		}
	}
	
	private List<Player> getListOn(int x, int y, List<Player> list) {
		List<Player> players = new LinkedList<Player>();
		for (Player player : list) {
			if (player.getX() == x && player.getY() == y) {
				players.add(player);
			}
		}
		return players;
	}
	
	public List<Player> getPlayersOn(int x, int y) {
		return getListOn(x, y, players);
	}
	
	public List<Player> getSavagesOn(int x, int y) {
		return getListOn(x, y, savages);
	}
	
	public Player buildPlayer(int id) {
		Player newPlayer = new Player(rng.nextInt(map.size), rng.nextInt(map.size), id, rng);
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
	
	public void sendInfo(Player player) {
		InfoMessage info = new InfoMessage();
		info.message =
			Player.DIRECTIONS[player.getDirection()] +
			"  /  Location: (" + player.getX() + ", " + player.getY() + ")" +
			"  /  Ammunition: " + player.getAmmo() + 
			"  /  Advantage: " + player.getAdvantage() +
			"  /  Room Bonus: " + map.getRoom(player.getX(), player.getY()).getAdvantage() +
			"  /  Humans: " + getPlayersOn(player.getX(), player.getY()).size() + 
			"  /  Savages: " + getSavagesOn(player.getX(), player.getY()).size();
		server.sendToTCP(player.id, info);
	}
	
	public static class ClientConnection extends Connection {
		public String name;
	}
	
	public static void main(String[] args) throws IOException {
		Attribute.build();
		if (args.length == 3) {
			new TAFPSServer(Integer.parseInt(args[0]), Integer.parseInt(args[1]), args[2]);
		} else {
			new TAFPSServer(5, 1000, "TAFPS");
		}
	}
}