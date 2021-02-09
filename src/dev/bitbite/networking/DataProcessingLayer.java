package dev.bitbite.networking;

/**
 * Represents a single data processing layer. Incoming data will be processed 
 * by {@link #process(String)} and its result will be returned.
 * 
 * @version 0.0.1-alpha
 */
@FunctionalInterface
public interface DataProcessingLayer {

	/**
	 * Processes incoming data
	 * @param data to process
	 * @return processed data
	 */
	public String process(String data);
	
}
