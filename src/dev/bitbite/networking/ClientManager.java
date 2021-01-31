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
		this.server.onAcceptStart();
		while(!Thread.currentThread().isInterrupted()) {
			try {
				Socket clientSocket = this.server.getServerSocket().accept();
				CommunicationHandler ch = new CommunicationHandler(clientSocket, this);
				this.communicationHandler.add(ch);
				ch.start();
				this.server.onAccept(clientSocket);
			} catch(Exception e) {
				if(!e.getMessage().contentEquals("Interrupted function call: accept failed")){
					this.server.onAcceptFailed(e);
				}
				if(e.getMessage().contentEquals("Socket is closed")) {
					Thread.currentThread().interrupt();
				}
			}
		}
		this.server.onAcceptEnd();
	}
	
	public void close() {
		Thread.currentThread().interrupt();
		communicationHandler.forEach(ch -> ch.close());
		try {
			server.getServerSocket().close();
		} catch(IOException e) {
			//TODO handle failing cm close
		}
	}
	
	public CommunicationHandler getCommunicationHandlerByIp(String clientAddress) {
		for(CommunicationHandler ch : communicationHandler) {
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
