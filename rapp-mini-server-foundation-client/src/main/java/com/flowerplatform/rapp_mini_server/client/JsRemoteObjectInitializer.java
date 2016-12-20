package com.flowerplatform.rapp_mini_server.client;

import com.flowerplatform.rapp_mini_server.shared.AbstractRemoteObjectInitializer;
import com.flowerplatform.rapp_mini_server.shared.IRequestSender;
import com.flowerplatform.rapp_mini_server.shared.RemoteObject;
import com.flowerplatform.rapp_mini_server.shared.ResultCallback;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;

import jsinterop.annotations.JsType;

/**
 * A JS/GWT RemoteObject is a JS Proxy that contains an actual {@link RemoteObject}. The proxy
 * intercepts method calls and delegate to the {@link RemoteObject}.
 * 
 * @author Cristian Spiescu
 */
@JsType(namespace="rapp_mini_server")
public class JsRemoteObjectInitializer extends AbstractRemoteObjectInitializer implements IRequestSender {

	protected Object proxyHandler;
	
	public JsRemoteObjectInitializer() {
		super();
		createProxyHandler();
	}
	
	protected native void createProxyHandler()/*-{
		this.proxyHandler = { 
			get: function(target, name) {
				return function() {
					// NOTE: in GWT using ...args didn't work, thus we use "arguments"
					// convert to array (from an object of type Arguments)
					var argsArray = Array.from(arguments);
					var callback = null;
					if (argsArray.length > 0) {
						// do we have a callback?
						callback = argsArray[argsArray.length - 1];
						if (typeof callback === "function") {
							// ... yes, so remove it from the array of args
							argsArray.splice(argsArray.length - 1, 1);
						} else {
							// ... nope
							callback = null;
						}
					} 
					target.delegate.invokeMethod(name, argsArray, callback);
				}
			}
		};
	}-*/;
	
	@Override
	public native <T extends RemoteObject> T initialize(T remoteObject)/*-{
		remoteObject.setRequestSender(this);
		return new Proxy({
			delegate: remoteObject,
			p: this.proxyHandler
		}, this.proxyHandler);
	}-*/;

	@Override
	public void sendRequest(String url, String payload, ResultCallback callback) {
		RequestBuilder rb = new RequestBuilder(RequestBuilder.POST, url);
		log(url);
		try {
			rb.sendRequest(payload, new RequestCallback() {
				@Override
				public void onResponseReceived(Request request, Response response) {
					if (response.getStatusCode() == Response.SC_OK) {
						callback.run(response.getText());
					}
				}
				
				@Override
				public void onError(Request request, Throwable exception) {
					
				}
			});
		} catch (RequestException e) {
			throw new RuntimeException(e);
		}
	};
	
	public native void log(String s)/*-{
		console.log(s);
	}-*/;

}
