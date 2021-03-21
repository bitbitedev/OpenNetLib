package dev.bitbite.networking.exceptions;

/**
 * Gets thrown if the initialization of the {@link DataProcessingLayer} failed.
 * @version 0.0.1-alpha
 */
public class LayerInitFailedException extends Exception {
	private static final long serialVersionUID = 1L;
	
	/**
	 * @param message
	 */
	public LayerInitFailedException(String message) {
		super(message);
	}
}
