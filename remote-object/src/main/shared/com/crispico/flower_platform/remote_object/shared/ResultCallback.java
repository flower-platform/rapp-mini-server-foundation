package com.crispico.flower_platform.remote_object.shared;

import jsinterop.annotations.JsFunction;

/**
 * @author Cristian Spiescu
 */
@JsFunction
public interface ResultCallback {

	void run(Object result);
	
}
