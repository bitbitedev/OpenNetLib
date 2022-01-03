package dev.bitbite.networking;

public class ClientCloseListener extends IOHandlerListener {

	private Client client;
	
	public ClientCloseListener(Client client) {
		this.client = client;
	}
	
	@Override
	public void onCloseEnd() {
		this.client.close();
	}

}