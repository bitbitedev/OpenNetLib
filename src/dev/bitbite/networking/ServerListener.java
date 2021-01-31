package dev.bitbite.networking;

import java.net.Socket;

public abstract class ServerListener {

	public void onStart() {}
	public void onStartFailed(Exception e) {}
	public void onStartSuccess() {}
	
	public void onAccept(Socket clientSocket) {}
	public void onAcceptStart() {}
	public void onAcceptEnd() {}
	public void onAcceptFailed(Exception e) {}
	
}
