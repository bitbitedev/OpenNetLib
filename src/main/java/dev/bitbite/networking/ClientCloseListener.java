package dev.bitbite.networking;

import lombok.AllArgsConstructor;

/**
 * This class represents a listener for the close event of a client connection.
 * It will close the client object once the connection is closed.
 */
@AllArgsConstructor
public class ClientCloseListener extends IOHandlerListener {

	/**
	 * private constructor to prevent instantiation without a client object
	 */
	@SuppressWarnings("unused")
	private ClientCloseListener() {}

	private Client client;
	
	/**
	 * Called when the client's close operation has completed.
	 * Closes the client connection.
	 */
	@Override
	public void onCloseEnd() {
		this.client.close();
	}

}