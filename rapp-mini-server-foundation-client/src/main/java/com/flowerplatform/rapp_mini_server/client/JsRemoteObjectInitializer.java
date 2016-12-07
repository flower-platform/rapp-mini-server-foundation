package com.flowerplatform.rapp_mini_server.client;

import com.flowerplatform.rapp_mini_server.shared.AbstractRemoteObjectInitializer;
import com.flowerplatform.rapp_mini_server.shared.IRequestSender;
import com.flowerplatform.rapp_mini_server.shared.RemoteObject;

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
	public native void sendRequest(String url, String payload)/*-{
		console.log(payload);
	}-*/;
}
