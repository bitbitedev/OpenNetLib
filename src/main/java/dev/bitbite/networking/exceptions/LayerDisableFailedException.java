package dev.bitbite.networking.exceptions;

/**
 * Gets thrown if the disabling process of the {@link dev.bitbite.networking.DataProcessingLayer} failed.
 * @version 0.0.1-alpha
 */
public class LayerDisableFailedException extends Exception {
	private static final long serialVersionUID = 1L;
	
	/**
	 * @param message error description
	 */
	public LayerDisableFailedException(String message) {
		super(message);
	}
}
