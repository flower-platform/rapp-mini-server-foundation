package com.flowerplatform.rapp_mini_server.client;

import com.flowerplatform.rapp_mini_server.shared.AbstractRemoteObjectInitializer;
import com.flowerplatform.rapp_mini_server.shared.IRequestSender;
import com.flowerplatform.rapp_mini_server.shared.IScheduler;
import com.flowerplatform.rapp_mini_server.shared.RemoteObject;
import com.flowerplatform.rapp_mini_server.shared.ResponseCallback;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Timer;

import jsinterop.annotations.JsType;

/**
 * A JS/GWT RemoteObject is a JS Proxy that contains an actual {@link RemoteObject}. The proxy
 * intercepts method calls and delegate to the {@link RemoteObject}.
 * 
 * @author Cristian Spiescu
 */
@JsType(namespace="rapp_mini_server")
public class JsRemoteObjectInitializer extends AbstractRemoteObjectInitializer implements IRequestSender, IScheduler {

	protected Object proxyHandler;
	
	public JsRemoteObjectInitializer() {
		super();
		createProxyHandler();
	}
	
	protected native void createProxyHandler()/*-{
		this.proxyHandler = { 
			apply: function(target, thisArg, argumentsList) {
				console.log(target);
				console.log(thisArg);
				console.log(argumentsList);
			}, 			
			get: function(target, name) {
				// bypass logic for invokeMethod
				if (name == 'invokeMethod') {
					return function() {
						target.delegate[name].apply(target.delegate, arguments);
					};
				};
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
	public void sendRequest(String url, String payload, ResponseCallback callback) {
		RequestBuilder rb = new RequestBuilder(RequestBuilder.POST, url);
		log(url);
		try {
			rb.sendRequest(payload, new RequestCallback() {
				@Override
				public void onResponseReceived(Request request, Response response) {
					if (response.getStatusCode() == Response.SC_OK) {
						callback.onSuccess(response.getText());
					} else {
						callback.onError("HTTP status code: " + response.getStatusCode());
					}
				}
				
				@Override
				public void onError(Request request, Throwable exception) {
					callback.onError(exception.toString());
				}
			});
		} catch (RequestException e) {
			throw new RuntimeException(e);
		}
	};
	
	public static native void log(String s)/*-{
		console.log(s);
	}-*/;

	@Override
	public void schedule(Runnable task, int millis) {
		final Timer timer = new Timer() {
			@Override
			public void run() {
				task.run();
			}
		};
		timer.schedule(millis);
	}
	
}
