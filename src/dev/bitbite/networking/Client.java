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
	
	public Client(String host, int port) {
		this.HOST = host;
		this.PORT = port;
	}
	
	public void connect() {
		try {
			this.socket = new Socket(this.HOST, this.PORT);
			this.ioHandler = new IOHandler(this.socket.getInputStream(), this.socket.getOutputStream(), this::processReceivedData);
			if(this.socket.isConnected()) {
				onConnectionSuccess();
				this.socket.setKeepAlive(this.keepAlive);
			}
		} catch (Exception e) {
			onConnectionFailed(e);
		}
	}
	
	public boolean close() {
		try {
			onClose();
			this.ioHandler.close();
			this.socket.close();
		} catch(Exception e) {
			onCloseFailed(e);
			return false;
		}
		return true;
	}
	
	protected void onClose() {}
	protected abstract void processReceivedData(String data);
	
	private void onConnectionSuccess() {
		this.listeners.forEach(l -> l.onConnectionSuccess());
	}
	
	private void onConnectionFailed(Exception e) {
		this.listeners.forEach(l -> l.onConnectionFailed(e));
	}
	
	private void onCloseFailed(Exception e) {
		this.listeners.forEach(l -> l.onConnectionFailed(e));
	}
	
	public boolean isConnected() {
		if(this.socket == null || this.socket.isClosed()) {
			return false;
		}
		return this.socket.isConnected();
	}
	
}
