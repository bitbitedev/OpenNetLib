package dev.bitbite.networking.exceptions;

/**
 * Gets thrown if the initialization of the {@link dev.bitbite.networking.DataProcessingLayer} failed.
 * @version 0.0.1-alpha
 */
public class LayerInitFailedException extends Exception {
	private static final long serialVersionUID = 1L;
	
	/**
	 * Creates a new LayerInitFailedException
	 * @param message error description
	 */
	public LayerInitFailedException(String message) {
		super(message);
	}
}
