package com.crispico.java;

/**
 * Callback similar to {@link Runnable} that takes a param and returns a value.
 * 
 * @author Cristian Spiescu
 * @param <R> The type of the result. Can be {@link Void} if not used.
 * @param <P> The type of the parameter. Can be {@link Void} if not used.
 */
public interface RunnableWithParam<R, P> {
	/**
	 *@author see class
	 **/
	R run(P param);

}
