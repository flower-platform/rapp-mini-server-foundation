package com.flowerplatform.rapp_mini_server.shared;

/**
 * 
 * @author Claudiu Matei
 */
public interface IScheduler {

	public void schedule(Runnable task, int millis);
	
}
