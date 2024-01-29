package dev.bitbite.networking;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ClientCloseListener extends IOHandlerListener {

	private Client client;
	
	@Override
	public void onCloseEnd() {
		this.client.close();
	}

}