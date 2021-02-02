package dev.bitbite.networking;

public abstract class ClientListener {

	public void onConnection() {}
	public void onConnectionSuccess() {}
	public void onConnectionFailed(Object[] e) {}
	
	public void onClose() {}
	public void onCloseFailed(Object[] e) {}
	public void onCloseSuccess() {}
	
}
