package dev.bitbite.networking;

public abstract class ServerListener {

	public void onStart() {}
	public void onStartFailed(Object[] e) {}
	public void onStartSuccess() {}
	
	public void onAccept(Object[] e) {}
	public void onAcceptStart() {}
	public void onAcceptEnd() {}
	public void onAcceptFailed(Object[] e) {}
	public void onClose() {}
	public void onCloseSuccess() {}
	public void onCloseFailed(Object[] args) {}
	
}
