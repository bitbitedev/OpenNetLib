package dev.bitbite.networking;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

import dev.bitbite.networking.DataPreProcessor.TransferMode;
import dev.bitbite.networking.exceptions.LayerDisableFailedException;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents an abstract implementation of the server-side connection.<br>
 * The server must be started using {@link Server#start()} 
 * for clients to be able to connect. <br>
 * Shutting down the server can be done using {@link Server#close()}<br>
 * Incoming data from any client will be processed by the DataProcessingLayers and then
 * propagated to {@link Server#processReceivedData(String, byte[])}
 * containing the clients address of the client the data came from.
 * In order to send data to the client you must request the proper 
 * {@link CommunicationHandler} using the servers {@link ClientManager}
 * ({@link ClientManager#getCommunicationHandlerByIP(String)})
 *
 * @see ClientManager
 * @see CommunicationHandler
 * @see DataProcessingLayer
 * @see DataPreProcessor
 *
 * @version 0.0.2-alpha
 */
public abstract class Server {

	public final int PORT;
	@Getter protected ServerSocket serverSocket;
	@Getter protected ClientManager clientManager;
	@Getter protected DataPreProcessor dataPreProcessor;
	protected DisconnectedClientDetector disconnectedClientDetector;
	@Getter protected ArrayList<ServerListener> listeners;
	@Getter protected ArrayList<IOHandlerListener> iOListeners;
	@Getter @Setter private int SO_TIMEOUT = 0;
	
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
		this.dataPreProcessor = new DataPreProcessor();
		this.disconnectedClientDetector = new DisconnectedClientDetector(this);
		this.disconnectedClientDetector.setName("Disconnected Client Detector");
		this.listeners = new ArrayList<ServerListener>();
		this.iOListeners = new ArrayList<IOHandlerListener>();
	}
	
	/**
	 * Opens a {@link ServerSocket}, initializes the {@link DataProcessingLayer}s and starts listening on the specified port
	 */
	public void start() {
		notifyListeners(EventType.START);
		try {
			this.openServerSocket();
			this.serverSocket.setSoTimeout(SO_TIMEOUT);
			this.disconnectedClientDetector.start();
			this.dataPreProcessor.initLayers();
		} catch(Exception e) {
			this.notifyListeners(EventType.START_FAILED, e);
			return;
		}
		this.clientManager.start();
		this.notifyListeners(EventType.START_SUCCESS);
	}
	
	/**
	 * Opens the {@link ServerSocket}. 
	 * Moved to a different function to make it easier to replace the ServerSocket implementation.
	 * @throws IOException when the process of opening the ServerSocket fails.
	 */
	protected void openServerSocket() throws IOException {
		this.serverSocket = new ServerSocket(this.PORT);
	}
	
	/**
	 * Initiates the closing process of the Server with closing the {@link ClientManager} and disabling the {@link DataProcessingLayer}s.
	 * Finally it closes the serverSocket
	 */
	public void close() {
		this.notifyListeners(EventType.CLOSE);
		this.clientManager.close();
		try {
			this.dataPreProcessor.shutdown();
			this.serverSocket.close();
			this.disconnectedClientDetector.interrupt();
		} catch (LayerDisableFailedException | IOException e) {
			this.notifyListeners(EventType.CLOSE_FAILED, e);
		}
		this.notifyListeners(EventType.CLOSE_END);
	}
	
	/**
	 * This function will be called once the server receives data from the client.
	 * 
	 * @param clientAddress of the client the data came from
	 * @param data sent by the server
	 */
	protected abstract void processReceivedData(String clientAddress, byte[] data);

	public boolean send(String clientAddress, byte[] data) {
		data = this.dataPreProcessor.process(TransferMode.OUT, data);
		this.clientManager.getCommunicationHandlerByIP(clientAddress).send(data);
		return true;
	}
	
	/**
	 * Sends the data to all connected clients.
	 * @param data to broadcast
	 */
	public void broadcast(byte[] data) {
		byte[] processedData = this.dataPreProcessor.process(TransferMode.OUT, data);
		this.clientManager.getCommunicationHandler().forEach(ch -> ch.send(processedData));
	}
	
	/**
	 * Registers a ClientListener
	 * @param listener to add
	 */
	public void registerListener(ServerListener listener) {
		listeners.add(listener);
	}
	
	/**
	 * Registers an IOHandlerListener
	 * @param listener to add
	 */
	public void registerListener(IOHandlerListener listener) {
		iOListeners.add(listener);
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
		if(iOListeners.contains(listener)) {
			iOListeners.remove(listener);
		}
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
				} else if(args.length == 1) {
					if(!(args[0] instanceof Exception)) {
						throw new IllegalArgumentException("Expected object of type Exception, but got "+args[0].getClass().getSimpleName());
					}
					this.listeners.forEach(l -> l.onSocketClosed((Exception)args[0]));
				} else if(args.length >= 2) {
					if(!(args[0] instanceof Exception) || !(args[1] instanceof String)) {
						throw new IllegalArgumentException("Expected objects of type Exception and String, but got "+args[0].getClass().getSimpleName()+" and "+args[1].getClass().getSimpleName());
					}
					this.listeners.forEach(l -> l.onSocketClosed((Exception)args[0],(String)args[1]));
				}
				
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
				if(args.length == 0) {
					throw new IllegalArgumentException("Expected object of type CommunicationHandler, but got nothing");
				} else if(!(args[0] instanceof CommunicationHandler)) {
					throw new IllegalArgumentException("Expected object of type CommunicationHandler, but got "+args[0].getClass().getSimpleName());
				}
				this.listeners.forEach(l -> l.onCommunicationHandlerClose((CommunicationHandler)args[0]));
				break;
			case COMMUNICATIONHANDLER_CLOSE_END:
				if(args.length == 0) {
					throw new IllegalArgumentException("Expected object of type CommunicationHandler, but got nothing");
				} else if(!(args[0] instanceof CommunicationHandler)) {
					throw new IllegalArgumentException("Expected object of type CommunicationHandler, but got "+args[0].getClass().getSimpleName());
				}
				this.listeners.forEach(l -> l.onCommunicationHandlerCloseEnd((CommunicationHandler)args[0]));
				break;
			case COMMUNICATIONHANDLER_CLOSE_FAILED:
				if(args.length == 0) {
					throw new IllegalArgumentException("Expected object of type Exception, but got nothing");
				} else if(!(args[0] instanceof CommunicationHandler && args[1] instanceof Exception)) {
					throw new IllegalArgumentException("Expected objects of type CommunicationHandler and Exception, but got "+args[0].getClass().getSimpleName()+" and "+args[1].getClass().getSimpleName());
				}
				this.listeners.forEach(l -> l.onCommunicationHandlerCloseFailed((CommunicationHandler)args[0], (Exception)args[1]));
				break;
		}
	}
	
}