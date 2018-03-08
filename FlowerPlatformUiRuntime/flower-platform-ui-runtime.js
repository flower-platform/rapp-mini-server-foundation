var gwtReadyListeners;
function onGwtReady() {
	window["gwtReady"] = true;
	if (gwtReadyListeners) {
		for (var i = 0; i < gwtReadyListeners.length; i++) {
			gwtReadyListeners[i]();
		}
	}
}

var addGwtReadyListener = function(gwtReadyListener) {
	if (!gwtReadyListeners) {
		gwtReadyListeners = [];
	}
	gwtReadyListeners.push(gwtReadyListener);
}

var RemoteObjectRegistry = function(defineRemoteObjects) {
	var remoteObjectRegistry = {
			setRemoteAddress: function(value) {
				remoteObjectRegistry.remoteAddress = value;
				return this;
			}, 
			setSecurityToken: function(value) {
				remoteObjectRegistry.securityToken = value;
				return this;
			},
			setLocalNodeId: function(value) {
				remoteObjectRegistry.localNodeId = value;
				return this;
			},
			setNodeId: function(value) {
				remoteObjectRegistry.nodeId = value;
				return this;
			},
			addRemoteObject: function(name, remoteObject) {
				if (!remoteObject.getRemoteAddress()) {
					remoteObject.setRemoteAddress(remoteObjectRegistry.remoteAddress);
				}
				if (!remoteObject.getSecurityToken()) {
					remoteObject.setSecurityToken(remoteObjectRegistry.securityToken);
				}
				if (!remoteObject.getNodeId()) {
					remoteObject.setNodeId(remoteObjectRegistry.nodeId);
				}
				
				// Memorize the initialized remoteObject
				if (remoteObjectRegistry.remoteObjects === undefined) {
					remoteObjectRegistry.remoteObjects = {};
				}
				if(remoteObjectRegistry.remoteObjectBase === undefined) {
					remoteObjectRegistry.remoteObjectBase = new rapp_mini_server.JsRemoteObjectBase();
				}

				// a = instanceName
				remoteObjectRegistry.remoteObjects[name] = remoteObjectRegistry.remoteObjectBase.initialize(remoteObject);
				return this;
			},
			startRemoteObjectWebSocketConnection: function(name, wsConnection, callback) {
				if (!wsConnection.getRemoteAddress()) {
					wsConnection.setRemoteAddress(remoteObjectRegistry.remoteAddress);
				}
				if (!wsConnection.getSecurityToken()) {
					wsConnection.setSecurityToken(remoteObjectRegistry.securityToken);
				}
				if (!wsConnection.getLocalNodeId()) {
					wsConnection.setLocalNodeId(remoteObjectRegistry.localNodeId);
				}

				if (remoteObjectRegistry.webSocketConnections === undefined) {
					remoteObjectRegistry.webSocketConnections = {};
				}
				if(remoteObjectRegistry.remoteObjectBase === undefined) {
					remoteObjectRegistry.remoteObjectBase = new rapp_mini_server.JsRemoteObjectBase();
				}

				wsConnection.setServiceInvoker(remoteObjectRegistry.remoteObjectBase);
				remoteObjectRegistry.webSocketConnections[name] = wsConnection;
		        wsConnection.start(callback);
		        
		        return this;
			}
	};

	var proxyHandler = {
			get: function(remoteObjectRegistry, name, args) {
				if (name in remoteObjectRegistry) {
					// For the already existing methods: setRemoteAddress, setSecurityToken, setNodeId, addRemoteObject
					// just apply them (no proxy code)
					if (typeof target[name] === "function") {
						return function(...args) {
							return target[name].apply(this, args);
						};
					} else {
						return target[name];
					}
				} else {
					// Proxy code
					if (typeof window["gwtReady"] !== 'undefined') {
						// Return from the memorized remote objects
						if (remoteObjectRegistry.remoteObjects && remoteObjectRegistry.remoteObjects[name]) {
							return remoteObjectRegistry.remoteObjects[name];
						} else if (remoteObjectRegistry.webSocketConnections && remoteObjectRegistry.webSocketConnections[name]) {
							return remoteObjectRegistry.webSocketConnections[name];
						} else {
							throw new Error("Remote object (" + name + ") was not configured");
						}
					} else {
						// Return a proxy pending remote object that memorizes the methods calls for calling them later when gwt initialized
						if (!remoteObjectRegistry.pendingRemoteObjects) {
							remoteObjectRegistry.pendingRemoteObjects = [];
						}
						
						var pendingRemoteObject;
						for (var i = 0; i < remoteObjectRegistry.pendingRemoteObjects.length; i++) {
							if (remoteObjectRegistry.pendingRemoteObjects[i].name == name) {
								pendingRemoteObject = remoteObjectRegistry.pendingRemoteObjects[i];
								break;
							}
						}
						
						if (!pendingRemoteObject) {
							// Create and memorize the pendingRemoteObject
							pendingRemoteObject = {name:name};
							remoteObjectRegistry.pendingRemoteObjects.push(pendingRemoteObject);
						}
						
						var pendingRemoteObjectProxyHandler = {
							get: function(pendingRemoteObject, methodName, methodArgs) {
								// For the already existing fields(name)
								// just return them (no proxy code)
								if (name in remoteObjectRegistry) {
									if (typeof target[name] === "function") {
										return function(...args) {
											return target[name].apply(this, args);
										};
									} else {
										return target[name];
									}
								}
								
								return function(...args) {
									// Proxy code: memorize method calls
									if (!pendingRemoteObject.pendingInvocations) {
										pendingRemoteObject.pendingInvocations = [];
									}
									pendingRemoteObject.pendingInvocations.push({name:methodName, args:args});
								}
							}
						}
						var pendingRemoteObjectProxy = new Proxy(pendingRemoteObject, pendingRemoteObjectProxyHandler);
						return pendingRemoteObjectProxy;
					}
				}
			}
	};
	
	var  remoteObjectRegistryProxy = new Proxy(remoteObjectRegistry, proxyHandler);
	
	addGwtReadyListener(function(){
		defineRemoteObjects(remoteObjectRegistry);
		
		
		
		if (remoteObjectRegistry.pendingRemoteObjects) {
			for (var i = 0; i < remoteObjectRegistry.pendingRemoteObjects.length; i++) {
				var pendingRemoteObject = remoteObjectRegistry.pendingRemoteObjects[i];
				if (!remoteObjectRegistry.remoteObjects[pendingRemoteObject.name]) {
					throw new Error("Remote object (" + name + ") was not configured");
				}
				var remoteObject = remoteObjectRegistry.remoteObjects[pendingRemoteObject.name];
				for (var j = 0; j < pendingRemoteObject.pendingInvocations.length; j++) {
					var pendingInvocation = pendingRemoteObject.pendingInvocations[j];
					remoteObject[pendingInvocation.name].apply(remoteObject, pendingInvocation.args);
				}
			}
			remoteObjectRegistry.pendingRemoteObjects = [];
		}
	});
	
	// Get current path
	var libFolder;
	var scripts = document.getElementsByTagName('script');
	var libName = 'flower-platform-ui-runtime'; 
    if (scripts && scripts.length > 0) {
        for (var i in scripts) {
            if (scripts[i].src && scripts[i].src.match(new RegExp(libName+'\\.js$'))) {
            	libFolder = scripts[i].src.replace(new RegExp('(.*)'+libName+'\\.js$'), '$1');
            }
        }
    }
    
	var js = document.createElement("script");
	js.type = "text/javascript";
	js.src = libFolder + "FlowerPlatformUiRuntime.nocache.js";
	document.body.appendChild(js);
	
	return remoteObjectRegistryProxy;
};