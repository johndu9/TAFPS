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
import com.johndu9.tafps.Network.Death;
import com.johndu9.tafps.Network.DescriptionMessage;
import com.johndu9.tafps.Network.InfoMessage;
import com.johndu9.tafps.Network.Join;
import com.johndu9.tafps.Network.Resume;
import com.johndu9.tafps.Network.Wait;

@SuppressWarnings("serial")
public class TAFPSClient extends JFrame implements MouseListener, MouseMotionListener, KeyListener{

	private JTextField infoField = new JTextField("");
	private JTextArea outputArea = new JTextArea("You appear in an empty void. Everything is dark.\n");
	private JScrollPane scroll;
	private int oldX = 0;
	private int addedX = 0;
	private char oldChar = 0;
	private char newChar = 0;
	private final String host;
	private boolean waiting = false;
	private Robot robot;
	
	private Client client;
	
	public TAFPSClient(String server, boolean undecorated) {
		super("TAFPS");
		try {
			robot = new Robot();
		} catch (AWTException e) {
			e.printStackTrace();
		}
		this.host = server;
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setUndecorated(undecorated);
		if (isUndecorated()) {
			setBackground(new Color(0, 0, 0, 128));
		}
        setSize(new Dimension(640, 480));
        setLocationRelativeTo(null);
        getRootPane().putClientProperty("apple.awt.draggableWindowBackground", false);
        setFocusable(true);
        getContentPane().setLayout(new java.awt.BorderLayout(0, 312 + ((isUndecorated()) ? (56) : (0))));
        infoField.setEditable(false);
        infoField.setBackground(new Color(0, 0, 0, 255));
        infoField.setForeground(new Color(255, 255, 255, 255));
        infoField.setBorder(null);
        infoField.setHorizontalAlignment(JTextField.CENTER);
        outputArea.setEditable(false);
        outputArea.setBackground(new Color(0, 0, 0, 255));
        outputArea.setForeground(new Color(255, 255, 255, 255));
        outputArea.setBorder(null);
        outputArea.setLineWrap(true);
        getContentPane().add(infoField, java.awt.BorderLayout.NORTH);
        getContentPane().add(outputArea, java.awt.BorderLayout.CENTER);
        scroll = new JScrollPane(outputArea,JScrollPane.VERTICAL_SCROLLBAR_NEVER,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
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
				if (object instanceof InfoMessage) {
					InfoMessage info = (InfoMessage)object;
					infoField.setText(info.message);
					return;
				}
				if (object instanceof DescriptionMessage) {
					DescriptionMessage description = (DescriptionMessage)object;
					appendln(description.message);
					return;
				}
				if (object instanceof Wait) {
					waiting = true;
					try {
						Thread.sleep(1000);
						client.sendUDP(new Resume());
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					return;
				}
				if (object instanceof Resume) {
					waiting = false;
					return;
				}
				if (object instanceof Death) {
					client.stop();
					infoField.setText("\\ to return to life");
					return;
				}
			}

			public void disconnected (Connection c) {
				appendln("You return to the void.");
			}
		});
		tryReconnect();
	}
	
	public void tryReconnect() {
		while (!client.isConnected()){
			new Thread("Connect") {
				public void run () {
					try {
						client.start();
						client.connect(5000, host, Network.TCPPORT, Network.UDPPORT);
					} catch (IOException e) {
						appendln("Your attempt to regain life proves futile. You will try again later.");
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
		robot.mouseMove(getX() + getWidth() / 2, getY() + getHeight() / 2);
	}
	
	public void append(String string) {
		outputArea.append(string);
        outputArea.setCaretPosition(outputArea.getDocument().getLength());
	}
	
	public void appendln(String string) {
		if (!waiting) {
			append(string + "\n");
		}
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
			if (oldChar == '\\') {
				appendln("You attempt to return to life.");
				client.stop();
				tryReconnect();
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
			appendln("You attack.");
			sendAction("f");
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}
	
	public void sendAction(String action) {
		if (!waiting) {
			ActionMessage act = new ActionMessage();
			act.message = action;
			client.sendUDP(act);
		}
	}
	
    public static void main(String[] args) {
    	String host;
    	boolean undecorated = false;
    	if (args.length == 2) {
    		host = args[0];
    		undecorated = Boolean.parseBoolean(args[1]);
    	} else if (args.length == 1) {
    		host = args[0];
    	} else {
    		host = "localhost";
    	}
    	new TAFPSClient(host, undecorated);
    }
}