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
	private ArrayList<IOHandlerListener> ioListeners;
	
	/**
	 * The different event-types, which occur in the server, listeners can listen on
	 * 
	 * @see ServerListener
	 * @version 0.0.1-alpha
	 */
	enum EventType {
		ACCEPT,
		ACCEPT_END,
		ACCEPT_FAILED,
		ACCEPT_START,
		CLOSE,
		CLOSE_END,
		CLOSE_FAILED,
		COMMUNICATIONHANDLER_CLOSE,
		COMMUNICATIONHANDLER_CLOSE_END,
		COMMUNICATIONHANDLER_CLOSE_FAILED,
		COMMUNICATIONHANDLER_INIT_FAILED,
		SOCKET_CLOSED,
		START,
		START_FAILED,
		START_SUCCESS
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
		this.ioListeners = new ArrayList<IOHandlerListener>();
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
	 * Registers a ClientListener
	 * @param listener to add
	 */
	public void registerListener(ServerListener listener) {
		listeners.add(listener);
	}
	
	/**
	 * Registers a IOHandlerListener
	 * @param listener to add
	 */
	public void registerListener(IOHandlerListener listener) {
		ioListeners.add(listener);
	}
	
	/**
	 * Removes ClientListener from the listeners
	 * @param listener to remove
	 */
	public void removeListener(ServerListener listener) {
		if(listeners.contains(listener)) {
			listeners.remove(listener);
		}
	}
	
	/**
	 * Removes IOHandlerListener from the listeners
	 * @param listener to remove
	 */
	public void removeListener(IOHandlerListener listener) {
		if(ioListeners.contains(listener)) {
			ioListeners.remove(listener);
		}
	}
	
	/**
	 * Returns a list of all ServerListeners registered at the server
	 * @return the list of ServerListeners
	 */
	public ArrayList<ServerListener> getServerListeners(){
		return this.listeners;
	}
	
	/**
	 * Returns a list of all IOHandlers registered at the server
	 * @return the list of IOHandlers
	 */
	public ArrayList<IOHandlerListener> getIOHandlerListeners(){
		return this.ioListeners;
	}
	
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
	 * @version 0.0.2-alpha
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
				if(args.length == 0) {
					throw new IllegalArgumentException("Expected object of type Exception, but got nothing");
				} else if(!(args[0] instanceof Exception)) {
					throw new IllegalArgumentException("Expected object of type Exception, but got "+args[0].getClass().getSimpleName());
				}
				this.listeners.forEach(l -> l.onStartFailed((Exception)args[0]));
				break;
			case ACCEPT:
				if(args.length == 0) {
					throw new IllegalArgumentException("Expected object of type CommunicationHandler, but got nothing");
				} else if(!(args[0] instanceof CommunicationHandler)) {
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
				if(args.length == 0) {
					throw new IllegalArgumentException("Expected object of type Exception, but got nothing");
				} else if(!(args[0] instanceof Exception)) {
					throw new IllegalArgumentException("Expected object of type Exception, but got "+args[0].getClass().getSimpleName());
				}
				this.listeners.forEach(l -> l.onAcceptFailed((Exception)args[0]));
				break;
			case SOCKET_CLOSED:
				if(args.length == 0) {
					throw new IllegalArgumentException("Expected object of type Exception, but got nothing");
				} else if(!(args[0] instanceof Exception)) {
					throw new IllegalArgumentException("Expected object of type Exception, but got "+args[0].getClass().getSimpleName());
				}
				this.listeners.forEach(l -> l.onSocketClosed((Exception)args[0]));
				break;
			case CLOSE:
				this.listeners.forEach(l -> l.onClose());
				break;
			case CLOSE_END:
				this.listeners.forEach(l -> l.onCloseEnd());
				break;
			case CLOSE_FAILED:
				if(args.length == 0) {
					throw new IllegalArgumentException("Expected object of type Exception, but got nothing");
				} else if(!(args[0] instanceof Exception)) {
					throw new IllegalArgumentException("Expected object of type Exception, but got "+args[0].getClass().getSimpleName());
				}
				this.listeners.forEach(l -> l.onCloseFailed((Exception)args[0]));
				break;
			case COMMUNICATIONHANDLER_INIT_FAILED:
				if(args.length == 0) {
					throw new IllegalArgumentException("Expected object of type Exception, but got nothing");
				} else if(!(args[0] instanceof Exception)) {
					throw new IllegalArgumentException("Expected object of type Exception, but got "+args[0].getClass().getSimpleName());
				}
				this.listeners.forEach(l -> l.onCommunicationHandlerInitFailed((Exception)args[0]));
				break;
			case COMMUNICATIONHANDLER_CLOSE:
				this.listeners.forEach(l -> l.onCommunicationHandlerClose());
				break;
			case COMMUNICATIONHANDLER_CLOSE_END:
				this.listeners.forEach(l -> l.onCommunicationHandlerCloseEnd());
				break;
			case COMMUNICATIONHANDLER_CLOSE_FAILED:
				if(args.length == 0) {
					throw new IllegalArgumentException("Expected object of type Exception, but got nothing");
				} else if(!(args[0] instanceof Exception)) {
					throw new IllegalArgumentException("Expected object of type Exception, but got "+args[0].getClass().getSimpleName());
				}
				this.listeners.forEach(l -> l.onCommunicationHandlerCloseFailed((Exception)args[0]));
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