package com.flowerplatform.rapp_mini_server.shared;

/**
 * A {@link RemoteObject} needs a {@link IRequestSender} that deals with
 * the network stack. There is an implementation for each platform: Java and one for JS/GWT. As
 * we didn't want to introduce injection (that would slow down a bit startup time
 * for Java/Raspberry), we introduced this class, whose main purpose is to set
 * a platform dependent {@link IRequestSender}, to the {@link RemoteObject}.
 * 
 * @author Cristian Spiescu
 */
public abstract class AbstractRemoteObjectInitializer {

	public abstract <T extends RemoteObject> T initialize(T remoteObject);
	
}
