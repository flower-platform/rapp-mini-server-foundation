package com.flowerplatform.rapp_mini_server.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;

/**
 * @author Cristian Spiescu
 */
public class RappMiniServerFoundationClient implements EntryPoint {

	public void onModuleLoad() {
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			@Override
			public void execute() {
				notifyGwtReady();
			}
		});
	}
	
	/**
	 * Notifies anyone who cares that this GWT script has finished loading.
	 * If function is not present, then no call is made (avoid console errors in browser)
	 */
	private native void notifyGwtReady() /*-{
		if ($wnd.onGwtReady) {
			$wnd.onGwtReady();
		}
	}-*/;
}
