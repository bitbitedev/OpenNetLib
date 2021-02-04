package dev.bitbite.networking;

import java.net.ServerSocket;
import java.util.ArrayList;

/**
 * Represents an abstract implementation of the server-side connection.<br>
 * The server must be started using {@link Server#start()} 
 * for clients to be able to connect. <br>
 * Shutting down the server can be done using {@link Server#close()}<br>
 * Incomming data from any client will be propagated to {@link Server#processReceivedData(String, String)}
 * containing the clients address of the client the data came from.<br>
 * In order to send data to the client you must request the proper {@link CommunicationHandler} using the servers {@link ClientManager}
 * ({@link ClientManager#getCommunicationHandlerByIP(String)})
 *
 * @version 0.0.1-alpha
 */
public abstract class Server {

	public final int PORT;
	private ServerSocket serverSocket;
	private ClientManager clientManager;
	private ArrayList<ServerListener> listeners;
	
	/**
	 * The different event-types listeners can listen on
	 * 
	 * @see ServerListener
	 * @version 0.0.1-alpha
	 */
	enum EventType {
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
	
	/**
	 * Creates a server object and sets the port the server will try to listen on
	 * @param port to listen on
	 */
	public Server(int port) {
		this.PORT = port;
		this.clientManager = new ClientManager(this);
		this.clientManager.setName("ClientManager");
		this.listeners = new ArrayList<ServerListener>();
	}
	
	/**
	 * Opens a serversocket and starts listening on the specified port
	 * @return true if the server has been started successfully
	 */
	public boolean start() {
		notifyListeners(EventType.START);
		try {
			this.serverSocket = new ServerSocket(this.PORT);
		} catch(Exception e) {
			notifyListeners(EventType.START_FAILED);
		}
		this.clientManager.start();
		notifyListeners(EventType.START_SUCCESS);
		return true;
	}
	
	/**
	 * Initiates the closing process of the Server
	 * @return true if the closing process has been completed successfully
	 */
	public boolean close() {
		return this.clientManager.close();
	}
	
	/**
	 * This function will be called once the server receives data from the client.
	 * 
	 * @param clientAddress of the client the data came from
	 * @param data sent by the server
	 */
	protected abstract void processReceivedData(String clientAddress, String data);
	
	/**
	 * Calls the respective function of each listener depending on the event type.<br>
	 * Optionally Propagates additional info such as exceptions.
	 * 
	 * @param type of event that occured
	 * @param args optional additional data
	 * 
	 * @throws IllegalArgumentException if additional arguments are supplied
	 * whose types do not match the expected types of the listeners eventfunction
	 * 
	 * @see ServerListener
	 * 
	 * @version 0.0.1-alpha
	 */
	protected void notifyListeners(EventType type, Object... args) {
		switch(type) {
			case START:
				this.listeners.forEach(l -> l.onStart());
				break;
			case START_SUCCESS:
				this.listeners.forEach(l -> l.onStartSuccess());
				break;
			case START_FAILED:
				if(!(args[0] instanceof Exception)) {
					throw new IllegalArgumentException("Expected object of type Exception, but got "+args[0].getClass().getSimpleName());
				}
				this.listeners.forEach(l -> l.onStartFailed((Exception)args[0]));
				break;
			case ACCEPT:
				if(!(args[0] instanceof CommunicationHandler)) {
					throw new IllegalArgumentException("Expected object of type CommunicationHandler, but got "+args[0].getClass().getSimpleName());
				}
				this.listeners.forEach(l -> l.onAccept((CommunicationHandler)args[0]));
				break;
			case ACCEPT_END:
				this.listeners.forEach(l -> l.onAcceptEnd());
				break;
			case ACCEPT_START:
				this.listeners.forEach(l -> l.onAcceptStart());
				break;
			case ACCEPT_FAILED:
				if(!(args[0] instanceof Exception)) {
					throw new IllegalArgumentException("Expected object of type Exception, but got "+args[0].getClass().getSimpleName());
				}
				this.listeners.forEach(l -> l.onAcceptFailed((Exception)args[0]));
				break;
			case CLOSE:
				this.listeners.forEach(l -> l.onClose());
				break;
			case CLOSE_SUCCESS:
				this.listeners.forEach(l -> l.onCloseSuccess());
				break;
			case CLOSE_FAILED:
				if(!(args[0] instanceof Exception)) {
					throw new IllegalArgumentException("Expected object of type Exception, but got "+args[0].getClass().getSimpleName());
				}
				this.listeners.forEach(l -> l.onCloseFailed((Exception)args[0]));
				break;
		}
	}
	
	/**
	 * Returns the current ServerSocket
	 * @return the current ServerSocket
	 */
	protected ServerSocket getServerSocket() {
		return this.serverSocket;
	}
}