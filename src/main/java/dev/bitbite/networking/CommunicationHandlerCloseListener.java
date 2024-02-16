package dev.bitbite.networking;

import lombok.AllArgsConstructor;

/**
 * This class is a listener for the close event of a CommunicationHandler.
 * It will close the CommunicationHandler once the connection is closed.
 */
@AllArgsConstructor
public class CommunicationHandlerCloseListener extends IOHandlerListener {

	/**
	 * private constructor to prevent instantiation without a CommunicationHandler object
	 */
	private CommunicationHandlerCloseListener() {}

	private CommunicationHandler communicationHandler;
	
	/**
	 * This method is called when the close operation is completed.
	 * It closes the communication handler.
	 */
	@Override
	public void onCloseEnd() {
		this.communicationHandler.close();
	}

}
