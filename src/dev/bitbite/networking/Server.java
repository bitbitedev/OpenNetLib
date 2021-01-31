package dev.bitbite.networking;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public abstract class Server {

	public final int PORT;
	private ServerSocket serverSocket;
	private ClientManager clientManager;
	private ArrayList<ServerListener> listeners;
	
	public Server(int port) {
		this.PORT = port;
		this.clientManager = new ClientManager(this);
		this.clientManager.setName("ClientManager");
		listeners = new ArrayList<ServerListener>();
	}
	
	public boolean start() {
		onStart();
		try {
			this.serverSocket = new ServerSocket(this.PORT);
		} catch(Exception e) {
			onStartFailed(e);
		}
		clientManager.start();
		return true;
	}
	
	protected abstract void processReceivedData(String clientAddress, String data);
	
	public void onStart() {
		this.listeners.forEach(l -> l.onStart());
	}
	
	public void onStartFailed(Exception e) {
		this.listeners.forEach(l -> l.onStartFailed(e));
	}
	
	public void onAccept(Socket clientSocket) {
		this.listeners.forEach(l -> l.onAccept(clientSocket));
	}
	
	public void onAcceptStart() {
		this.listeners.forEach(l -> l.onAcceptStart());
	}
	
	public void onAcceptEnd() {
		this.listeners.forEach(l -> l.onAcceptEnd());
	}
	
	public void onAcceptFailed(Exception e) {
		this.listeners.forEach(l -> l.onAcceptFailed(e));
	}
	
	protected ServerSocket getServerSocket() {
		return this.serverSocket;
	}
}
