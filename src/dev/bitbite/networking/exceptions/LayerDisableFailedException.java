package dev.bitbite.networking.exceptions;

/**
 * Gets thrown if the disabling process of the {@link DataProcessingLayer} failed.
 * @version 0.0.1-alpha
 */
public class LayerDisableFailedException extends Exception {
	private static final long serialVersionUID = 1L;
	
	/**
	 * @param message
	 */
	public LayerDisableFailedException(String message) {
		super(message);
	}
}
