package dev.bitbite.networking;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Administrates the clients an runs in its own Thread. It accepts them from the serversocket and
 * starts a {@link CommunicationHandler} in a separate thread for each connecting client.<br>
 * 
 * @see CommunicationHandler
 * 
 * @version 0.0.1-alpha
 */
public class ClientManager extends Thread{

	private final Server server;
	private CopyOnWriteArrayList<CommunicationHandler> communicationHandler;
	
	/**
	 * Initiates a ClientManager object
	 * @param server that it should manage clients for
	 */
	public ClientManager(Server server) {
		this.server = server;
		this.communicationHandler = new CopyOnWriteArrayList<CommunicationHandler>();
	}
	
	/**
	 * Starts the process of accepting clients to the server. For each client that is accepted
	 * a {@link CommunicationHandler} is started in a separate Thread.
	 * 
	 * @see CommunicationHandler
	 * 
	 * @version 0.0.1-alpha
	 */
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
	
	/**
	 * Closes all client connection and the serversocket itself
	 * @return true if the closing process finishes successfully
	 * 
	 * @version 0.0.1-alpha
	 */
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
		this.server.notifyListeners(Server.EventType.CLOSE_END);
		return true;
	}
	
	/**
	 * Searches for a {@link CommunicationHandler} by its sockets remote socket address and
	 * returns it. If no CommunicationHandler with that address it will return <code>null</code>.
	 * 
	 * @param clientAddress the IP address of the socket related to the CommunicationHandler to look for
	 * 
	 * @return the communicationhandler or null
	 */
	public CommunicationHandler getCommunicationHandlerByIP(String clientAddress) {
		for(CommunicationHandler ch : this.communicationHandler) {
			if(ch.getIP().equals(clientAddress)) {
				return ch;
			}
		}
		return null;
	}
	
	/**
	 * Returns the server object related to this clientmanager
	 * @return the serverobject
	 */
	public Server getServer() {
		return this.server;
	}
}
