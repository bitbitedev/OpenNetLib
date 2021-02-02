package dev.bitbite.networking;

import java.net.ServerSocket;
import java.util.ArrayList;

public abstract class Server {

	public final int PORT;
	private ServerSocket serverSocket;
	private ClientManager clientManager;
	private ArrayList<ServerListener> listeners;
	
	enum ActionType {
		START,
		START_SUCCESS,
		START_FAILED,
		ACCEPT,
		ACCEPT_START,
		ACCEPT_END,
		ACCEPT_FAILED,
		CLOSE,
		CLOSE_SUCCESS,
		CLOSE_FAILED
	}
	
	public Server(int port) {
		this.PORT = port;
		this.clientManager = new ClientManager(this);
		this.clientManager.setName("ClientManager");
		this.listeners = new ArrayList<ServerListener>();
	}
	
	public boolean start() {
		notifyListeners(ActionType.START);
		try {
			this.serverSocket = new ServerSocket(this.PORT);
		} catch(Exception e) {
			notifyListeners(ActionType.START_FAILED);
		}
		this.clientManager.start();
		notifyListeners(ActionType.START_SUCCESS);
		return true;
	}
	
	protected abstract void processReceivedData(String clientAddress, String data);
	
	protected void notifyListeners(ActionType type, Object... args) {
		switch(type) {
			case START:
				this.listeners.forEach(l -> l.onStart());
				break;
			case START_SUCCESS:
				this.listeners.forEach(l -> l.onStartSuccess());
				break;
			case START_FAILED:
				this.listeners.forEach(l -> l.onStartFailed(args));
				break;
			case ACCEPT:
				this.listeners.forEach(l -> l.onAccept(args));
				break;
			case ACCEPT_END:
				this.listeners.forEach(l -> l.onAcceptEnd());
				break;
			case ACCEPT_START:
				this.listeners.forEach(l -> l.onAcceptStart());
				break;
			case ACCEPT_FAILED:
				this.listeners.forEach(l -> l.onAcceptFailed(args));
				break;
			case CLOSE:
				this.listeners.forEach(l -> l.onClose());
				break;
			case CLOSE_SUCCESS:
				this.listeners.forEach(l -> l.onCloseSuccess());
				break;
			case CLOSE_FAILED:
				this.listeners.forEach(l -> l.onCloseFailed(args));
				break;
		}
	}
	
	protected ServerSocket getServerSocket() {
		return this.serverSocket;
	}
}