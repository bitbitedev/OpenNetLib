package dev.bitbite.networking;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;

public class ClientManager extends Thread{

	private final Server server;
	private CopyOnWriteArrayList<CommunicationHandler> communicationHandler;
	
	public ClientManager(Server server) {
		this.server = server;
		this.communicationHandler = new CopyOnWriteArrayList<CommunicationHandler>();
	}
	
	@Override
	public void run() {
		this.server.notifyListeners(Server.EventType.ACCEPT_START);
		while(!Thread.currentThread().isInterrupted()) {
			try {
				Socket clientSocket = this.server.getServerSocket().accept();
				CommunicationHandler ch = new CommunicationHandler(clientSocket, this);
				this.communicationHandler.add(ch);
				ch.start();
				this.server.notifyListeners(Server.EventType.ACCEPT, ch);
			} catch(Exception e) {
				if(!e.getMessage().contentEquals("Interrupted function call: accept failed")){
					this.server.notifyListeners(Server.EventType.ACCEPT_FAILED, e);
				}
				if(e.getMessage().contentEquals("Socket is closed")) {
					Thread.currentThread().interrupt();
				}
			}
		}
		this.server.notifyListeners(Server.EventType.ACCEPT_END);
	}
	
	public boolean close() {
		this.server.notifyListeners(Server.EventType.CLOSE);
		Thread.currentThread().interrupt();
		this.communicationHandler.forEach(ch -> ch.close());
		try {
			this.server.getServerSocket().close();
		} catch(IOException e) {
			this.server.notifyListeners(Server.EventType.CLOSE_FAILED, e);
			return false;
		}
		this.server.notifyListeners(Server.EventType.CLOSE_SUCCESS);
		return true;
	}
	
	public CommunicationHandler getCommunicationHandlerByIp(String clientAddress) {
		for(CommunicationHandler ch : this.communicationHandler) {
			if(ch.getIP().equals(clientAddress)) {
				return ch;
			}
		}
		return null;
	}
	
	public Server getServer() {
		return this.server;
	}
}
