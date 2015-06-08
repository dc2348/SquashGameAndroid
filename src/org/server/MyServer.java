package org.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Set;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A simple WebSocketServer implementation. Keeps track of a "chatroom".
 */
public class MyServer extends WebSocketServer {

	public MyServer(int port) throws UnknownHostException, InterruptedException {
		super(new InetSocketAddress(port));
		WebSocket.DEBUG = true;
	}

	public MyServer(InetSocketAddress address) {
		super(address);
	}

	@Override
	public void onOpen(WebSocket conn, ClientHandshake handshake) {
		// this.sendToAll( "new connection: " +
		// handshake.getResourceDescriptor() );
		// System.out.println(
		// conn.getRemoteSocketAddress().getAddress().getHostAddress() +
		// " entered the room!" );
	}

	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote) {
		// this.sendToAll( conn + " has left the room!" );
		// System.out.println( conn + " has left the room!" );
	}

	@Override
	public void onMessage(WebSocket conn, String message) {
		
	}

	@Override
	public void onError(WebSocket conn, Exception ex) {
		ex.printStackTrace();
	}

	public void sendToAll(String text) {
		Set<WebSocket> con = connections();
		synchronized (con) {
			for (WebSocket c : con) {
				c.send(text);
			}
		}
	}

	
}
