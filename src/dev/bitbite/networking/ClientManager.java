package dev.bitbite.networking;

import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Administrates the clients an runs in its own Thread. It accepts them from the serversocket and
 * starts a {@link CommunicationHandler} in a separate thread for each connecting client.<br>
 * 
 * @see CommunicationHandler
 */
public class ClientManager extends Thread {

	private final Server server;
	private Thread readThread;
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
	 */
	@Override
	public void run() {
		this.server.notifyListeners(Server.EventType.ACCEPT_START);
		this.readThread = new Thread(()->{
			while(!readThread.isInterrupted()) {
				this.communicationHandler.forEach(ch -> ch.getIOHandler().read());
			}
		});
		readThread.setName("Data reader");
		readThread.start();
		while(!Thread.currentThread().isInterrupted()) {
			if(this.server.getServerSocket().isClosed()) {
				Thread.currentThread().interrupt();
				continue;
			}
			Socket clientSocket = null;
			try {
				clientSocket = this.server.getServerSocket().accept();
				if(clientSocket == null) continue;
				CommunicationHandler ch = new CommunicationHandler(clientSocket, this);
				ch.registerListener(this.server.getIOHandlerListeners());
				this.communicationHandler.add(ch);
				this.server.notifyListeners(Server.EventType.ACCEPT, ch);
			} catch(SocketTimeoutException e) {
				continue;
			} catch(Exception e) {
				if(e.getMessage() != null && e.getMessage().contentEquals("Socket operation on nonsocket: configureBlocking")) {
					Thread.currentThread().interrupt();
					continue;
				}
				if(e.getMessage() == null || !e.getMessage().contentEquals("Interrupted function call: accept failed")){
					this.server.notifyListeners(Server.EventType.ACCEPT_FAILED, e);
				}
				if(e.getMessage() != null && e.getMessage().contentEquals("Socket is closed")) {
					if(clientSocket != null) {
						this.server.notifyListeners(Server.EventType.SOCKET_CLOSED, e, clientSocket.getRemoteSocketAddress());
					} else {
						this.server.notifyListeners(Server.EventType.SOCKET_CLOSED, e);
					}
					Thread.currentThread().interrupt();
				}
			}
		}
		this.server.notifyListeners(Server.EventType.ACCEPT_END);
	}
	
	/**
	 * Closes all client connection and the serversocket itself
	 * @return true if the closing process finishes successfully
	 */
	public boolean close() {
		Thread.currentThread().interrupt();
		this.communicationHandler.forEach(ch -> ch.close());
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
	protected Server getServer() {
		return this.server;
	}

	/**
	 * Returns a list of CommunicationHandlers
	 * @return a list of CommunicationHandlers
	 */
	protected CopyOnWriteArrayList<CommunicationHandler> getCommunicationHandler() {
		return this.communicationHandler;
	}
}
