package dev.bitbite.networking;

/**
 * Represents a single data processing layer. Incoming data will be processed 
 * by {@link #process(byte[])} and its result will be returned.
 * 
 * @version 0.0.2-alpha
 */
@FunctionalInterface
public interface DataProcessingLayer {

	/**
	 * Processes incoming data
	 * @param data to process
	 * @return processed data
	 */
	public byte[] process(byte[] data);
	
	/**
	 * Gets called on {@link Server#start()} to initialize the {@link DataProcessingLayer}.
	 * @return true on success
	 */
	default boolean onEnable() { return true; }
	
	/**
	 * Gets called on {@link Server#close()} to disable the {@link DataProcessingLayer}.
	 * @return true on success
	 */
	default boolean onDisable() { return true; }
}
