package dev.bitbite.networking;

import java.net.Socket;
import java.util.ArrayList;

public abstract class Client {

	private final String HOST;
	private final int PORT;
	private Socket socket;
	private IOHandler ioHandler;
	private boolean keepAlive = false;
	private ArrayList<ClientListener> listeners;
	
	enum ActionType {
		CONNECTION,
		CONNECTION_SUCCESS,
		CONNECTION_FAILED,
		CLOSE,
		CLOSE_FAILED,
		CLOSE_SUCCESS
	}
	
	public Client(String host, int port) {
		this.HOST = host;
		this.PORT = port;
	}
	
	public void connect() {
		try {
			notifyListeners(ActionType.CONNECTION);
			this.socket = new Socket(this.HOST, this.PORT);
			this.ioHandler = new IOHandler(this.socket.getInputStream(), this.socket.getOutputStream(), this::processReceivedData);
			if(this.socket.isConnected()) {
				notifyListeners(ActionType.CONNECTION_SUCCESS);
				this.socket.setKeepAlive(this.keepAlive);
			}
		} catch (Exception e) {
			notifyListeners(ActionType.CONNECTION_FAILED);
		}
	}
	
	public boolean close() {
		try {
			notifyListeners(ActionType.CLOSE);
			this.ioHandler.close();
			this.socket.close();
		} catch(Exception e) {
			notifyListeners(ActionType.CLOSE_FAILED);
			return false;
		}
		notifyListeners(ActionType.CLOSE_SUCCESS);
		return true;
	}
	
	protected abstract void processReceivedData(String data);
	
	private void notifyListeners(ActionType type, Object... args) {
		switch(type) {
			case CONNECTION:
				listeners.forEach(l -> l.onConnection());
				break;
			case CONNECTION_SUCCESS:
				listeners.forEach(l -> l.onConnectionSuccess());
				break;
			case CONNECTION_FAILED:
				listeners.forEach(l -> l.onConnectionFailed(args));
				break;
			case CLOSE:
				listeners.forEach(l -> l.onClose());
				break;
			case CLOSE_FAILED:
				listeners.forEach(l -> l.onCloseFailed(args));
				break;
			case CLOSE_SUCCESS:
				listeners.forEach(l -> l.onCloseSuccess());
				break;
			default:
				break;
		}
	}
	
	public boolean isConnected() {
		if(this.socket == null || this.socket.isClosed()) {
			return false;
		}
		return this.socket.isConnected();
	}
	
}
