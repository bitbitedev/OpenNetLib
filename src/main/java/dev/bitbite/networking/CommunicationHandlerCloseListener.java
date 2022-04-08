package dev.bitbite.networking;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CommunicationHandlerCloseListener extends IOHandlerListener {

	private CommunicationHandler communicationHandler;
	
	@Override
	public void onCloseEnd() {
		this.communicationHandler.close();
	}

}
