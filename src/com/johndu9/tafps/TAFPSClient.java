package com.johndu9.tafps;
import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.johndu9.tafps.Network.ActionMessage;
import com.johndu9.tafps.Network.DescriptionMessage;
import com.johndu9.tafps.Network.Join;

@SuppressWarnings("serial")
public class TAFPSClient extends JFrame implements MouseListener, MouseMotionListener, KeyListener{

	private JTextField debug = new JTextField("");
	private JTextArea output = new JTextArea("You appear in an empty void. Everything is dark.\n");
	private JScrollPane scroll;
	private int oldX = 0;
	private int addedX = 0;
	private char oldChar = 0;
	private char newChar = 0;
	private final int width;
	private final int height;
	private final String host;
	private Robot robot;
	
	private Client client;
	
	public TAFPSClient(String server) {
		super("Transparent Window");
		try {
			robot = new Robot();
		} catch (AWTException e) {
			e.printStackTrace();
		}
		this.host = server;
		width = getGraphicsConfiguration().getBounds().width;
		height = getGraphicsConfiguration().getBounds().height;
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setUndecorated(true);
		setBackground(new Color(0, 0, 0, 128));
        setSize(new Dimension(640, 480));
        setLocationRelativeTo(null);
        getRootPane().putClientProperty("apple.awt.draggableWindowBackground", false);
        setFocusable(true);
        getContentPane().setLayout(new java.awt.BorderLayout(0, 360));
        debug.setEditable(false);
        debug.setBackground(new Color(0, 0, 0, 255));
        debug.setForeground(new Color(255, 255, 255, 255));
        debug.setBorder(null);
        output.setEditable(false);
        output.setBackground(new Color(0, 0, 0, 255));
        output.setForeground(new Color(255, 255, 255, 255));
        output.setBorder(null);
        output.setLineWrap(true);
        getContentPane().add(debug, java.awt.BorderLayout.NORTH);
        getContentPane().add(output, java.awt.BorderLayout.CENTER);
        scroll = new JScrollPane(output,JScrollPane.VERTICAL_SCROLLBAR_NEVER,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        getContentPane().add(scroll);
        setVisible(true);
		addMouseMotionListener(this);
		addMouseListener(this);
		addKeyListener(this);
		
		client = new Client();
		client.start();
		Network.register(client);

		client.addListener(new Listener() {
			public void connected (Connection c) {
				Join join = new Join();
				client.sendTCP(join);
			}

			public void received (Connection c, Object object) {
				if (object instanceof DescriptionMessage) {
					DescriptionMessage description = (DescriptionMessage)object;
					appendln(description.message);
					return;
				}
			}

			public void disconnected (Connection c) {
				appendln("You return to the void.");
			}
		});
		while (!client.isConnected()){
			new Thread("Connect") {
				public void run () {
					try {
						client.connect(5000, host, Network.TCPPORT, Network.UDPPORT);
					} catch (IOException e) {
						System.out.println("Could not connect.");
						System.out.println("Attempting to reconnect in 5 seconds.");
					}
				}
			}.start();
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		mouseMoved(e);
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		addedX += e.getX() - 320;
		if (addedX > oldX + 320) {
			oldX = addedX;
			appendln("You turn right.");
			sendAction("tr");
		} else if (addedX < oldX - 320) {
			oldX = addedX;
			appendln("You turn left.");
			sendAction("tl");
		}
		robot.mouseMove(width / 2, height / 2);
	}
	
	public void append(String string) {
		output.append(string);
        output.setCaretPosition(output.getDocument().getLength());
	}
	
	public void appendln(String string) {
		append(string + "\n");
	}

	@Override
	public void keyPressed(KeyEvent e) {
	}

	@Override
	public void keyReleased(KeyEvent e) {
		oldChar = 0;
	}

	@Override
	public void keyTyped(KeyEvent e) {
		newChar = e.getKeyChar();
		if (newChar != oldChar) {
			oldChar = newChar;
			if (oldChar == 'w') {
				appendln("You walk forward.");
				sendAction("mf");
			}
			if (oldChar == 'a') {
				appendln("You walk left.");
				sendAction("ml");
			}
			if (oldChar == 's') {
				appendln("You walk backward.");
				sendAction("mb");
			}
			if (oldChar == 'd') {
				appendln("You walk right.");
				sendAction("mr");
			}
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (e.getButton() == 1) {
			appendln("You open fire.");
			sendAction("f");
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}
	
	public void sendAction(String action) {
		ActionMessage act = new ActionMessage();
		act.message = action;
		client.sendUDP(act);
	}
	
    public static void main(String[] args) {
    	String host;
    	if (args.length == 1) {
    		host = args[0];
    	} else {
    		host = "localhost";
    	}
    	new TAFPSClient(host);
    }
}