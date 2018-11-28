package com.crispico.flower_platform.remote_object.client;

import java.util.ArrayList;
import java.util.List;

import com.crispico.flower_platform.remote_object.shared.FlowerPlatformRemotingProtocolPacket;
import com.crispico.flower_platform.remote_object.shared.IRemoteObjectInitializer;
import com.crispico.flower_platform.remote_object.shared.IRemoteObjectServiceInvoker;
import com.crispico.flower_platform.remote_object.shared.IRequestSender;
import com.crispico.flower_platform.remote_object.shared.IScheduler;
import com.crispico.flower_platform.remote_object.shared.RemoteObject;
import com.crispico.flower_platform.remote_object.shared.ResponseCallback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
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
public class JsRemoteObjectBase implements IRemoteObjectInitializer, IRequestSender, IScheduler, IRemoteObjectServiceInvoker {

	protected Object proxyHandler;
	
	public JsRemoteObjectBase() {
		super();
		createProxyHandler();
		GWT.log("ROI created");
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

	private WebSocket webSocket; 
	
	@Override
	public void sendRequest(String url, String payload, ResponseCallback callback) {
		RequestBuilder rb = new RequestBuilder(RequestBuilder.POST, url);
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

	@Override
	public Object invoke(FlowerPlatformRemotingProtocolPacket packet) {
		String functionCall = packet.nextField();
		List<String> argsStr = new ArrayList<>();
		while (packet.hasMoreFields()) {
			argsStr.add(packet.nextField());
		}
		JavaScriptObject res = jsInvoke(functionCall, argsStr.toArray());
		if (res != null) {
			return res.toString();
		}
		return res;
	}

	private Timer timer;
	
	private final native JavaScriptObject jsInvoke(String functionCall, Object[] args)/*-{
		var splitCall = functionCall.split('.');
		var method = $wnd;
		for (var i in splitCall) {
			method = method[splitCall[i]];
		}	
		return method.apply($wnd, args);
	}-*/;

	@Override
	public void schedule(Runnable task, int millis) {
		clear();
		timer = new Timer() {
			@Override
			public void run() {
				task.run();
			}
		};
		timer.schedule(millis);
	}
	
	@Override
	public void clear() {
		if (timer != null) {
			timer.cancel();
		}
	}
	
}
