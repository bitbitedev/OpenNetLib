package dev.bitbite.networking;

public abstract class ClientListener {

	public abstract void onConnectionSuccess();
	public abstract void onConnectionFailed(Exception e);
	
}
