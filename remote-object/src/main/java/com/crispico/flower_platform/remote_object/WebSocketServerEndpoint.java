package com.crispico.flower_platform.remote_object;

import static java.lang.String.format;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/remoteObjectWs")
public class WebSocketServerEndpoint {

    static Map<String, RemoteObjectWebSocketHandler> connectedClients = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session) {
        System.out.println(format("%s connected.", session.getId()));
        RemoteObjectWebSocketHandler handler = new RemoteObjectWebSocketHandler(session);
        connectedClients.put(session.getId(), handler);
    }

    @OnMessage
    public void onMessage(String message, Session session) throws IOException, EncodeException {
    	RemoteObjectWebSocketHandler handler = connectedClients.get(session.getId());
    	if (handler != null) {
    		handler.onWebSocketText(message);
    	}
    }

    @OnClose
    public void onClose(Session session) throws IOException, EncodeException {
        System.out.println(format("%s disconnected.", session.getId()));
        RemoteObjectWebSocketHandler handler = connectedClients.remove(session.getId());
        if (handler != null) {
        	handler.onWebSocketClose();
        }
    }

}