package com.flowerplatform.rapp_mini_server.client;

import com.google.gwt.core.client.JavaScriptObject;

public class WebSocket extends JavaScriptObject {

	protected WebSocket() {

	}

	public interface Listener {

		void onOpen(JavaScriptObject event);

		void onClose(JavaScriptObject event);

		void onMessage(String data);

		void onError(JavaScriptObject error);
	}

	public static native WebSocket create(String url, Listener callback) /*-{
		var ws = new $wnd.WebSocket(url);
		ws.onopen = $entry(function(e) {
			callback.@com.flowerplatform.rapp_mini_server.client.WebSocket.Listener::onOpen(*)(e)
		});
		ws.onclose = $entry(function(e) {
			callback.@com.flowerplatform.rapp_mini_server.client.WebSocket.Listener::onClose(*)(e)
		});
		ws.onmessage = $entry(function(e) {
			callback.@com.flowerplatform.rapp_mini_server.client.WebSocket.Listener::onMessage(Ljava/lang/String;)(e.data);
		});
		ws.onerror = $entry(function(e) {
			callback.@com.flowerplatform.rapp_mini_server.client.WebSocket.Listener::onError(*)(e)
		});
		return ws;
	}-*/;

	public native final void sendMessage(String message) /*-{
		try {
			this.send(message);
		} catch (e) {
			this.onerror(e);
		}
	}-*/;

	public native final void close() /*-{
		this.close();
	}-*/;

}