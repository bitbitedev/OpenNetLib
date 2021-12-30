package dev.bitbite.networking;

public class CommunicationHandlerCloseListener extends IOHandlerListener {

	private CommunicationHandler communicationHandler;
	
	public CommunicationHandlerCloseListener(CommunicationHandler communicationHandler) {
		this.communicationHandler = communicationHandler;
	}
	
	@Override
	public void onCloseEnd() {
		this.communicationHandler.close();
	}

}
