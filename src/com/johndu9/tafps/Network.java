package com.johndu9.tafps;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.EndPoint;

public class Network {

	public static final int TCPPORT = 51978;
	public static final int UDPPORT = 52978;
	
	public static void register(EndPoint endPoint) {
		Kryo kryo = endPoint.getKryo();
		kryo.register(Join.class);
		kryo.register(Wait.class);
		kryo.register(Resume.class);
		kryo.register(ActionMessage.class);
		kryo.register(DescriptionMessage.class);
		kryo.register(InfoMessage.class);
	}
	
	public static class Join {
	}

	public static class Wait {
	}
	
	public static class Resume {
	}
	
	public static class ActionMessage {
		public String message;
	}
	
	public static class DescriptionMessage {
		public String message;
	}
	
	public static class InfoMessage {
		public String message;
	}
	
}