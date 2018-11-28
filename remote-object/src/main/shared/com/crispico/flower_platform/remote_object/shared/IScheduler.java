package com.crispico.flower_platform.remote_object.shared;

/**
 * 
 * @author Claudiu Matei
 */
public interface IScheduler {

	public void schedule(Runnable task, int millis);

	public void clear(); 
	
}
